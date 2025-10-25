package io.elev8.resources;

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
     * Get the API path for this resource type.
     *
     * @return the API path
     */
    String getApiPath();
}
