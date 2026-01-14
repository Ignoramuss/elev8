package io.elev8.resources.informer;

import io.elev8.resources.KubernetesResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe in-memory implementation of {@link Indexer} that extends {@link InMemoryStore}
 * with secondary indexing capabilities.
 *
 * <p>Indices are maintained automatically when resources are added, updated, or deleted.
 * All index operations are thread-safe using concurrent data structures.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Indexer<Pod> indexer = new InMemoryIndexer<>();
 * indexer.addIndex("byNode", pod -> {
 *     String node = pod.getSpec().getNodeName();
 *     return node != null ? List.of(node) : List.of();
 * });
 *
 * indexer.add(pod1);
 * List<Pod> podsOnNode = indexer.getByIndex("byNode", "node-1");
 * }</pre>
 *
 * @param <T> the type of Kubernetes resource stored
 */
public class InMemoryIndexer<T extends KubernetesResource> extends InMemoryStore<T> implements Indexer<T> {

    private final Map<String, IndexFunc<T>> indexFunctions = new ConcurrentHashMap<>();

    private final Map<String, Map<String, Set<String>>> indices = new ConcurrentHashMap<>();

    @Override
    public void addIndex(final String indexName, final IndexFunc<T> indexFunc) {
        if (indexName == null || indexName.isEmpty()) {
            throw new IllegalArgumentException("Index name cannot be null or empty");
        }
        if (indexFunc == null) {
            throw new IllegalArgumentException("Index function cannot be null");
        }
        if (indexFunctions.containsKey(indexName)) {
            throw new IllegalArgumentException("Index '" + indexName + "' already exists");
        }

        indexFunctions.put(indexName, indexFunc);
        indices.put(indexName, new ConcurrentHashMap<>());

        for (final T resource : list()) {
            updateIndexForResource(indexName, indexFunc, resource);
        }
    }

    @Override
    public List<T> getByIndex(final String indexName, final String indexValue) {
        validateIndexExists(indexName);

        final Map<String, Set<String>> indexMap = indices.get(indexName);
        final Set<String> keys = indexMap.get(indexValue);
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        final List<T> result = new ArrayList<>();
        for (final String key : keys) {
            final T resource = getByKey(key);
            if (resource != null) {
                result.add(resource);
            }
        }
        return result;
    }

    @Override
    public List<String> getIndexKeys(final String indexName, final String indexValue) {
        validateIndexExists(indexName);

        final Map<String, Set<String>> indexMap = indices.get(indexName);
        final Set<String> keys = indexMap.get(indexValue);
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(keys);
    }

    @Override
    public Set<String> getAllIndexValues(final String indexName) {
        validateIndexExists(indexName);

        final Map<String, Set<String>> indexMap = indices.get(indexName);
        return Collections.unmodifiableSet(new HashSet<>(indexMap.keySet()));
    }

    @Override
    public Set<String> getIndexNames() {
        return Collections.unmodifiableSet(new HashSet<>(indexFunctions.keySet()));
    }

    @Override
    public void add(final T resource) {
        if (resource == null) {
            return;
        }

        final String key = keyFor(resource.getNamespace(), resource.getName());
        final T oldResource = getByKey(key);

        if (oldResource != null) {
            removeFromIndices(oldResource);
        }

        super.add(resource);
        addToIndices(resource);
    }

    @Override
    public void update(final T resource) {
        add(resource);
    }

    @Override
    public void delete(final T resource) {
        if (resource == null) {
            return;
        }

        removeFromIndices(resource);
        super.delete(resource);
    }

    @Override
    public void replace(final List<T> resources) {
        clearIndices();
        super.replace(resources);

        if (resources != null) {
            for (final T resource : resources) {
                addToIndices(resource);
            }
        }
    }

    @Override
    public void clear() {
        clearIndices();
        super.clear();
    }

    private void addToIndices(final T resource) {
        final String key = keyFor(resource.getNamespace(), resource.getName());

        for (final Map.Entry<String, IndexFunc<T>> entry : indexFunctions.entrySet()) {
            final String indexName = entry.getKey();
            final IndexFunc<T> indexFunc = entry.getValue();
            final List<String> indexValues = indexFunc.index(resource);

            if (indexValues != null) {
                final Map<String, Set<String>> indexMap = indices.get(indexName);
                for (final String indexValue : indexValues) {
                    if (indexValue != null) {
                        indexMap.computeIfAbsent(indexValue, k -> ConcurrentHashMap.newKeySet())
                                .add(key);
                    }
                }
            }
        }
    }

    private void removeFromIndices(final T resource) {
        final String key = keyFor(resource.getNamespace(), resource.getName());

        for (final Map.Entry<String, IndexFunc<T>> entry : indexFunctions.entrySet()) {
            final String indexName = entry.getKey();
            final IndexFunc<T> indexFunc = entry.getValue();
            final List<String> indexValues = indexFunc.index(resource);

            if (indexValues != null) {
                final Map<String, Set<String>> indexMap = indices.get(indexName);
                for (final String indexValue : indexValues) {
                    if (indexValue != null) {
                        final Set<String> keys = indexMap.get(indexValue);
                        if (keys != null) {
                            keys.remove(key);
                            if (keys.isEmpty()) {
                                indexMap.remove(indexValue);
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateIndexForResource(final String indexName, final IndexFunc<T> indexFunc, final T resource) {
        final String key = keyFor(resource.getNamespace(), resource.getName());
        final List<String> indexValues = indexFunc.index(resource);

        if (indexValues != null) {
            final Map<String, Set<String>> indexMap = indices.get(indexName);
            for (final String indexValue : indexValues) {
                if (indexValue != null) {
                    indexMap.computeIfAbsent(indexValue, k -> ConcurrentHashMap.newKeySet())
                            .add(key);
                }
            }
        }
    }

    private void clearIndices() {
        for (final Map<String, Set<String>> indexMap : indices.values()) {
            indexMap.clear();
        }
    }

    private void validateIndexExists(final String indexName) {
        if (!indexFunctions.containsKey(indexName)) {
            throw new IllegalArgumentException("Index '" + indexName + "' does not exist");
        }
    }
}
