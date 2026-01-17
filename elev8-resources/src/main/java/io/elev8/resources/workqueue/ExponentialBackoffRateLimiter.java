package io.elev8.resources.workqueue;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A rate limiter that uses exponential backoff.
 *
 * <p>Each failure doubles the delay (up to a maximum) using the formula:
 * <pre>
 * delay = min(baseDelay * 2^(failures-1), maxDelay)
 * </pre>
 *
 * <p>For example, with baseDelay=5ms and maxDelay=1000s:
 * <ul>
 *   <li>1st failure: 5ms</li>
 *   <li>2nd failure: 10ms</li>
 *   <li>3rd failure: 20ms</li>
 *   <li>4th failure: 40ms</li>
 *   <li>... and so on until maxDelay is reached</li>
 * </ul>
 *
 * @param <T> the type of items being rate limited
 */
@Slf4j
public class ExponentialBackoffRateLimiter<T> implements RateLimiter<T> {

    private static final Duration DEFAULT_BASE_DELAY = Duration.ofMillis(5);
    private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(1000);

    private final Duration baseDelay;
    private final Duration maxDelay;
    private final Map<T, AtomicInteger> failures;

    /**
     * Creates an exponential backoff rate limiter with default settings.
     * Default baseDelay is 5ms, maxDelay is 1000s.
     */
    public ExponentialBackoffRateLimiter() {
        this(DEFAULT_BASE_DELAY, DEFAULT_MAX_DELAY);
    }

    /**
     * Creates an exponential backoff rate limiter with the specified delays.
     *
     * @param baseDelay the initial delay for the first failure
     * @param maxDelay the maximum delay cap
     */
    public ExponentialBackoffRateLimiter(final Duration baseDelay, final Duration maxDelay) {
        if (baseDelay == null || baseDelay.isNegative()) {
            throw new IllegalArgumentException("baseDelay must be non-negative");
        }
        if (maxDelay == null || maxDelay.isNegative()) {
            throw new IllegalArgumentException("maxDelay must be non-negative");
        }
        if (baseDelay.compareTo(maxDelay) > 0) {
            throw new IllegalArgumentException("baseDelay cannot be greater than maxDelay");
        }
        this.baseDelay = baseDelay;
        this.maxDelay = maxDelay;
        this.failures = new ConcurrentHashMap<>();
    }

    @Override
    public Duration when(final T item) {
        final AtomicInteger counter = failures.computeIfAbsent(item, k -> new AtomicInteger(0));
        final int failureCount = counter.incrementAndGet();

        final long baseMs = baseDelay.toMillis();
        final long maxMs = maxDelay.toMillis();

        final long backoffMs;
        if (failureCount == 1) {
            backoffMs = baseMs;
        } else {
            final int exponent = Math.min(failureCount - 1, 62);
            final long calculated = baseMs << exponent;
            if (calculated < 0 || calculated > maxMs) {
                backoffMs = maxMs;
            } else {
                backoffMs = calculated;
            }
        }

        final Duration delay = Duration.ofMillis(Math.min(backoffMs, maxMs));
        log.trace("Exponential backoff for {} (failure {}): {}", item, failureCount, delay);
        return delay;
    }

    @Override
    public void forget(final T item) {
        failures.remove(item);
        log.trace("Forgot rate limiting for: {}", item);
    }

    @Override
    public int numRequeues(final T item) {
        final AtomicInteger counter = failures.get(item);
        return counter != null ? counter.get() : 0;
    }

    /**
     * Returns the configured base delay.
     *
     * @return the base delay
     */
    public Duration getBaseDelay() {
        return baseDelay;
    }

    /**
     * Returns the configured maximum delay.
     *
     * @return the maximum delay
     */
    public Duration getMaxDelay() {
        return maxDelay;
    }
}
