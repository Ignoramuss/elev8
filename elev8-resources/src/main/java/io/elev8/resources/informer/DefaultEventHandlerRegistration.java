package io.elev8.resources.informer;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default implementation of EventHandlerRegistration.
 * Tracks the handler, its options, and whether it's still active.
 *
 * @param <T> the resource type
 */
public final class DefaultEventHandlerRegistration<T> implements EventHandlerRegistration<T> {

    private final ResourceEventHandler<T> handler;
    private final Duration resyncPeriod;
    private final AtomicBoolean active;

    public DefaultEventHandlerRegistration(
            final ResourceEventHandler<T> handler,
            final ResourceEventHandlerOptions options) {
        this.handler = handler;
        this.resyncPeriod = options != null ? options.getResyncPeriod() : Duration.ZERO;
        this.active = new AtomicBoolean(true);
    }

    @Override
    public ResourceEventHandler<T> getHandler() {
        return handler;
    }

    @Override
    public Duration getResyncPeriod() {
        return resyncPeriod;
    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    /**
     * Marks this registration as inactive.
     * Called when the handler is removed from the informer.
     */
    void deactivate() {
        active.set(false);
    }

    /**
     * Returns whether resync is configured for this handler.
     *
     * @return true if resync period is greater than zero
     */
    boolean hasResync() {
        return !resyncPeriod.isZero() && !resyncPeriod.isNegative();
    }
}
