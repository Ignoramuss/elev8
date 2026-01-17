package io.elev8.resources.workqueue;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default implementation of {@link WorkQueue} with three-set deduplication.
 *
 * <p>Uses three data structures for deduplication:
 * <ul>
 *   <li>{@code queue} - items ready for processing (FIFO order)</li>
 *   <li>{@code dirty} - all items pending or being processed (for deduplication)</li>
 *   <li>{@code processing} - items currently being processed</li>
 * </ul>
 *
 * <p>State machine:
 * <ol>
 *   <li>{@code add(item)}: If in dirty, skip. Else add to dirty. If not in processing, add to queue.</li>
 *   <li>{@code get()}: Take from queue, add to processing.</li>
 *   <li>{@code done(item)}: Remove from processing. If still in dirty, re-queue.</li>
 * </ol>
 *
 * @param <T> the type of items in the queue
 */
@Slf4j
public class DefaultWorkQueue<T> implements WorkQueue<T> {

    private final LinkedBlockingDeque<T> queue;
    private final Set<T> dirty;
    private final Set<T> processing;
    private final AtomicBoolean shuttingDown;

    private final Object lock = new Object();

    public DefaultWorkQueue() {
        this.queue = new LinkedBlockingDeque<>();
        this.dirty = ConcurrentHashMap.newKeySet();
        this.processing = ConcurrentHashMap.newKeySet();
        this.shuttingDown = new AtomicBoolean(false);
    }

    @Override
    public void add(final T item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        if (shuttingDown.get()) {
            log.debug("Queue is shutting down, rejecting add for: {}", item);
            return;
        }

        synchronized (lock) {
            if (dirty.contains(item)) {
                log.trace("Item already pending, skipping add: {}", item);
                return;
            }

            dirty.add(item);

            if (!processing.contains(item)) {
                queue.add(item);
                log.trace("Added item to queue: {}", item);
            } else {
                log.trace("Item in processing, marked dirty for re-queue: {}", item);
            }
        }
    }

    @Override
    public T get() throws InterruptedException {
        while (!shuttingDown.get()) {
            final T item = queue.poll(100, TimeUnit.MILLISECONDS);
            if (item != null) {
                synchronized (lock) {
                    dirty.remove(item);
                    processing.add(item);
                }
                log.trace("Got item from queue: {}", item);
                return item;
            }
        }
        throw new InterruptedException("Queue is shutting down");
    }

    @Override
    public T poll(final Duration timeout) throws InterruptedException {
        final long deadline = System.nanoTime() + timeout.toNanos();

        while (!shuttingDown.get()) {
            final long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                return null;
            }

            final long waitMs = Math.min(remaining / 1_000_000, 100);
            final T item = queue.poll(waitMs, TimeUnit.MILLISECONDS);

            if (item != null) {
                synchronized (lock) {
                    dirty.remove(item);
                    processing.add(item);
                }
                log.trace("Polled item from queue: {}", item);
                return item;
            }
        }
        return null;
    }

    @Override
    public void done(final T item) {
        if (item == null) {
            return;
        }

        synchronized (lock) {
            processing.remove(item);

            if (dirty.contains(item)) {
                queue.add(item);
                log.trace("Item re-added during processing, re-queued: {}", item);
            } else {
                log.trace("Item completed: {}", item);
            }
        }
    }

    @Override
    public int length() {
        return queue.size();
    }

    @Override
    public void shutdown() {
        if (shuttingDown.compareAndSet(false, true)) {
            log.debug("Work queue shutting down");
        }
    }

    @Override
    public boolean isShuttingDown() {
        return shuttingDown.get();
    }
}
