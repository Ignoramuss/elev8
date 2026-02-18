package io.elev8.resources.aggregation;

import io.elev8.resources.KubernetesResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Immutable container for multi-type resource query results.
 */
public final class AggregateResult {

    private final Map<ResourceType, List<? extends KubernetesResource>> results;

    AggregateResult(final Map<ResourceType, List<? extends KubernetesResource>> results) {
        final EnumMap<ResourceType, List<? extends KubernetesResource>> copy = new EnumMap<>(ResourceType.class);
        for (final Map.Entry<ResourceType, List<? extends KubernetesResource>> entry : results.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        this.results = copy;
    }

    /**
     * Returns the resources for the given type, or an empty list if absent.
     */
    public List<? extends KubernetesResource> get(final ResourceType type) {
        return results.getOrDefault(type, Collections.emptyList());
    }

    /**
     * Returns a flattened list of all resources across all types.
     */
    public List<KubernetesResource> getAll() {
        final List<KubernetesResource> all = new ArrayList<>();
        for (final List<? extends KubernetesResource> list : results.values()) {
            all.addAll(list);
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * Returns the set of resource types present in this result.
     */
    public Set<ResourceType> types() {
        return Collections.unmodifiableSet(results.keySet());
    }

    /**
     * Returns the total number of resources across all types.
     */
    public int size() {
        int total = 0;
        for (final List<? extends KubernetesResource> list : results.values()) {
            total += list.size();
        }
        return total;
    }

    /**
     * Returns the number of resources for a specific type.
     */
    public int size(final ResourceType type) {
        return results.getOrDefault(type, Collections.emptyList()).size();
    }

    /**
     * Returns true if no resources are present.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Derives a {@link ResourceCounts} from this result.
     */
    public ResourceCounts counts() {
        final EnumMap<ResourceType, Integer> counts = new EnumMap<>(ResourceType.class);
        for (final Map.Entry<ResourceType, List<? extends KubernetesResource>> entry : results.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().size());
        }
        return new ResourceCounts(counts);
    }
}
