package io.elev8.resources.workqueue;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A rate limiter using the token bucket algorithm.
 *
 * <p>This provides global rate limiting across all items. Tokens are generated
 * at a fixed rate up to a burst capacity. Each request consumes one token.
 * When no tokens are available, requests are delayed proportionally.
 *
 * <p>Unlike {@link ExponentialBackoffRateLimiter}, this limits the overall
 * rate of requests regardless of which item is being processed.
 *
 * @param <T> the type of items being rate limited
 */
@Slf4j
public class BucketRateLimiter<T> implements RateLimiter<T> {

    private static final double DEFAULT_RATE = 10.0;
    private static final int DEFAULT_BURST = 100;

    private final double tokensPerSecond;
    private final int burstCapacity;
    private final Map<T, AtomicInteger> requeues;

    private final AtomicLong lastRefillTimeNanos;
    private double availableTokens;
    private final Object tokenLock = new Object();

    /**
     * Creates a bucket rate limiter with default settings.
     * Default rate is 10 tokens/second with burst capacity of 100.
     */
    public BucketRateLimiter() {
        this(DEFAULT_RATE, DEFAULT_BURST);
    }

    /**
     * Creates a bucket rate limiter with the specified rate and burst.
     *
     * @param tokensPerSecond the rate at which tokens are generated
     * @param burstCapacity the maximum number of tokens that can accumulate
     */
    public BucketRateLimiter(final double tokensPerSecond, final int burstCapacity) {
        if (tokensPerSecond <= 0) {
            throw new IllegalArgumentException("tokensPerSecond must be positive");
        }
        if (burstCapacity <= 0) {
            throw new IllegalArgumentException("burstCapacity must be positive");
        }
        this.tokensPerSecond = tokensPerSecond;
        this.burstCapacity = burstCapacity;
        this.requeues = new ConcurrentHashMap<>();
        this.lastRefillTimeNanos = new AtomicLong(System.nanoTime());
        this.availableTokens = burstCapacity;
    }

    @Override
    public Duration when(final T item) {
        requeues.computeIfAbsent(item, k -> new AtomicInteger(0)).incrementAndGet();

        final Duration delay;
        synchronized (tokenLock) {
            refillTokens();

            if (availableTokens >= 1.0) {
                availableTokens -= 1.0;
                delay = Duration.ZERO;
            } else {
                final double tokensNeeded = 1.0 - availableTokens;
                final long delayNanos = (long) (tokensNeeded / tokensPerSecond * 1_000_000_000);
                availableTokens = 0;
                delay = Duration.ofNanos(delayNanos);
            }
        }

        log.trace("Bucket rate limit for {}: delay {}", item, delay);
        return delay;
    }

    private void refillTokens() {
        final long now = System.nanoTime();
        final long lastRefill = lastRefillTimeNanos.get();
        final long elapsedNanos = now - lastRefill;

        if (elapsedNanos > 0) {
            final double tokensToAdd = (elapsedNanos / 1_000_000_000.0) * tokensPerSecond;
            availableTokens = Math.min(burstCapacity, availableTokens + tokensToAdd);
            lastRefillTimeNanos.set(now);
        }
    }

    @Override
    public void forget(final T item) {
        requeues.remove(item);
        log.trace("Forgot rate limiting for: {}", item);
    }

    @Override
    public int numRequeues(final T item) {
        final AtomicInteger counter = requeues.get(item);
        return counter != null ? counter.get() : 0;
    }

    /**
     * Returns the configured tokens per second rate.
     *
     * @return the rate in tokens per second
     */
    public double getTokensPerSecond() {
        return tokensPerSecond;
    }

    /**
     * Returns the configured burst capacity.
     *
     * @return the burst capacity
     */
    public int getBurstCapacity() {
        return burstCapacity;
    }
}
