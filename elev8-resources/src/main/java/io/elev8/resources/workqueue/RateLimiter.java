package io.elev8.resources.workqueue;

import java.time.Duration;

/**
 * Strategy interface for computing rate limiting delays.
 * Implementations track failure counts and compute appropriate backoff delays.
 *
 * <p>Rate limiters are used by {@link RateLimitingWorkQueue} to determine
 * how long to wait before re-processing a failed item.
 *
 * @param <T> the type of items being rate limited
 */
public interface RateLimiter<T> {

    /**
     * Returns the delay before the item should be retried.
     * Each call increments the failure count for the item.
     *
     * @param item the item being rate limited
     * @return the delay before retry
     */
    Duration when(T item);

    /**
     * Stops tracking the item and resets its failure count.
     * Should be called when processing succeeds.
     *
     * @param item the item to forget
     */
    void forget(T item);

    /**
     * Returns the number of times this item has been requeued.
     *
     * @param item the item to check
     * @return the number of requeues (failures)
     */
    int numRequeues(T item);
}
