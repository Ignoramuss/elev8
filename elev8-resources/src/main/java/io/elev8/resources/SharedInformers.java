package io.elev8.resources;

import io.elev8.core.watch.ResourceChangeStream;
import io.elev8.core.watch.StreamOptions;
import io.elev8.resources.informer.DefaultSharedIndexInformer;
import io.elev8.resources.informer.InformerOptions;
import io.elev8.resources.informer.SharedIndexInformer;

import java.time.Duration;
import java.util.List;

/**
 * Factory utility for creating standalone SharedInformers from ResourceManagers.
 * Use this when you need a single SharedInformer without a factory.
 * For managing multiple SharedInformers, use {@link io.elev8.resources.informer.SharedInformerFactory}.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * // Namespace-scoped shared informer
 * SharedIndexInformer<Pod> podInformer = SharedInformers.forNamespace(
 *     podManager, "default", InformerOptions.defaults());
 *
 * // Add handlers
 * podInformer.addEventHandler(myHandler);
 * podInformer.addEventHandler(anotherHandler);
 *
 * // Start and use
 * podInformer.run();
 * }</pre>
 */
public final class SharedInformers {

    private SharedInformers() {
    }

    /**
     * Creates a SharedIndexInformer for resources in a specific namespace.
     *
     * @param manager the resource manager
     * @param namespace the namespace to watch
     * @param options informer options
     * @param <T> the resource type
     * @return a new SharedIndexInformer
     */
    public static <T extends KubernetesResource> SharedIndexInformer<T> forNamespace(
            final ResourceManager<T> manager,
            final String namespace,
            final InformerOptions options) {

        return forNamespace(manager, namespace, options, Duration.ZERO);
    }

    /**
     * Creates a SharedIndexInformer for resources in a specific namespace with resync.
     *
     * @param manager the resource manager
     * @param namespace the namespace to watch
     * @param options informer options
     * @param resyncPeriod default resync period for handlers
     * @param <T> the resource type
     * @return a new SharedIndexInformer
     */
    public static <T extends KubernetesResource> SharedIndexInformer<T> forNamespace(
            final ResourceManager<T> manager,
            final String namespace,
            final InformerOptions options,
            final Duration resyncPeriod) {

        if (manager == null) {
            throw new IllegalArgumentException("ResourceManager cannot be null");
        }
        if (namespace == null || namespace.isEmpty()) {
            throw new IllegalArgumentException("Namespace cannot be null or empty");
        }

        final InformerOptions effectiveOptions = options != null ? options : InformerOptions.defaults();
        final StreamOptions streamOptions = effectiveOptions.getStreamOptions();

        return new DefaultSharedIndexInformer<>(
                () -> safeList(() -> manager.list(namespace)),
                () -> safeStream(() -> manager.stream(namespace, streamOptions)),
                resyncPeriod
        );
    }

    /**
     * Creates a SharedIndexInformer for resources across all namespaces.
     *
     * @param manager the resource manager
     * @param options informer options
     * @param <T> the resource type
     * @return a new SharedIndexInformer
     */
    public static <T extends KubernetesResource> SharedIndexInformer<T> forAllNamespaces(
            final ResourceManager<T> manager,
            final InformerOptions options) {

        return forAllNamespaces(manager, options, Duration.ZERO);
    }

    /**
     * Creates a SharedIndexInformer for resources across all namespaces with resync.
     *
     * @param manager the resource manager
     * @param options informer options
     * @param resyncPeriod default resync period for handlers
     * @param <T> the resource type
     * @return a new SharedIndexInformer
     */
    public static <T extends KubernetesResource> SharedIndexInformer<T> forAllNamespaces(
            final ResourceManager<T> manager,
            final InformerOptions options,
            final Duration resyncPeriod) {

        if (manager == null) {
            throw new IllegalArgumentException("ResourceManager cannot be null");
        }

        final InformerOptions effectiveOptions = options != null ? options : InformerOptions.defaults();
        final StreamOptions streamOptions = effectiveOptions.getStreamOptions();

        return new DefaultSharedIndexInformer<>(
                () -> safeList(manager::listAllNamespaces),
                () -> safeStream(() -> manager.streamAllNamespaces(streamOptions)),
                resyncPeriod
        );
    }

    /**
     * Creates a SharedIndexInformer for cluster-scoped resources.
     *
     * @param manager the cluster resource manager
     * @param options informer options
     * @param <T> the resource type
     * @return a new SharedIndexInformer
     */
    public static <T extends KubernetesResource> SharedIndexInformer<T> forClusterResource(
            final ClusterResourceManager<T> manager,
            final InformerOptions options) {

        return forClusterResource(manager, options, Duration.ZERO);
    }

    /**
     * Creates a SharedIndexInformer for cluster-scoped resources with resync.
     *
     * @param manager the cluster resource manager
     * @param options informer options
     * @param resyncPeriod default resync period for handlers
     * @param <T> the resource type
     * @return a new SharedIndexInformer
     */
    public static <T extends KubernetesResource> SharedIndexInformer<T> forClusterResource(
            final ClusterResourceManager<T> manager,
            final InformerOptions options,
            final Duration resyncPeriod) {

        if (manager == null) {
            throw new IllegalArgumentException("ClusterResourceManager cannot be null");
        }

        final InformerOptions effectiveOptions = options != null ? options : InformerOptions.defaults();
        final StreamOptions streamOptions = effectiveOptions.getStreamOptions();

        return new DefaultSharedIndexInformer<>(
                () -> safeList(manager::list),
                () -> safeStream(() -> manager.stream(streamOptions)),
                resyncPeriod
        );
    }

    @FunctionalInterface
    private interface ListSupplier<T> {
        List<T> get() throws ResourceException;
    }

    @FunctionalInterface
    private interface StreamSupplier<T> {
        ResourceChangeStream<T> get() throws ResourceException;
    }

    private static <T> List<T> safeList(final ListSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ResourceException e) {
            throw new InformerException("Failed to list resources", e);
        }
    }

    private static <T> ResourceChangeStream<T> safeStream(final StreamSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ResourceException e) {
            throw new InformerException("Failed to start resource stream", e);
        }
    }
}
