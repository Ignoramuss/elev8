package io.elev8.resources.informer;

import io.elev8.resources.KubernetesResource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe in-memory implementation of {@link Store} backed by a {@link ConcurrentHashMap}.
 *
 * <p>Resources are keyed by namespace/name for namespace-scoped resources,
 * or just name for cluster-scoped resources.</p>
 *
 * @param <T> the type of Kubernetes resource stored
 */
public class InMemoryStore<T extends KubernetesResource> implements Store<T> {

    private final ConcurrentHashMap<String, T> items = new ConcurrentHashMap<>();

    @Override
    public void add(final T resource) {
        if (resource == null) {
            return;
        }
        final String key = generateKey(resource);
        items.put(key, resource);
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
        final String key = generateKey(resource);
        items.remove(key);
    }

    @Override
    public T get(final String namespace, final String name) {
        if (name == null) {
            return null;
        }
        final String key = namespace != null ? namespace + "/" + name : name;
        return items.get(key);
    }

    @Override
    public T getByKey(final String key) {
        if (key == null) {
            return null;
        }
        return items.get(key);
    }

    @Override
    public List<T> list() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<String> listKeys() {
        return new ArrayList<>(items.keySet());
    }

    @Override
    public boolean containsKey(final String key) {
        return key != null && items.containsKey(key);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public void replace(final List<T> resources) {
        items.clear();
        if (resources != null) {
            for (final T resource : resources) {
                add(resource);
            }
        }
    }

    @Override
    public void clear() {
        items.clear();
    }

    /**
     * Generates the store key for a resource.
     * Format: "namespace/name" for namespace-scoped resources, "name" for cluster-scoped.
     *
     * @param resource the resource to generate a key for
     * @return the store key
     */
    private String generateKey(final T resource) {
        final String namespace = resource.getNamespace();
        final String name = resource.getName();
        return namespace != null ? namespace + "/" + name : name;
    }

    /**
     * Generates the store key from namespace and name.
     *
     * @param namespace the namespace (may be null for cluster-scoped resources)
     * @param name the resource name
     * @return the store key
     */
    public static String keyFor(final String namespace, final String name) {
        return namespace != null ? namespace + "/" + name : name;
    }

    /**
     * Generates the store key for a resource.
     *
     * @param resource the resource to generate a key for
     * @param <R> the resource type
     * @return the store key
     */
    public static <R extends KubernetesResource> String keyFor(final R resource) {
        if (resource == null) {
            return null;
        }
        return keyFor(resource.getNamespace(), resource.getName());
    }
}
