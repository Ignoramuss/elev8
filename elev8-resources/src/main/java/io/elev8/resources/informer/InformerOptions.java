package io.elev8.resources.informer;

import io.elev8.core.watch.StreamOptions;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

/**
 * Configuration options for creating an Informer.
 * Allows customization of streaming behavior, resource filtering, and resync behavior.
 */
@Getter
@Builder
public class InformerOptions {

    /**
     * Options for the underlying resource change stream.
     * Includes queue capacity, state tracking, and watch options.
     */
    @Builder.Default
    private final StreamOptions streamOptions = StreamOptions.defaults();

    /**
     * Resync period in milliseconds. When greater than 0, the Informer will periodically
     * re-list all resources and fire synthetic update events to handlers.
     * Default is 0 (resync disabled).
     */
    @Builder.Default
    private final long resyncPeriodMillis = 0;

    /**
     * Creates an InformerOptions instance with default settings.
     *
     * @return a new InformerOptions with default values
     */
    public static InformerOptions defaults() {
        return InformerOptions.builder().build();
    }

    /**
     * Creates an InformerOptions instance with the given StreamOptions.
     *
     * @param streamOptions the stream options to use
     * @return a new InformerOptions with the specified stream options
     */
    public static InformerOptions withStreamOptions(final StreamOptions streamOptions) {
        return InformerOptions.builder()
                .streamOptions(streamOptions != null ? streamOptions : StreamOptions.defaults())
                .build();
    }

    /**
     * Creates an InformerOptions instance with a label selector.
     *
     * @param labelSelector the label selector to filter resources
     * @return a new InformerOptions with the specified label selector
     */
    public static InformerOptions withLabelSelector(final String labelSelector) {
        return InformerOptions.builder()
                .streamOptions(StreamOptions.withLabelSelector(labelSelector))
                .build();
    }

    /**
     * Creates an InformerOptions instance with a resync period.
     *
     * @param periodMillis the resync period in milliseconds (0 to disable)
     * @return a new InformerOptions with the specified resync period
     */
    public static InformerOptions withResyncPeriod(final long periodMillis) {
        return InformerOptions.builder()
                .resyncPeriodMillis(periodMillis)
                .build();
    }

    /**
     * Creates an InformerOptions instance with a resync period.
     *
     * @param period the resync period duration (null or zero to disable)
     * @return a new InformerOptions with the specified resync period
     */
    public static InformerOptions withResyncPeriod(final Duration period) {
        final long millis = period != null ? period.toMillis() : 0;
        return InformerOptions.builder()
                .resyncPeriodMillis(millis)
                .build();
    }
}
