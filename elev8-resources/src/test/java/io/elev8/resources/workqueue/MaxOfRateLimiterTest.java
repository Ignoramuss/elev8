package io.elev8.resources.workqueue;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaxOfRateLimiterTest {

    @Test
    void shouldReturnMaxDelayFromMultipleLimiters() {
        final RateLimiter<String> fast = new ExponentialBackoffRateLimiter<>(Duration.ofMillis(5), Duration.ofMillis(100));
        final RateLimiter<String> slow = new ExponentialBackoffRateLimiter<>(Duration.ofMillis(50), Duration.ofMillis(1000));

        final MaxOfRateLimiter<String> combined = new MaxOfRateLimiter<>(fast, slow);

        final Duration delay = combined.when("item");

        assertThat(delay).isEqualTo(Duration.ofMillis(50));
    }

    @Test
    void shouldPropagateForgetToAllLimiters() {
        final ExponentialBackoffRateLimiter<String> limiter1 = new ExponentialBackoffRateLimiter<>();
        final ExponentialBackoffRateLimiter<String> limiter2 = new ExponentialBackoffRateLimiter<>();
        final MaxOfRateLimiter<String> combined = new MaxOfRateLimiter<>(limiter1, limiter2);

        combined.when("item");
        combined.when("item");

        assertThat(limiter1.numRequeues("item")).isEqualTo(2);
        assertThat(limiter2.numRequeues("item")).isEqualTo(2);

        combined.forget("item");

        assertThat(limiter1.numRequeues("item")).isEqualTo(0);
        assertThat(limiter2.numRequeues("item")).isEqualTo(0);
    }

    @Test
    void shouldReturnMaxRequeuesFromAllLimiters() {
        final ExponentialBackoffRateLimiter<String> limiter1 = new ExponentialBackoffRateLimiter<>();
        final ExponentialBackoffRateLimiter<String> limiter2 = new ExponentialBackoffRateLimiter<>();

        limiter1.when("item");
        limiter1.when("item");
        limiter1.when("item");

        limiter2.when("item");

        final MaxOfRateLimiter<String> combined = new MaxOfRateLimiter<>(limiter1, limiter2);

        assertThat(combined.numRequeues("item")).isEqualTo(3);
    }

    @Test
    void shouldAcceptListOfLimiters() {
        final List<RateLimiter<String>> limiters = Arrays.asList(
                new ExponentialBackoffRateLimiter<>(),
                new BucketRateLimiter<>()
        );

        final MaxOfRateLimiter<String> combined = new MaxOfRateLimiter<>(limiters);

        assertThat(combined.size()).isEqualTo(2);
    }

    @Test
    void shouldRejectNullLimiters() {
        assertThatThrownBy(() -> new MaxOfRateLimiter<>((RateLimiter<String>[]) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectEmptyLimiters() {
        assertThatThrownBy(() -> new MaxOfRateLimiter<>(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldWorkWithSingleLimiter() {
        final ExponentialBackoffRateLimiter<String> single = new ExponentialBackoffRateLimiter<>(
                Duration.ofMillis(100), Duration.ofMillis(1000));
        final MaxOfRateLimiter<String> combined = new MaxOfRateLimiter<>(single);

        assertThat(combined.when("item")).isEqualTo(Duration.ofMillis(100));
        assertThat(combined.when("item")).isEqualTo(Duration.ofMillis(200));
    }

    @Test
    void shouldCombineExponentialAndBucketLimiters() {
        final ExponentialBackoffRateLimiter<String> exponential =
                new ExponentialBackoffRateLimiter<>(Duration.ofMillis(10), Duration.ofSeconds(1));
        final BucketRateLimiter<String> bucket = new BucketRateLimiter<>(10.0, 2);

        final MaxOfRateLimiter<String> combined = new MaxOfRateLimiter<>(exponential, bucket);

        assertThat(combined.when("item1")).isEqualTo(Duration.ofMillis(10));
        assertThat(combined.when("item2")).isEqualTo(Duration.ofMillis(10));

        final Duration delay = combined.when("item3");
        assertThat(delay).isGreaterThanOrEqualTo(Duration.ofMillis(10));
    }

    @Test
    void shouldReturnZeroRequeuesForUnknownItem() {
        final MaxOfRateLimiter<String> combined = new MaxOfRateLimiter<>(
                new ExponentialBackoffRateLimiter<>(),
                new BucketRateLimiter<>()
        );

        assertThat(combined.numRequeues("unknown")).isEqualTo(0);
    }
}
