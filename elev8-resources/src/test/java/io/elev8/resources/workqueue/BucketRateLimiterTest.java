package io.elev8.resources.workqueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BucketRateLimiterTest {

    private BucketRateLimiter<String> limiter;

    @BeforeEach
    void setUp() {
        limiter = new BucketRateLimiter<>(10.0, 5);
    }

    @Test
    void shouldAllowImmediatelyWithAvailableTokens() {
        final Duration delay = limiter.when("item");

        assertThat(delay).isEqualTo(Duration.ZERO);
    }

    @Test
    void shouldAllowBurstCapacityRequests() {
        for (int i = 0; i < 5; i++) {
            final Duration delay = limiter.when("item-" + i);
            assertThat(delay).isEqualTo(Duration.ZERO);
        }
    }

    @Test
    void shouldDelayWhenTokensExhausted() {
        for (int i = 0; i < 5; i++) {
            limiter.when("item-" + i);
        }

        final Duration delay = limiter.when("item-6");

        assertThat(delay).isGreaterThan(Duration.ZERO);
    }

    @Test
    void shouldRefillTokensOverTime() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            limiter.when("item-" + i);
        }

        Thread.sleep(150);

        final Duration delay = limiter.when("item-new");
        assertThat(delay).isEqualTo(Duration.ZERO);
    }

    @Test
    void shouldTrackRequeuesPerItem() {
        limiter.when("item");
        limiter.when("item");
        limiter.when("item");

        assertThat(limiter.numRequeues("item")).isEqualTo(3);
    }

    @Test
    void shouldResetRequeuesOnForget() {
        limiter.when("item");
        limiter.when("item");
        assertThat(limiter.numRequeues("item")).isEqualTo(2);

        limiter.forget("item");

        assertThat(limiter.numRequeues("item")).isEqualTo(0);
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
    void shouldRejectZeroRate() {
        assertThatThrownBy(() -> new BucketRateLimiter<String>(0, 100))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNegativeRate() {
        assertThatThrownBy(() -> new BucketRateLimiter<String>(-1.0, 100))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectZeroBurst() {
        assertThatThrownBy(() -> new BucketRateLimiter<String>(10.0, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNegativeBurst() {
        assertThatThrownBy(() -> new BucketRateLimiter<String>(10.0, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCreateWithDefaultValues() {
        final BucketRateLimiter<String> defaultLimiter = new BucketRateLimiter<>();

        assertThat(defaultLimiter.getTokensPerSecond()).isEqualTo(10.0);
        assertThat(defaultLimiter.getBurstCapacity()).isEqualTo(100);
    }

    @Test
    void shouldNotExceedBurstCapacityAfterRefill() throws InterruptedException {
        final BucketRateLimiter<String> smallLimiter = new BucketRateLimiter<>(1000.0, 3);

        Thread.sleep(100);

        assertThat(smallLimiter.when("item1")).isEqualTo(Duration.ZERO);
        assertThat(smallLimiter.when("item2")).isEqualTo(Duration.ZERO);
        assertThat(smallLimiter.when("item3")).isEqualTo(Duration.ZERO);

        final Duration delay = smallLimiter.when("item4");
        assertThat(delay.toMillis()).isGreaterThanOrEqualTo(0);
    }
}
