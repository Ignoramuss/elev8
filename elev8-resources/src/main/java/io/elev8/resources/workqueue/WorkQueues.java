package io.elev8.resources.workqueue;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Factory methods for creating work queues.
 *
 * <p>Provides convenient methods for creating pre-configured work queues
 * for common use cases.
 */
public final class WorkQueues {

    private WorkQueues() {
    }

    /**
     * Creates a basic work queue with FIFO ordering and deduplication.
     *
     * @param <T> the type of items in the queue
     * @return a new work queue
     */
    public static <T> WorkQueue<T> newDefaultQueue() {
        return new DefaultWorkQueue<>();
    }

    /**
     * Creates a work queue with delayed addition support.
     *
     * @param <T> the type of items in the queue
     * @return a new delaying work queue
     */
    public static <T> DelayingWorkQueue<T> newDelayingQueue() {
        return new DefaultDelayingWorkQueue<>();
    }

    /**
     * Creates a work queue with delayed addition using the specified scheduler.
     *
     * @param <T> the type of items in the queue
     * @param scheduler the scheduler for delayed tasks
     * @return a new delaying work queue
     */
    public static <T> DelayingWorkQueue<T> newDelayingQueue(final ScheduledExecutorService scheduler) {
        return new DefaultDelayingWorkQueue<>(new DefaultWorkQueue<>(), scheduler, false);
    }

    /**
     * Creates a rate-limited work queue with default settings.
     * Uses exponential backoff (5ms base, 1000s max) combined with
     * token bucket rate limiting (10/s rate, 100 burst).
     *
     * @param <T> the type of items in the queue
     * @return a new rate limiting work queue
     */
    public static <T> RateLimitingWorkQueue<T> newDefaultRateLimitingQueue() {
        return new DefaultRateLimitingWorkQueue<>();
    }

    /**
     * Creates a rate-limited work queue with the specified rate limiter.
     *
     * @param <T> the type of items in the queue
     * @param rateLimiter the rate limiter to use
     * @return a new rate limiting work queue
     */
    public static <T> RateLimitingWorkQueue<T> newRateLimitingQueue(final RateLimiter<T> rateLimiter) {
        return new DefaultRateLimitingWorkQueue<>(rateLimiter);
    }

    /**
     * Creates a rate-limited work queue with exponential backoff.
     *
     * @param <T> the type of items in the queue
     * @param baseDelay the initial delay for the first failure
     * @param maxDelay the maximum delay cap
     * @return a new rate limiting work queue
     */
    public static <T> RateLimitingWorkQueue<T> newExponentialBackoffQueue(final Duration baseDelay,
                                                                           final Duration maxDelay) {
        return new DefaultRateLimitingWorkQueue<>(new ExponentialBackoffRateLimiter<>(baseDelay, maxDelay));
    }

    /**
     * Creates an exponential backoff rate limiter.
     *
     * @param <T> the type of items being rate limited
     * @param baseDelay the initial delay for the first failure
     * @param maxDelay the maximum delay cap
     * @return a new rate limiter
     */
    public static <T> RateLimiter<T> newExponentialBackoffRateLimiter(final Duration baseDelay,
                                                                       final Duration maxDelay) {
        return new ExponentialBackoffRateLimiter<>(baseDelay, maxDelay);
    }

    /**
     * Creates a token bucket rate limiter.
     *
     * @param <T> the type of items being rate limited
     * @param tokensPerSecond the rate at which tokens are generated
     * @param burstCapacity the maximum number of tokens that can accumulate
     * @return a new rate limiter
     */
    public static <T> RateLimiter<T> newBucketRateLimiter(final double tokensPerSecond,
                                                          final int burstCapacity) {
        return new BucketRateLimiter<>(tokensPerSecond, burstCapacity);
    }

    /**
     * Creates the default rate limiter for Kubernetes controllers.
     * Combines exponential backoff with token bucket limiting.
     *
     * @param <T> the type of items being rate limited
     * @return a new rate limiter
     */
    public static <T> RateLimiter<T> newDefaultRateLimiter() {
        return DefaultControllerRateLimiter.create();
    }
}
