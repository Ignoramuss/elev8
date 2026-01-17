package io.elev8.resources.workqueue;

import java.time.Duration;

/**
 * A work queue that provides deduplication for pending items.
 * Items added while already pending will not create duplicates.
 * Items are processed in FIFO order.
 *
 * <p>Typical usage pattern:
 * <pre>{@code
 * WorkQueue<String> queue = WorkQueues.newDefaultQueue();
 *
 * // Producer
 * queue.add("key1");
 *
 * // Consumer
 * while (!queue.isShuttingDown()) {
 *     String key = queue.get();
 *     try {
 *         processItem(key);
 *     } finally {
 *         queue.done(key);
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of items in the queue
 */
public interface WorkQueue<T> extends AutoCloseable {

    /**
     * Adds an item to the queue. If the item is already pending (in the queue
     * or being processed), this call is a no-op. This provides deduplication.
     *
     * @param item the item to add
     * @throws IllegalStateException if the queue is shutting down
     */
    void add(T item);

    /**
     * Blocks until an item is available and returns it.
     * The caller must call {@link #done(Object)} when processing is complete.
     *
     * @return the next item to process
     * @throws InterruptedException if interrupted while waiting
     */
    T get() throws InterruptedException;

    /**
     * Waits up to the specified timeout for an item to become available.
     *
     * @param timeout the maximum time to wait
     * @return the next item to process, or null if timeout elapses
     * @throws InterruptedException if interrupted while waiting
     */
    T poll(Duration timeout) throws InterruptedException;

    /**
     * Marks an item as done processing. This must be called after
     * processing an item obtained via {@link #get()} or {@link #poll(Duration)}.
     * If the item was re-added while being processed, it will be re-queued.
     *
     * @param item the item that has finished processing
     */
    void done(T item);

    /**
     * Returns the current number of items in the queue.
     * Does not include items currently being processed.
     *
     * @return the number of pending items
     */
    int length();

    /**
     * Initiates a graceful shutdown. Subsequent add calls will be rejected.
     * Workers blocked on {@link #get()} will be interrupted.
     */
    void shutdown();

    /**
     * Returns true if {@link #shutdown()} has been called.
     *
     * @return true if shutting down
     */
    boolean isShuttingDown();

    /**
     * Closes the queue and releases any resources.
     * Equivalent to calling {@link #shutdown()}.
     */
    @Override
    default void close() {
        shutdown();
    }
}
