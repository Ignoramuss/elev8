package io.elev8.resources.workqueue;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Default implementation of {@link RateLimitingWorkQueue}.
 *
 * <p>Wraps a {@link DelayingWorkQueue} and uses a {@link RateLimiter} to
 * compute delays for rate-limited additions.
 *
 * @param <T> the type of items in the queue
 */
@Slf4j
public class DefaultRateLimitingWorkQueue<T> implements RateLimitingWorkQueue<T> {

    private final DelayingWorkQueue<T> delegate;
    private final RateLimiter<T> rateLimiter;

    /**
     * Creates a new rate limiting work queue with default settings.
     * Uses {@link DefaultControllerRateLimiter} for rate limiting.
     */
    public DefaultRateLimitingWorkQueue() {
        this(new DefaultDelayingWorkQueue<>(), DefaultControllerRateLimiter.create());
    }

    /**
     * Creates a new rate limiting work queue with the specified rate limiter.
     *
     * @param rateLimiter the rate limiter to use
     */
    public DefaultRateLimitingWorkQueue(final RateLimiter<T> rateLimiter) {
        this(new DefaultDelayingWorkQueue<>(), rateLimiter);
    }

    /**
     * Creates a new rate limiting work queue with the specified delegate and rate limiter.
     *
     * @param delegate the underlying delaying work queue
     * @param rateLimiter the rate limiter to use
     */
    public DefaultRateLimitingWorkQueue(final DelayingWorkQueue<T> delegate,
                                         final RateLimiter<T> rateLimiter) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate cannot be null");
        }
        if (rateLimiter == null) {
            throw new IllegalArgumentException("rateLimiter cannot be null");
        }
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void addRateLimited(final T item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        final Duration delay = rateLimiter.when(item);
        log.trace("Rate limiting item {} with delay {}", item, delay);
        delegate.addAfter(item, delay);
    }

    @Override
    public void forget(final T item) {
        rateLimiter.forget(item);
    }

    @Override
    public int numRequeues(final T item) {
        return rateLimiter.numRequeues(item);
    }

    @Override
    public void add(final T item) {
        delegate.add(item);
    }

    @Override
    public void addAfter(final T item, final Duration delay) {
        delegate.addAfter(item, delay);
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
        delegate.shutdown();
    }

    @Override
    public boolean isShuttingDown() {
        return delegate.isShuttingDown();
    }
}
