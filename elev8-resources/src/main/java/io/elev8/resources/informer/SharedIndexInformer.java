package io.elev8.resources.informer;

import io.elev8.resources.KubernetesResource;

/**
 * SharedIndexInformer extends SharedInformer with secondary indexing capabilities.
 * This is the most commonly used interface for shared informers, as it allows
 * efficient queries beyond the primary namespace/name key.
 *
 * <p>Indices can be added before the informer is started to enable queries like
 * "find all pods on node X" or "find all services with label app=myapp".</p>
 *
 * @param <T> the type of Kubernetes resource
 */
public interface SharedIndexInformer<T extends KubernetesResource> extends SharedInformer<T> {

    /**
     * Returns the indexer for secondary index queries.
     *
     * @return the resource indexer
     */
    Indexer<T> getIndexer();

    /**
     * Adds a secondary index to the backing store.
     * Must be called before the informer is started.
     *
     * @param indexName the name of the index
     * @param indexFunc the function to extract index values from resources
     * @throws IllegalStateException if the informer has already started
     */
    void addIndex(String indexName, IndexFunc<T> indexFunc);

    /**
     * {@inheritDoc}
     * Returns the indexer as the store (indexer extends store).
     */
    @Override
    default Store<T> getStore() {
        return getIndexer();
    }
}
