package io.elev8.core.watch;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StreamOptionsTest {

    @Nested
    class Defaults {

        @Test
        void shouldHaveDefaultQueueCapacity() {
            final StreamOptions options = StreamOptions.defaults();
            assertThat(options.getQueueCapacity()).isEqualTo(1000);
        }

        @Test
        void shouldTrackPreviousStateByDefault() {
            final StreamOptions options = StreamOptions.defaults();
            assertThat(options.isTrackPreviousState()).isTrue();
        }

        @Test
        void shouldHaveDefaultWatchOptions() {
            final StreamOptions options = StreamOptions.defaults();
            assertThat(options.getWatchOptions()).isNotNull();
        }
    }

    @Nested
    class FromWatchOptions {

        @Test
        void shouldWrapWatchOptions() {
            final WatchOptions watchOptions = WatchOptions.withLabelSelector("app=test");
            final StreamOptions options = StreamOptions.from(watchOptions);

            assertThat(options.getWatchOptions()).isSameAs(watchOptions);
            assertThat(options.getQueueCapacity()).isEqualTo(1000);
            assertThat(options.isTrackPreviousState()).isTrue();
        }

        @Test
        void shouldHandleNullWatchOptions() {
            final StreamOptions options = StreamOptions.from(null);

            assertThat(options.getWatchOptions()).isNotNull();
        }
    }

    @Nested
    class WithoutStateTracking {

        @Test
        void shouldDisableStateTracking() {
            final StreamOptions options = StreamOptions.withoutStateTracking();

            assertThat(options.isTrackPreviousState()).isFalse();
            assertThat(options.getQueueCapacity()).isEqualTo(1000);
        }
    }

    @Nested
    class WithQueueCapacity {

        @Test
        void shouldSetCustomQueueCapacity() {
            final StreamOptions options = StreamOptions.withQueueCapacity(5000);

            assertThat(options.getQueueCapacity()).isEqualTo(5000);
            assertThat(options.isTrackPreviousState()).isTrue();
        }
    }

    @Nested
    class WithLabelSelector {

        @Test
        void shouldSetLabelSelector() {
            final StreamOptions options = StreamOptions.withLabelSelector("app=myapp");

            assertThat(options.getWatchOptions().getLabelSelector()).isEqualTo("app=myapp");
            assertThat(options.getQueueCapacity()).isEqualTo(1000);
        }
    }

    @Nested
    class Builder {

        @Test
        void shouldBuildWithAllOptions() {
            final WatchOptions watchOptions = WatchOptions.builder()
                    .labelSelector("env=prod")
                    .timeoutSeconds(600)
                    .build();

            final StreamOptions options = StreamOptions.builder()
                    .queueCapacity(2000)
                    .trackPreviousState(false)
                    .watchOptions(watchOptions)
                    .build();

            assertThat(options.getQueueCapacity()).isEqualTo(2000);
            assertThat(options.isTrackPreviousState()).isFalse();
            assertThat(options.getWatchOptions().getLabelSelector()).isEqualTo("env=prod");
            assertThat(options.getWatchOptions().getTimeoutSeconds()).isEqualTo(600);
        }
    }
}
