package io.elev8.resources.aggregation;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Immutable mapping of {@link ResourceType} to resource count.
 */
public final class ResourceCounts {

    private final Map<ResourceType, Integer> counts;

    ResourceCounts(final Map<ResourceType, Integer> counts) {
        this.counts = new EnumMap<>(counts);
    }

    /**
     * Returns the count for the given type, or 0 if absent.
     */
    public int get(final ResourceType type) {
        return counts.getOrDefault(type, 0);
    }

    /**
     * Returns the sum of all counts.
     */
    public int total() {
        int sum = 0;
        for (final int count : counts.values()) {
            sum += count;
        }
        return sum;
    }

    /**
     * Returns an unmodifiable view of the underlying map.
     */
    public Map<ResourceType, Integer> asMap() {
        return Collections.unmodifiableMap(counts);
    }

    /**
     * Returns true if the total count is 0.
     */
    public boolean isEmpty() {
        return total() == 0;
    }

    /**
     * Returns the set of types that have a non-zero count.
     */
    public Set<ResourceType> types() {
        return Collections.unmodifiableSet(counts.keySet());
    }
}
