package io.elev8.core.watch;

import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A stream of resource change events that can be consumed as an Iterator or Java Stream.
 * This provides a higher-level abstraction over the callback-based Watcher interface.
 *
 * <p>Usage patterns:</p>
 * <pre>{@code
 * // As an Iterator
 * try (ResourceChangeStream<Pod> stream = manager.stream(namespace, options)) {
 *     for (ResourceChangeEvent<Pod> event : stream) {
 *         process(event);
 *     }
 * }
 *
 * // As a Java Stream
 * try (ResourceChangeStream<Pod> stream = manager.stream(namespace, options)) {
 *     stream.stream()
 *         .filter(e -> e.isCreated() || e.isUpdated())
 *         .forEach(this::process);
 * }
 *
 * // Non-blocking poll
 * ResourceChangeEvent<Pod> event = stream.poll(5, TimeUnit.SECONDS);
 * }</pre>
 *
 * @param <T> the type of Kubernetes resource being streamed
 */
@Slf4j
public class ResourceChangeStream<T> implements Iterator<ResourceChangeEvent<T>>, AutoCloseable, Iterable<ResourceChangeEvent<T>> {

    private static final int DEFAULT_QUEUE_CAPACITY = 1000;
    private static final long POLL_TIMEOUT_MS = 100;

    private final BlockingQueue<ResourceChangeEvent<T>> eventQueue;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicReference<Exception> error = new AtomicReference<>();
    private final Runnable onClose;

    /**
     * Creates a new ResourceChangeStream with the default queue capacity.
     *
     * @param onClose callback to invoke when the stream is closed
     */
    public ResourceChangeStream(final Runnable onClose) {
        this(DEFAULT_QUEUE_CAPACITY, onClose);
    }

    /**
     * Creates a new ResourceChangeStream with a custom queue capacity.
     *
     * @param queueCapacity the maximum number of events to buffer
     * @param onClose callback to invoke when the stream is closed
     */
    public ResourceChangeStream(final int queueCapacity, final Runnable onClose) {
        final int effectiveCapacity = queueCapacity > 0 ? queueCapacity : DEFAULT_QUEUE_CAPACITY;
        if (queueCapacity <= 0) {
            log.warn("Invalid queue capacity {}, using default capacity {}", queueCapacity, DEFAULT_QUEUE_CAPACITY);
        }
        this.eventQueue = new LinkedBlockingQueue<>(effectiveCapacity);
        this.onClose = onClose;
    }

    /**
     * Enqueues an event to the stream.
     * This method is intended for internal use by adapters.
     * If the queue is full, this method will block until space is available.
     *
     * @param event the event to enqueue
     */
    public void enqueue(final ResourceChangeEvent<T> event) {
        if (!closed.get() && event != null) {
            try {
                eventQueue.put(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while enqueuing event, event may be lost");
            }
        }
    }

    /**
     * Signals that an error occurred in the watch stream.
     * This method is intended for internal use by adapters.
     * After this is called, subsequent operations will throw WatchStreamException.
     *
     * @param exception the error that occurred
     */
    public void setError(final Exception exception) {
        this.error.set(exception);
        closed.set(true);
    }

    /**
     * Signals that the watch stream has closed normally.
     * This method is intended for internal use by adapters.
     */
    public void signalClose() {
        closed.set(true);
    }

    @Override
    public boolean hasNext() {
        checkError();
        return !closed.get() || !eventQueue.isEmpty();
    }

    @Override
    public ResourceChangeEvent<T> next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Stream is closed and no more events are available");
        }
        try {
            ResourceChangeEvent<T> event = eventQueue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            while (event == null && hasNext()) {
                event = eventQueue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            }
            if (event == null) {
                throw new NoSuchElementException("Stream is closed and no more events are available");
            }
            return event;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WatchStreamException("Interrupted while waiting for event", e);
        }
    }

    /**
     * Polls for the next event with a timeout.
     * Returns null if no event is available within the timeout period.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the next event, or null if the timeout expires
     * @throws InterruptedException if interrupted while waiting
     * @throws WatchStreamException if an error occurred in the watch stream
     */
    public ResourceChangeEvent<T> poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        checkError();
        return eventQueue.poll(timeout, unit);
    }

    /**
     * Returns this stream as an Iterable for use in enhanced for-loops.
     *
     * @return this stream as an Iterable
     */
    @Override
    public Iterator<ResourceChangeEvent<T>> iterator() {
        return this;
    }

    /**
     * Converts this stream to a Java Stream for functional-style processing.
     * The returned stream should be used in a single thread.
     * Closing the returned Stream will also close this ResourceChangeStream.
     *
     * @return a Stream of resource change events
     */
    public Stream<ResourceChangeEvent<T>> stream() {
        final Spliterator<ResourceChangeEvent<T>> spliterator = Spliterators.spliteratorUnknownSize(
                this,
                Spliterator.ORDERED | Spliterator.NONNULL
        );
        return StreamSupport.stream(spliterator, false)
                .onClose(this::close);
    }

    /**
     * Converts this stream to a Java Stream filtered by change types.
     *
     * @param types the change types to include
     * @return a filtered Stream of resource change events
     */
    public Stream<ResourceChangeEvent<T>> stream(final ResourceChangeType... types) {
        if (types == null || types.length == 0) {
            return stream();
        }
        final Set<ResourceChangeType> typeSet = Set.of(types);
        return stream().filter(e -> typeSet.contains(e.getType()));
    }

    /**
     * Converts this stream to a Java Stream with a custom filter.
     *
     * @param filter the predicate to filter events
     * @return a filtered Stream of resource change events
     */
    public Stream<ResourceChangeEvent<T>> stream(final Predicate<ResourceChangeEvent<T>> filter) {
        if (filter == null) {
            return stream();
        }
        return stream().filter(filter);
    }

    /**
     * Checks if this stream has been closed.
     *
     * @return true if the stream is closed
     */
    public boolean isClosed() {
        return closed.get();
    }

    /**
     * Gets the error that caused this stream to close, if any.
     *
     * @return the error, or null if no error occurred
     */
    public Exception getError() {
        return error.get();
    }

    /**
     * Returns the number of events currently buffered in the queue.
     *
     * @return the number of buffered events
     */
    public int getQueueSize() {
        return eventQueue.size();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            if (onClose != null) {
                try {
                    onClose.run();
                } catch (Exception e) {
                    log.warn("Error during stream close callback", e);
                }
            }
        }
    }

    private void checkError() {
        final Exception e = error.get();
        if (e != null) {
            throw new WatchStreamException("Watch stream error", e);
        }
    }
}
