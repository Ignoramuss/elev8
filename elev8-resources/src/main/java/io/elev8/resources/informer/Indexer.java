package io.elev8.resources.informer;

import io.elev8.resources.KubernetesResource;

import java.util.List;
import java.util.Set;

/**
 * An extension of {@link Store} that supports secondary indexing.
 * Allows efficient lookups by custom criteria beyond the primary namespace/name key.
 *
 * <p>Secondary indices are maintained automatically when resources are added,
 * updated, or deleted from the store.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Indexer<Pod> indexer = new InMemoryIndexer<>();
 *
 * // Add an index for pods by node name
 * indexer.addIndex("byNode", pod -> {
 *     String nodeName = pod.getSpec().getNodeName();
 *     return nodeName != null ? List.of(nodeName) : List.of();
 * });
 *
 * // Add resources
 * indexer.add(pod1);
 * indexer.add(pod2);
 *
 * // Query by index
 * List<Pod> podsOnNode1 = indexer.getByIndex("byNode", "node-1");
 * }</pre>
 *
 * @param <T> the type of Kubernetes resource stored
 */
public interface Indexer<T extends KubernetesResource> extends Store<T> {

    /**
     * Adds a secondary index to this store.
     * The IndexFunc will be applied to all existing resources and to
     * any resources added in the future.
     *
     * @param indexName the name of the index (e.g., "byNode", "byLabel")
     * @param indexFunc the function that extracts index values from resources
     * @throws IllegalArgumentException if an index with this name already exists
     */
    void addIndex(String indexName, IndexFunc<T> indexFunc);

    /**
     * Gets all resources matching an index value.
     *
     * @param indexName the name of the index
     * @param indexValue the value to search for
     * @return list of matching resources, empty if none match
     * @throws IllegalArgumentException if the index does not exist
     */
    List<T> getByIndex(String indexName, String indexValue);

    /**
     * Gets all primary keys for resources matching an index value.
     *
     * @param indexName the name of the index
     * @param indexValue the value to search for
     * @return list of matching primary keys (namespace/name format), empty if none match
     * @throws IllegalArgumentException if the index does not exist
     */
    List<String> getIndexKeys(String indexName, String indexValue);

    /**
     * Gets all distinct values present in a secondary index.
     *
     * @param indexName the name of the index
     * @return all distinct values in the index
     * @throws IllegalArgumentException if the index does not exist
     */
    Set<String> getAllIndexValues(String indexName);

    /**
     * Lists all registered index names.
     *
     * @return immutable set of index names
     */
    Set<String> getIndexNames();
}
