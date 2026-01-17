package io.elev8.resources.workqueue;

import java.time.Duration;

/**
 * Factory for creating a rate limiter with sensible defaults for Kubernetes controllers.
 *
 * <p>Creates a {@link MaxOfRateLimiter} combining:
 * <ul>
 *   <li>{@link ExponentialBackoffRateLimiter} with 5ms base delay and 1000s max delay</li>
 *   <li>{@link BucketRateLimiter} with 10 tokens/second and 100 burst capacity</li>
 * </ul>
 *
 * <p>This combination provides:
 * <ul>
 *   <li>Per-item exponential backoff for repeated failures</li>
 *   <li>Global rate limiting to prevent overwhelming the API server</li>
 * </ul>
 */
public final class DefaultControllerRateLimiter {

    private static final Duration DEFAULT_BASE_DELAY = Duration.ofMillis(5);
    private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(1000);
    private static final double DEFAULT_RATE = 10.0;
    private static final int DEFAULT_BURST = 100;

    private DefaultControllerRateLimiter() {
    }

    /**
     * Creates a new rate limiter with default settings.
     *
     * @param <T> the type of items being rate limited
     * @return a new rate limiter
     */
    public static <T> RateLimiter<T> create() {
        return create(DEFAULT_BASE_DELAY, DEFAULT_MAX_DELAY, DEFAULT_RATE, DEFAULT_BURST);
    }

    /**
     * Creates a new rate limiter with custom settings.
     *
     * @param <T> the type of items being rate limited
     * @param baseDelay the base delay for exponential backoff
     * @param maxDelay the maximum delay for exponential backoff
     * @param tokensPerSecond the token bucket rate
     * @param burstCapacity the token bucket burst capacity
     * @return a new rate limiter
     */
    public static <T> RateLimiter<T> create(final Duration baseDelay,
                                            final Duration maxDelay,
                                            final double tokensPerSecond,
                                            final int burstCapacity) {
        return new MaxOfRateLimiter<>(
                new ExponentialBackoffRateLimiter<>(baseDelay, maxDelay),
                new BucketRateLimiter<>(tokensPerSecond, burstCapacity)
        );
    }
}
