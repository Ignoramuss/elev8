package io.elev8.resources.informer;

import java.util.List;

/**
 * A function that extracts index values from a resource.
 * Used by {@link Indexer} to build secondary indices for efficient lookups.
 *
 * <p>An IndexFunc can return multiple values for a single resource,
 * allowing the resource to be indexed under multiple keys.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Index pods by node name
 * IndexFunc<Pod> byNode = pod -> {
 *     String nodeName = pod.getSpec().getNodeName();
 *     return nodeName != null ? List.of(nodeName) : List.of();
 * };
 *
 * // Index resources by labels (multiple values)
 * IndexFunc<Pod> byLabel = pod -> {
 *     return new ArrayList<>(pod.getMetadata().getLabels().values());
 * };
 * }</pre>
 *
 * @param <T> the type of resource to index
 */
@FunctionalInterface
public interface IndexFunc<T> {

    /**
     * Extracts index values from a resource.
     *
     * @param resource the resource to extract values from
     * @return a list of index values (may be empty, must not be null)
     */
    List<String> index(T resource);
}
