package io.elev8.resources.workqueue;

import java.time.Duration;

/**
 * A work queue that supports delayed addition of items.
 * Items can be scheduled to appear in the queue after a specified delay.
 *
 * <p>This is useful for implementing retry logic where you want to
 * re-process an item after a cooldown period.
 *
 * @param <T> the type of items in the queue
 */
public interface DelayingWorkQueue<T> extends WorkQueue<T> {

    /**
     * Adds an item to the queue after the specified delay.
     * If the item is already scheduled with a pending delay,
     * the new delay replaces the old one.
     *
     * @param item the item to add
     * @param delay the delay before the item appears in the queue
     * @throws IllegalStateException if the queue is shutting down
     */
    void addAfter(T item, Duration delay);
}
