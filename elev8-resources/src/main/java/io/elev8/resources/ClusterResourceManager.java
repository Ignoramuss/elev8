package io.elev8.resources;

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
     * Get the API path for this resource type.
     *
     * @return the API path
     */
    String getApiPath();
}
