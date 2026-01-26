package io.elev8.core.http;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe token bucket rate limiter implementation.
 * Tokens are refilled at a constant rate up to the burst capacity.
 */
@Slf4j
public final class TokenBucketRateLimiter {

    private final double tokensPerMillis;
    private final int burstCapacity;
    private final Lock lock;

    private double availableTokens;
    private long lastRefillTimestamp;

    /**
     * Creates a new token bucket rate limiter with the given configuration.
     *
     * @param config the rate limiter configuration
     * @throws IllegalArgumentException if configuration is invalid
     */
    public TokenBucketRateLimiter(final RateLimiterConfig config) {
        config.validate();
        this.tokensPerMillis = config.getRequestsPerSecond() / 1000.0;
        this.burstCapacity = config.getBurstCapacity();
        this.availableTokens = burstCapacity;
        this.lastRefillTimestamp = System.currentTimeMillis();
        this.lock = new ReentrantLock();
    }

    /**
     * Acquires a token, blocking until one is available.
     * Returns immediately if a token is available.
     */
    public void acquire() {
        lock.lock();
        try {
            refillTokens();

            while (availableTokens < 1.0) {
                final long waitMillis = calculateWaitTime();
                lock.unlock();
                try {
                    Thread.sleep(waitMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.debug("Rate limiter acquire interrupted");
                    return;
                }
                lock.lock();
                refillTokens();
            }

            availableTokens -= 1.0;
            log.trace("Token acquired, {} tokens remaining", availableTokens);

        } finally {
            lock.unlock();
        }
    }

    /**
     * Attempts to acquire a token within the specified timeout.
     *
     * @param timeout the maximum time to wait for a token
     * @return true if a token was acquired, false if timeout exceeded
     */
    public boolean tryAcquire(final Duration timeout) {
        final long deadline = System.currentTimeMillis() + timeout.toMillis();

        lock.lock();
        try {
            refillTokens();

            while (availableTokens < 1.0) {
                final long now = System.currentTimeMillis();
                if (now >= deadline) {
                    log.debug("Rate limiter tryAcquire timed out");
                    return false;
                }

                final long waitMillis = Math.min(calculateWaitTime(), deadline - now);
                lock.unlock();
                try {
                    Thread.sleep(waitMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.debug("Rate limiter tryAcquire interrupted");
                    return false;
                }
                lock.lock();
                refillTokens();
            }

            availableTokens -= 1.0;
            log.trace("Token acquired via tryAcquire, {} tokens remaining", availableTokens);
            return true;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the current number of available tokens (for testing/monitoring).
     *
     * @return the number of available tokens
     */
    public double getAvailableTokens() {
        lock.lock();
        try {
            refillTokens();
            return availableTokens;
        } finally {
            lock.unlock();
        }
    }

    private void refillTokens() {
        final long now = System.currentTimeMillis();
        final long elapsedMillis = now - lastRefillTimestamp;

        if (elapsedMillis > 0) {
            final double newTokens = elapsedMillis * tokensPerMillis;
            availableTokens = Math.min(burstCapacity, availableTokens + newTokens);
            lastRefillTimestamp = now;
        }
    }

    private long calculateWaitTime() {
        final double tokensNeeded = 1.0 - availableTokens;
        return Math.max(1, (long) Math.ceil(tokensNeeded / tokensPerMillis));
    }
}
