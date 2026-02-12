package io.elev8.core.watch;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration options for resource change event streaming.
 * Allows customization of queue capacity, state tracking, and underlying watch behavior.
 */
@Getter
@Builder
public class StreamOptions {

    /**
     * The maximum number of events to buffer in the stream queue.
     * If the consumer is slower than the producer, the producer will block
     * when the queue is full, providing backpressure.
     */
    @Builder.Default
    private final int queueCapacity = 1000;

    /**
     * Whether to track previous resource state for change detection.
     * When enabled, UPDATED events will include the previous state of the resource.
     * Disable this for memory-constrained scenarios or when previous state is not needed.
     */
    @Builder.Default
    private final boolean trackPreviousState = true;

    /**
     * The underlying watch options for configuring the Kubernetes watch API call.
     * Includes settings like resourceVersion, timeout, and selectors.
     */
    @Builder.Default
    private final WatchOptions watchOptions = WatchOptions.defaults();

    /**
     * Creates a StreamOptions instance with default settings.
     * - Queue capacity: 1000
     * - Track previous state: true
     * - Watch options: defaults
     *
     * @return a new StreamOptions with default values
     */
    public static StreamOptions defaults() {
        return StreamOptions.builder().build();
    }

    /**
     * Creates a StreamOptions instance that wraps the given WatchOptions.
     *
     * @param watchOptions the watch options to use
     * @return a new StreamOptions with the specified watch options
     */
    public static StreamOptions from(final WatchOptions watchOptions) {
        return StreamOptions.builder()
                .watchOptions(watchOptions != null ? watchOptions : WatchOptions.defaults())
                .build();
    }

    /**
     * Creates a StreamOptions instance with state tracking disabled.
     * Use this for high-volume scenarios where previous state is not needed.
     *
     * @return a new StreamOptions without state tracking
     */
    public static StreamOptions withoutStateTracking() {
        return StreamOptions.builder()
                .trackPreviousState(false)
                .build();
    }

    /**
     * Creates a StreamOptions instance with a custom queue capacity.
     *
     * @param capacity the maximum queue size
     * @return a new StreamOptions with the specified capacity
     */
    public static StreamOptions withQueueCapacity(final int capacity) {
        return StreamOptions.builder()
                .queueCapacity(capacity)
                .build();
    }

    /**
     * Creates a StreamOptions instance with a label selector.
     *
     * @param labelSelector the label selector to filter resources
     * @return a new StreamOptions with the specified label selector
     */
    public static StreamOptions withLabelSelector(final String labelSelector) {
        return StreamOptions.builder()
                .watchOptions(WatchOptions.withLabelSelector(labelSelector))
                .build();
    }

    /**
     * Creates a StreamOptions instance with a field selector.
     *
     * @param fieldSelector the field selector to filter resources
     * @return a new StreamOptions with the specified field selector
     */
    public static StreamOptions withFieldSelector(final String fieldSelector) {
        return StreamOptions.builder()
                .watchOptions(WatchOptions.withFieldSelector(fieldSelector))
                .build();
    }
}
