package io.elev8.resources.informer;

import io.elev8.core.watch.StreamOptions;
import io.elev8.core.watch.WatchOptions;
import io.elev8.resources.ClusterResourceManager;
import io.elev8.resources.InformerException;
import io.elev8.resources.KubernetesResource;
import io.elev8.resources.ResourceException;
import io.elev8.resources.ResourceManager;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Default implementation of SharedInformerBuilder.
 * Collects configuration and creates or retrieves SharedIndexInformers.
 *
 * @param <T> the resource type
 */
public class DefaultSharedInformerBuilder<T extends KubernetesResource> implements SharedInformerBuilder<T> {

    private final DefaultSharedInformerFactory factory;
    private final Class<T> resourceClass;
    private final Duration defaultResyncPeriod;

    private String namespace;
    private boolean allNamespaces = true;
    private String labelSelector;
    private String fieldSelector;
    private Duration resyncPeriod;
    private ResourceManager<T> resourceManager;
    private ClusterResourceManager<T> clusterResourceManager;

    DefaultSharedInformerBuilder(
            final DefaultSharedInformerFactory factory,
            final Class<T> resourceClass,
            final Duration defaultResyncPeriod) {
        this.factory = factory;
        this.resourceClass = resourceClass;
        this.defaultResyncPeriod = defaultResyncPeriod;
    }

    @Override
    public SharedInformerBuilder<T> inNamespace(final String namespace) {
        if (namespace == null || namespace.isEmpty()) {
            throw new IllegalArgumentException("Namespace cannot be null or empty");
        }
        this.namespace = namespace;
        this.allNamespaces = false;
        return this;
    }

    @Override
    public SharedInformerBuilder<T> inAllNamespaces() {
        this.namespace = null;
        this.allNamespaces = true;
        return this;
    }

    @Override
    public SharedInformerBuilder<T> withLabelSelector(final String labelSelector) {
        this.labelSelector = labelSelector;
        return this;
    }

    @Override
    public SharedInformerBuilder<T> withFieldSelector(final String fieldSelector) {
        this.fieldSelector = fieldSelector;
        return this;
    }

    @Override
    public SharedInformerBuilder<T> withResyncPeriod(final Duration resyncPeriod) {
        this.resyncPeriod = resyncPeriod;
        return this;
    }

    @Override
    public SharedInformerBuilder<T> withResourceManager(final ResourceManager<T> manager) {
        this.resourceManager = manager;
        return this;
    }

    @Override
    public SharedInformerBuilder<T> withClusterResourceManager(final ClusterResourceManager<T> manager) {
        this.clusterResourceManager = manager;
        return this;
    }

    @Override
    public SharedIndexInformer<T> build() {
        validateConfiguration();

        final SharedInformerKey key = new SharedInformerKey(
                resourceClass,
                namespace,
                labelSelector,
                fieldSelector
        );

        final Duration effectiveResync = resyncPeriod != null ? resyncPeriod : defaultResyncPeriod;
        final StreamOptions streamOptions = buildStreamOptions();

        final Supplier<List<T>> listSupplier;
        final Supplier<io.elev8.core.watch.ResourceChangeStream<T>> streamSupplier;

        if (clusterResourceManager != null) {
            listSupplier = () -> safeList(clusterResourceManager::list);
            streamSupplier = () -> safeStream(() -> clusterResourceManager.stream(streamOptions));
        } else if (resourceManager != null) {
            if (allNamespaces) {
                listSupplier = () -> safeList(resourceManager::listAllNamespaces);
                streamSupplier = () -> safeStream(() -> resourceManager.streamAllNamespaces(streamOptions));
            } else {
                listSupplier = () -> safeList(() -> resourceManager.list(namespace));
                streamSupplier = () -> safeStream(() -> resourceManager.stream(namespace, streamOptions));
            }
        } else {
            throw new IllegalStateException(
                    "No ResourceManager or ClusterResourceManager configured. " +
                    "Call withResourceManager() or withClusterResourceManager() before build().");
        }

        final DefaultSharedIndexInformer<T> informer = new DefaultSharedIndexInformer<>(
                listSupplier,
                streamSupplier,
                effectiveResync
        );

        return factory.registerInformer(key, informer);
    }

    private void validateConfiguration() {
        if (resourceManager == null && clusterResourceManager == null) {
            throw new IllegalStateException(
                    "Either resourceManager or clusterResourceManager must be set");
        }
        if (resourceManager != null && clusterResourceManager != null) {
            throw new IllegalStateException(
                    "Cannot set both resourceManager and clusterResourceManager");
        }
        if (clusterResourceManager != null && !allNamespaces && namespace != null) {
            throw new IllegalStateException(
                    "Cluster-scoped resources do not support namespace filtering");
        }
    }

    private StreamOptions buildStreamOptions() {
        final WatchOptions.WatchOptionsBuilder watchBuilder = WatchOptions.builder();

        if (labelSelector != null && !labelSelector.isEmpty()) {
            watchBuilder.labelSelector(labelSelector);
        }
        if (fieldSelector != null && !fieldSelector.isEmpty()) {
            watchBuilder.fieldSelector(fieldSelector);
        }

        return StreamOptions.builder()
                .watchOptions(watchBuilder.build())
                .build();
    }

    @FunctionalInterface
    private interface ListSupplier<T> {
        List<T> get() throws ResourceException;
    }

    @FunctionalInterface
    private interface StreamSupplier<T> {
        io.elev8.core.watch.ResourceChangeStream<T> get() throws ResourceException;
    }

    private List<T> safeList(final ListSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ResourceException e) {
            throw new InformerException("Failed to list resources", e);
        }
    }

    private io.elev8.core.watch.ResourceChangeStream<T> safeStream(final StreamSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ResourceException e) {
            throw new InformerException("Failed to start resource stream", e);
        }
    }
}
