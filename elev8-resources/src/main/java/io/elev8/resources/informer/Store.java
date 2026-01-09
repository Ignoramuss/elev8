package io.elev8.resources.informer;

import java.util.List;

/**
 * Interface for storing and retrieving Kubernetes resources by their key.
 * A Store maintains an in-memory cache of resources that can be accessed
 * by namespace/name or by a composite key.
 *
 * <p>For namespace-scoped resources, keys are formatted as "namespace/name".
 * For cluster-scoped resources, keys are just "name".</p>
 *
 * <p>Implementations must be thread-safe for concurrent access.</p>
 *
 * @param <T> the type of resource stored
 */
public interface Store<T> {

    /**
     * Adds a resource to the store.
     * If a resource with the same key already exists, it is replaced.
     *
     * @param resource the resource to add
     */
    void add(T resource);

    /**
     * Updates a resource in the store.
     * This is semantically equivalent to add() but makes the intent clearer.
     *
     * @param resource the resource to update
     */
    void update(T resource);

    /**
     * Deletes a resource from the store.
     *
     * @param resource the resource to delete (used to extract the key)
     */
    void delete(T resource);

    /**
     * Gets a resource by namespace and name.
     *
     * @param namespace the resource namespace (null for cluster-scoped resources)
     * @param name the resource name
     * @return the resource, or null if not found
     */
    T get(String namespace, String name);

    /**
     * Gets a resource by its composite key.
     *
     * @param key the resource key (namespace/name or just name)
     * @return the resource, or null if not found
     */
    T getByKey(String key);

    /**
     * Lists all resources in the store.
     *
     * @return a list of all stored resources
     */
    List<T> list();

    /**
     * Lists all keys in the store.
     *
     * @return a list of all resource keys
     */
    List<String> listKeys();

    /**
     * Checks if a resource with the given key exists in the store.
     *
     * @param key the resource key to check
     * @return true if the key exists, false otherwise
     */
    boolean containsKey(String key);

    /**
     * Returns the number of resources in the store.
     *
     * @return the store size
     */
    int size();

    /**
     * Replaces all resources in the store with the given list.
     * This is typically used during initial sync or resync operations.
     *
     * @param resources the new set of resources
     */
    void replace(List<T> resources);

    /**
     * Clears all resources from the store.
     */
    void clear();
}
