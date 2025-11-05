package io.elev8.core.watch;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration options for watch operations on Kubernetes resources.
 * These options control how the watch behaves and what events are received.
 */
@Getter
@Builder
public class WatchOptions {
    /**
     * The resource version to start watching from.
     * If not specified, watches from the current state.
     * If specified, watches from changes after this version.
     */
    private final String resourceVersion;

    /**
     * Timeout in seconds for the watch operation.
     * The server will close the connection after this time.
     * The watch can be resumed using the last resourceVersion received.
     */
    @Builder.Default
    private final Integer timeoutSeconds = 300;

    /**
     * If true, enables bookmark events for more efficient watch resumption.
     * Bookmark events contain only resourceVersion updates without object data.
     */
    @Builder.Default
    private final Boolean allowWatchBookmarks = true;

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
     * Creates a default WatchOptions instance with standard settings.
     *
     * @return a new WatchOptions with default values
     */
    public static WatchOptions defaults() {
        return WatchOptions.builder().build();
    }

    /**
     * Creates a WatchOptions instance starting from a specific resource version.
     *
     * @param resourceVersion the resource version to start watching from
     * @return a new WatchOptions with the specified resource version
     */
    public static WatchOptions from(final String resourceVersion) {
        return WatchOptions.builder()
                .resourceVersion(resourceVersion)
                .build();
    }

    /**
     * Creates a WatchOptions instance with a label selector.
     *
     * @param labelSelector the label selector to filter resources
     * @return a new WatchOptions with the specified label selector
     */
    public static WatchOptions withLabelSelector(final String labelSelector) {
        return WatchOptions.builder()
                .labelSelector(labelSelector)
                .build();
    }

    /**
     * Creates a WatchOptions instance with a field selector.
     *
     * @param fieldSelector the field selector to filter resources
     * @return a new WatchOptions with the specified field selector
     */
    public static WatchOptions withFieldSelector(final String fieldSelector) {
        return WatchOptions.builder()
                .fieldSelector(fieldSelector)
                .build();
    }
}
