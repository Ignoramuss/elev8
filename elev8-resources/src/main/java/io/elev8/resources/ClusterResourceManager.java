package io.elev8.resources;

import io.elev8.core.patch.ApplyOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.watch.ResourceChangeStream;
import io.elev8.core.watch.StreamOptions;
import io.elev8.core.watch.WatchOptions;
import io.elev8.core.watch.Watcher;

import java.util.List;

/**
 * Interface for managing cluster-scoped Kubernetes resources.
 * Unlike namespaced resources, cluster-scoped resources (like PersistentVolume, Namespace, etc.)
 * are not confined to a specific namespace and exist at the cluster level.
 *
 * @param <T> the resource type
 */
public interface ClusterResourceManager<T extends KubernetesResource> {

    /**
     * List all resources in the cluster.
     *
     * @return list of resources
     * @throws ResourceException if the operation fails
     */
    List<T> list() throws ResourceException;

    /**
     * Get a specific resource by name.
     *
     * @param name the resource name
     * @return the resource
     * @throws ResourceException if the operation fails
     */
    T get(String name) throws ResourceException;

    /**
     * Create a new resource.
     *
     * @param resource the resource to create
     * @return the created resource
     * @throws ResourceException if the operation fails
     */
    T create(T resource) throws ResourceException;

    /**
     * Update an existing resource.
     *
     * @param resource the resource to update
     * @return the updated resource
     * @throws ResourceException if the operation fails
     */
    T update(T resource) throws ResourceException;

    /**
     * Delete a resource.
     *
     * @param name the resource name
     * @throws ResourceException if the operation fails
     */
    void delete(String name) throws ResourceException;

    /**
     * Patch an existing cluster-scoped resource with partial updates.
     * Supports JSON Patch (RFC 6902), JSON Merge Patch (RFC 7396),
     * and Strategic Merge Patch (Kubernetes-specific).
     *
     * @param name the resource name
     * @param options patch options specifying patch type and behavior
     * @param patchBody the patch content (format depends on patch type)
     * @return the patched resource
     * @throws ResourceException if the operation fails
     */
    T patch(String name, PatchOptions options, String patchBody) throws ResourceException;

    /**
     * Apply a cluster-scoped resource configuration declaratively using Server-side Apply.
     * Server-side Apply tracks field-level ownership and handles conflicts automatically.
     * This is the recommended approach for declarative resource management.
     *
     * @param name the resource name
     * @param options apply options with required fieldManager
     * @param manifest the resource manifest (JSON or YAML format)
     * @return the applied resource
     * @throws ResourceException if the operation fails
     */
    T apply(String name, ApplyOptions options, String manifest) throws ResourceException;

    /**
     * Get the API path for this resource type.
     *
     * @return the API path
     */
    String getApiPath();

    /**
     * Watch cluster-scoped resources for changes.
     * The watcher will receive events as resources are added, modified, or deleted.
     *
     * @param options watch options for configuring the watch behavior
     * @param watcher the callback to handle watch events
     * @throws ResourceException if the watch operation fails
     */
    void watch(WatchOptions options, Watcher<T> watcher) throws ResourceException;

    /**
     * Stream resource change events for cluster-scoped resources.
     * Returns a ResourceChangeStream that can be iterated or converted to a Java Stream.
     * The caller is responsible for closing the stream when done.
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * try (ResourceChangeStream<Namespace> stream = namespaceManager.stream(StreamOptions.defaults())) {
     *     stream.stream()
     *         .filter(ResourceChangeEvent::isCreated)
     *         .forEach(e -> System.out.println("New namespace: " + e.getResource().getName()));
     * }
     * }</pre>
     *
     * @param options stream options for configuring the stream behavior
     * @return a ResourceChangeStream for iterating over events
     * @throws ResourceException if the stream operation fails to start
     */
    ResourceChangeStream<T> stream(StreamOptions options) throws ResourceException;
}
