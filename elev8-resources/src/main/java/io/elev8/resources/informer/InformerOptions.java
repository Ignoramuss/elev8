package io.elev8.resources.informer;

import io.elev8.core.watch.StreamOptions;
import lombok.Builder;
import lombok.Getter;

/**
 * Configuration options for creating an Informer.
 * Allows customization of streaming behavior and resource filtering.
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
}
