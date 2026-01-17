package io.elev8.resources.workqueue;

/**
 * A work queue with rate limiting for failed items.
 *
 * <p>Extends {@link DelayingWorkQueue} to add rate-limited re-queuing.
 * Items can be re-queued with an automatically computed delay based on
 * how many times they have failed.
 *
 * <p>Typical controller pattern:
 * <pre>{@code
 * while (!queue.isShuttingDown()) {
 *     String key = queue.get();
 *     try {
 *         processItem(key);
 *         queue.forget(key);  // Success - reset retry tracking
 *     } catch (Exception e) {
 *         queue.addRateLimited(key);  // Retry with backoff
 *     } finally {
 *         queue.done(key);
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of items in the queue
 */
public interface RateLimitingWorkQueue<T> extends DelayingWorkQueue<T> {

    /**
     * Adds an item to the queue after a rate-limited delay.
     * The delay is computed based on how many times this item has failed.
     *
     * @param item the item to re-queue
     */
    void addRateLimited(T item);

    /**
     * Stops tracking the item for rate limiting purposes.
     * Call this when processing succeeds to reset the retry count.
     *
     * @param item the item to forget
     */
    void forget(T item);

    /**
     * Returns the number of times this item has been requeued.
     *
     * @param item the item to check
     * @return the number of requeues
     */
    int numRequeues(T item);
}
