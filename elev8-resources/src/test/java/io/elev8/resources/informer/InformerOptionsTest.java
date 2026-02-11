package io.elev8.resources.informer;

import io.elev8.core.watch.StreamOptions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InformerOptionsTest {

    @Test
    void shouldCreateDefaultOptions() {
        final InformerOptions options = InformerOptions.defaults();

        assertThat(options).isNotNull();
        assertThat(options.getStreamOptions()).isNotNull();
    }

    @Test
    void shouldCreateWithStreamOptions() {
        final StreamOptions streamOptions = StreamOptions.withQueueCapacity(500);
        final InformerOptions options = InformerOptions.withStreamOptions(streamOptions);

        assertThat(options.getStreamOptions()).isEqualTo(streamOptions);
        assertThat(options.getStreamOptions().getQueueCapacity()).isEqualTo(500);
    }

    @Test
    void shouldCreateWithLabelSelector() {
        final InformerOptions options = InformerOptions.withLabelSelector("app=nginx");

        assertThat(options.getStreamOptions()).isNotNull();
        assertThat(options.getStreamOptions().getWatchOptions().getLabelSelector()).isEqualTo("app=nginx");
    }

    @Test
    void shouldCreateWithFieldSelector() {
        final InformerOptions options = InformerOptions.withFieldSelector("metadata.name=my-pod");

        assertThat(options.getStreamOptions()).isNotNull();
        assertThat(options.getStreamOptions().getWatchOptions().getFieldSelector()).isEqualTo("metadata.name=my-pod");
    }

    @Test
    void shouldHandleNullStreamOptions() {
        final InformerOptions options = InformerOptions.withStreamOptions(null);

        assertThat(options.getStreamOptions()).isNotNull();
    }

    @Test
    void shouldBuildWithCustomOptions() {
        final StreamOptions streamOptions = StreamOptions.builder()
                .queueCapacity(2000)
                .trackPreviousState(false)
                .build();

        final InformerOptions options = InformerOptions.builder()
                .streamOptions(streamOptions)
                .build();

        assertThat(options.getStreamOptions().getQueueCapacity()).isEqualTo(2000);
        assertThat(options.getStreamOptions().isTrackPreviousState()).isFalse();
    }
}
