package io.elev8.resources.workqueue;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * A composite rate limiter that combines multiple rate limiters.
 *
 * <p>When calculating the delay for an item, this limiter queries all
 * underlying limiters and returns the maximum delay. This ensures that
 * all rate limiting policies are satisfied.
 *
 * @param <T> the type of items being rate limited
 */
@Slf4j
public class MaxOfRateLimiter<T> implements RateLimiter<T> {

    private final List<RateLimiter<T>> limiters;

    /**
     * Creates a composite rate limiter from the given limiters.
     *
     * @param limiters the rate limiters to combine
     */
    @SafeVarargs
    public MaxOfRateLimiter(final RateLimiter<T>... limiters) {
        this(Arrays.asList(limiters));
    }

    /**
     * Creates a composite rate limiter from the given list of limiters.
     *
     * @param limiters the rate limiters to combine
     */
    public MaxOfRateLimiter(final List<RateLimiter<T>> limiters) {
        if (limiters == null || limiters.isEmpty()) {
            throw new IllegalArgumentException("limiters cannot be null or empty");
        }
        this.limiters = List.copyOf(limiters);
    }

    @Override
    public Duration when(final T item) {
        Duration maxDelay = Duration.ZERO;

        for (final RateLimiter<T> limiter : limiters) {
            final Duration delay = limiter.when(item);
            if (delay.compareTo(maxDelay) > 0) {
                maxDelay = delay;
            }
        }

        log.trace("MaxOf rate limit for {}: {}", item, maxDelay);
        return maxDelay;
    }

    @Override
    public void forget(final T item) {
        for (final RateLimiter<T> limiter : limiters) {
            limiter.forget(item);
        }
        log.trace("Forgot rate limiting across all limiters for: {}", item);
    }

    @Override
    public int numRequeues(final T item) {
        int max = 0;
        for (final RateLimiter<T> limiter : limiters) {
            final int count = limiter.numRequeues(item);
            if (count > max) {
                max = count;
            }
        }
        return max;
    }

    /**
     * Returns the number of underlying rate limiters.
     *
     * @return the number of limiters
     */
    public int size() {
        return limiters.size();
    }
}
