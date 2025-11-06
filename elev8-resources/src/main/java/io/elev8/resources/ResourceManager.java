package io.elev8.resources;

import io.elev8.core.patch.ApplyOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.watch.WatchOptions;
import io.elev8.core.watch.Watcher;

import java.util.List;

/**
 * Generic interface for managing Kubernetes resources with CRUD operations.
 *
 * @param <T> the resource type
 */
public interface ResourceManager<T extends KubernetesResource> {

    /**
     * List all resources in the namespace.
     *
     * @param namespace the namespace to list resources from
     * @return list of resources
     * @throws ResourceException if the operation fails
     */
    List<T> list(String namespace) throws ResourceException;

    /**
     * List all resources across all namespaces.
     *
     * @return list of resources
     * @throws ResourceException if the operation fails
     */
    List<T> listAllNamespaces() throws ResourceException;

    /**
     * Get a specific resource by name.
     *
     * @param namespace the namespace
     * @param name the resource name
     * @return the resource
     * @throws ResourceException if the operation fails
     */
    T get(String namespace, String name) throws ResourceException;

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
     * @param namespace the namespace
     * @param name the resource name
     * @throws ResourceException if the operation fails
     */
    void delete(String namespace, String name) throws ResourceException;

    /**
     * Patch an existing resource with partial updates.
     * Supports JSON Patch (RFC 6902), JSON Merge Patch (RFC 7396),
     * and Strategic Merge Patch (Kubernetes-specific).
     *
     * @param namespace the namespace
     * @param name the resource name
     * @param options patch options specifying patch type and behavior
     * @param patchBody the patch content (format depends on patch type)
     * @return the patched resource
     * @throws ResourceException if the operation fails
     */
    T patch(String namespace, String name, PatchOptions options, String patchBody) throws ResourceException;

    /**
     * Apply a resource configuration declaratively using Server-side Apply.
     * Server-side Apply tracks field-level ownership and handles conflicts automatically.
     * This is the recommended approach for declarative resource management.
     *
     * @param namespace the namespace
     * @param name the resource name
     * @param options apply options with required fieldManager
     * @param manifest the resource manifest (JSON or YAML format)
     * @return the applied resource
     * @throws ResourceException if the operation fails
     */
    T apply(String namespace, String name, ApplyOptions options, String manifest) throws ResourceException;

    /**
     * Get the API path for this resource type.
     *
     * @return the API path
     */
    String getApiPath();

    /**
     * Watch resources in a specific namespace for changes.
     * The watcher will receive events as resources are added, modified, or deleted.
     *
     * @param namespace the namespace to watch resources in
     * @param options watch options for configuring the watch behavior
     * @param watcher the callback to handle watch events
     * @throws ResourceException if the watch operation fails
     */
    void watch(String namespace, WatchOptions options, Watcher<T> watcher) throws ResourceException;

    /**
     * Watch resources across all namespaces for changes.
     * The watcher will receive events as resources are added, modified, or deleted.
     *
     * @param options watch options for configuring the watch behavior
     * @param watcher the callback to handle watch events
     * @throws ResourceException if the watch operation fails
     */
    void watchAllNamespaces(WatchOptions options, Watcher<T> watcher) throws ResourceException;
}
