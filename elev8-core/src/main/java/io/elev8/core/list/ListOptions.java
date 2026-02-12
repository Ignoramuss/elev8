package io.elev8.core.list;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration options for list operations on Kubernetes resources.
 * Supports server-side filtering via label selectors, field selectors,
 * and pagination via limit and continue tokens.
 */
@Getter
@Builder
public class ListOptions {

    /**
     * Label selector to filter resources by labels.
     * Example: "app=myapp,env=prod"
     */
    private final String labelSelector;

    /**
     * Field selector to filter resources by field values.
     * Example: "metadata.name=my-pod,status.phase=Running"
     */
    private final String fieldSelector;

    /**
     * Maximum number of results to return per page.
     * When set, the server may return a continue token for retrieving the next page.
     */
    private final Integer limit;

    /**
     * Token for retrieving the next page of results.
     * Named continueToken because "continue" is a Java reserved word.
     * Sent to the API as the "continue" query parameter.
     */
    private final String continueToken;

    /**
     * The resource version to use for the list request.
     * When set, results are served from the API server's cache at that version.
     */
    private final String resourceVersion;

    /**
     * Creates a default ListOptions instance with no filters or pagination.
     *
     * @return a new ListOptions with default values
     */
    public static ListOptions defaults() {
        return ListOptions.builder().build();
    }

    /**
     * Creates a ListOptions instance with a field selector.
     *
     * @param fieldSelector the field selector to filter resources
     * @return a new ListOptions with the specified field selector
     */
    public static ListOptions withFieldSelector(final String fieldSelector) {
        return ListOptions.builder()
                .fieldSelector(fieldSelector)
                .build();
    }

    /**
     * Creates a ListOptions instance with a label selector.
     *
     * @param labelSelector the label selector to filter resources
     * @return a new ListOptions with the specified label selector
     */
    public static ListOptions withLabelSelector(final String labelSelector) {
        return ListOptions.builder()
                .labelSelector(labelSelector)
                .build();
    }

    /**
     * Creates a ListOptions instance with a result limit for pagination.
     *
     * @param limit the maximum number of results per page
     * @return a new ListOptions with the specified limit
     */
    public static ListOptions withLimit(final int limit) {
        return ListOptions.builder()
                .limit(limit)
                .build();
    }
}
