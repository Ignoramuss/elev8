package io.elev8.resources;

import io.elev8.resources.informer.Informer;
import io.elev8.resources.informer.InformerOptions;
import io.elev8.core.watch.ResourceChangeStream;

import java.util.Collections;
import java.util.List;

/**
 * Factory utility for creating Informers from ResourceManagers.
 * Provides convenient methods for creating namespace-scoped, all-namespace, and cluster-scoped informers.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * // Namespace-scoped informer
 * Informer<Pod> podInformer = Informers.forNamespace(podManager, "default", InformerOptions.defaults());
 *
 * // All namespaces informer
 * Informer<Pod> allPodsInformer = Informers.forAllNamespaces(podManager, InformerOptions.defaults());
 *
 * // Cluster-scoped informer
 * Informer<Namespace> nsInformer = Informers.forClusterResource(namespaceManager, InformerOptions.defaults());
 * }</pre>
 */
public final class Informers {

    private Informers() {
    }

    /**
     * Creates an Informer for resources in a specific namespace.
     *
     * @param manager the resource manager
     * @param namespace the namespace to watch
     * @param options informer options
     * @param <T> the resource type
     * @return a new Informer
     */
    public static <T extends KubernetesResource> Informer<T> forNamespace(
            final ResourceManager<T> manager,
            final String namespace,
            final InformerOptions options) {

        if (manager == null) {
            throw new IllegalArgumentException("ResourceManager cannot be null");
        }
        if (namespace == null || namespace.isEmpty()) {
            throw new IllegalArgumentException("Namespace cannot be null or empty");
        }

        final InformerOptions effectiveOptions = options != null ? options : InformerOptions.defaults();

        return new Informer<>(
                () -> safeList(() -> manager.list(namespace)),
                () -> safeStream(() -> manager.stream(namespace, effectiveOptions.getStreamOptions()))
        );
    }

    /**
     * Creates an Informer for resources across all namespaces.
     *
     * @param manager the resource manager
     * @param options informer options
     * @param <T> the resource type
     * @return a new Informer
     */
    public static <T extends KubernetesResource> Informer<T> forAllNamespaces(
            final ResourceManager<T> manager,
            final InformerOptions options) {

        if (manager == null) {
            throw new IllegalArgumentException("ResourceManager cannot be null");
        }

        final InformerOptions effectiveOptions = options != null ? options : InformerOptions.defaults();

        return new Informer<>(
                () -> safeList(manager::listAllNamespaces),
                () -> safeStream(() -> manager.streamAllNamespaces(effectiveOptions.getStreamOptions()))
        );
    }

    /**
     * Creates an Informer for cluster-scoped resources.
     *
     * @param manager the cluster resource manager
     * @param options informer options
     * @param <T> the resource type
     * @return a new Informer
     */
    public static <T extends KubernetesResource> Informer<T> forClusterResource(
            final ClusterResourceManager<T> manager,
            final InformerOptions options) {

        if (manager == null) {
            throw new IllegalArgumentException("ClusterResourceManager cannot be null");
        }

        final InformerOptions effectiveOptions = options != null ? options : InformerOptions.defaults();

        return new Informer<>(
                () -> safeList(manager::list),
                () -> safeStream(() -> manager.stream(effectiveOptions.getStreamOptions()))
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
