package io.elev8.resources.workqueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExponentialBackoffRateLimiterTest {

    private ExponentialBackoffRateLimiter<String> limiter;

    @BeforeEach
    void setUp() {
        limiter = new ExponentialBackoffRateLimiter<>(Duration.ofMillis(10), Duration.ofMillis(1000));
    }

    @Test
    void shouldReturnBaseDelayForFirstFailure() {
        final Duration delay = limiter.when("item");

        assertThat(delay).isEqualTo(Duration.ofMillis(10));
    }

    @Test
    void shouldDoubleDelayForEachSubsequentFailure() {
        assertThat(limiter.when("item")).isEqualTo(Duration.ofMillis(10));
        assertThat(limiter.when("item")).isEqualTo(Duration.ofMillis(20));
        assertThat(limiter.when("item")).isEqualTo(Duration.ofMillis(40));
        assertThat(limiter.when("item")).isEqualTo(Duration.ofMillis(80));
        assertThat(limiter.when("item")).isEqualTo(Duration.ofMillis(160));
    }

    @Test
    void shouldCapAtMaxDelay() {
        for (int i = 0; i < 20; i++) {
            limiter.when("item");
        }

        final Duration delay = limiter.when("item");

        assertThat(delay).isEqualTo(Duration.ofMillis(1000));
    }

    @Test
    void shouldTrackSeparateItemsIndependently() {
        limiter.when("item1");
        limiter.when("item1");
        limiter.when("item1");

        final Duration delay1 = limiter.when("item1");
        final Duration delay2 = limiter.when("item2");

        assertThat(delay1).isEqualTo(Duration.ofMillis(80));
        assertThat(delay2).isEqualTo(Duration.ofMillis(10));
    }

    @Test
    void shouldResetCountOnForget() {
        limiter.when("item");
        limiter.when("item");
        limiter.when("item");
        assertThat(limiter.numRequeues("item")).isEqualTo(3);

        limiter.forget("item");

        assertThat(limiter.numRequeues("item")).isEqualTo(0);
        assertThat(limiter.when("item")).isEqualTo(Duration.ofMillis(10));
    }

    @Test
    void shouldReturnZeroRequeuesForUnknownItem() {
        assertThat(limiter.numRequeues("unknown")).isEqualTo(0);
    }

    @Test
    void shouldHandleForgetOfUnknownItem() {
        limiter.forget("unknown");
    }

    @Test
    void shouldRejectNegativeBaseDelay() {
        assertThatThrownBy(() -> new ExponentialBackoffRateLimiter<String>(
                Duration.ofMillis(-1), Duration.ofMillis(1000)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNegativeMaxDelay() {
        assertThatThrownBy(() -> new ExponentialBackoffRateLimiter<String>(
                Duration.ofMillis(10), Duration.ofMillis(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBaseDelayGreaterThanMaxDelay() {
        assertThatThrownBy(() -> new ExponentialBackoffRateLimiter<String>(
                Duration.ofMillis(1000), Duration.ofMillis(10)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCreateWithDefaultValues() {
        final ExponentialBackoffRateLimiter<String> defaultLimiter = new ExponentialBackoffRateLimiter<>();

        assertThat(defaultLimiter.getBaseDelay()).isEqualTo(Duration.ofMillis(5));
        assertThat(defaultLimiter.getMaxDelay()).isEqualTo(Duration.ofSeconds(1000));
    }

    @Test
    void shouldHandleZeroBaseDelay() {
        final ExponentialBackoffRateLimiter<String> zeroBaseLimiter =
                new ExponentialBackoffRateLimiter<>(Duration.ZERO, Duration.ofMillis(100));

        assertThat(zeroBaseLimiter.when("item")).isEqualTo(Duration.ZERO);
        assertThat(zeroBaseLimiter.when("item")).isEqualTo(Duration.ZERO);
    }

    @Test
    void shouldHandleVeryLargeFailureCount() {
        final ExponentialBackoffRateLimiter<String> largeLimiter =
                new ExponentialBackoffRateLimiter<>(Duration.ofMillis(1), Duration.ofSeconds(1000));

        for (int i = 0; i < 100; i++) {
            largeLimiter.when("item");
        }

        assertThat(largeLimiter.numRequeues("item")).isEqualTo(100);
        assertThat(largeLimiter.when("item")).isEqualTo(Duration.ofSeconds(1000));
    }
}
