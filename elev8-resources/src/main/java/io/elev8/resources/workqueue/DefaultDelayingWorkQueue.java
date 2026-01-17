package io.elev8.resources.workqueue;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default implementation of {@link DelayingWorkQueue} using a {@link ScheduledExecutorService}.
 *
 * <p>Wraps a delegate {@link WorkQueue} and adds delayed scheduling capability.
 * When an item is added with a delay, a scheduled task is created to add it
 * to the underlying queue when the delay expires.
 *
 * <p>If the same item is added again before the previous delay expires,
 * the old scheduled task is cancelled and replaced with the new one.
 *
 * @param <T> the type of items in the queue
 */
@Slf4j
public class DefaultDelayingWorkQueue<T> implements DelayingWorkQueue<T> {

    private final WorkQueue<T> delegate;
    private final ScheduledExecutorService scheduler;
    private final Map<T, ScheduledFuture<?>> pendingDelays;
    private final AtomicBoolean shuttingDown;
    private final boolean ownsScheduler;

    /**
     * Creates a new delaying work queue with a default scheduler.
     */
    public DefaultDelayingWorkQueue() {
        this(new DefaultWorkQueue<>(), createDefaultScheduler(), true);
    }

    /**
     * Creates a new delaying work queue wrapping the given delegate.
     *
     * @param delegate the underlying work queue
     */
    public DefaultDelayingWorkQueue(final WorkQueue<T> delegate) {
        this(delegate, createDefaultScheduler(), true);
    }

    /**
     * Creates a new delaying work queue with the given delegate and scheduler.
     *
     * @param delegate the underlying work queue
     * @param scheduler the scheduler for delayed tasks
     * @param ownsScheduler if true, the scheduler will be shut down when this queue shuts down
     */
    public DefaultDelayingWorkQueue(final WorkQueue<T> delegate,
                                    final ScheduledExecutorService scheduler,
                                    final boolean ownsScheduler) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate cannot be null");
        }
        if (scheduler == null) {
            throw new IllegalArgumentException("scheduler cannot be null");
        }
        this.delegate = delegate;
        this.scheduler = scheduler;
        this.pendingDelays = new ConcurrentHashMap<>();
        this.shuttingDown = new AtomicBoolean(false);
        this.ownsScheduler = ownsScheduler;
    }

    private static ScheduledExecutorService createDefaultScheduler() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "delaying-workqueue-scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void add(final T item) {
        if (shuttingDown.get()) {
            log.debug("Queue is shutting down, rejecting add for: {}", item);
            return;
        }
        cancelPendingDelay(item);
        delegate.add(item);
    }

    @Override
    public void addAfter(final T item, final Duration delay) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        if (delay == null) {
            throw new IllegalArgumentException("delay cannot be null");
        }
        if (shuttingDown.get()) {
            log.debug("Queue is shutting down, rejecting addAfter for: {}", item);
            return;
        }

        if (delay.isNegative() || delay.isZero()) {
            add(item);
            return;
        }

        cancelPendingDelay(item);

        final ScheduledFuture<?> future = scheduler.schedule(() -> {
            pendingDelays.remove(item);
            if (!shuttingDown.get()) {
                delegate.add(item);
                log.trace("Delayed item added to queue after delay: {}", item);
            }
        }, delay.toMillis(), TimeUnit.MILLISECONDS);

        pendingDelays.put(item, future);
        log.trace("Scheduled item for delayed add: {} in {}", item, delay);
    }

    private void cancelPendingDelay(final T item) {
        final ScheduledFuture<?> existing = pendingDelays.remove(item);
        if (existing != null && !existing.isDone()) {
            existing.cancel(false);
            log.trace("Cancelled pending delay for: {}", item);
        }
    }

    @Override
    public T get() throws InterruptedException {
        return delegate.get();
    }

    @Override
    public T poll(final Duration timeout) throws InterruptedException {
        return delegate.poll(timeout);
    }

    @Override
    public void done(final T item) {
        delegate.done(item);
    }

    @Override
    public int length() {
        return delegate.length();
    }

    @Override
    public void shutdown() {
        if (shuttingDown.compareAndSet(false, true)) {
            log.debug("Delaying work queue shutting down");

            for (final ScheduledFuture<?> future : pendingDelays.values()) {
                future.cancel(false);
            }
            pendingDelays.clear();

            if (ownsScheduler) {
                scheduler.shutdown();
            }

            delegate.shutdown();
        }
    }

    @Override
    public boolean isShuttingDown() {
        return shuttingDown.get() || delegate.isShuttingDown();
    }

    /**
     * Returns the number of items with pending delays.
     *
     * @return the number of pending delayed items
     */
    public int pendingDelayCount() {
        return pendingDelays.size();
    }
}
