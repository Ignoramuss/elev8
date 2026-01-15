package io.elev8.resources.informer;

import java.time.Duration;

/**
 * Registration handle for an event handler added to a SharedInformer.
 * Used to track and remove the handler later.
 *
 * @param <T> the resource type
 */
public interface EventHandlerRegistration<T> {

    /**
     * Returns the registered event handler.
     *
     * @return the event handler
     */
    ResourceEventHandler<T> getHandler();

    /**
     * Returns the resync period configured for this handler.
     *
     * @return the resync period, or Duration.ZERO if no resync
     */
    Duration getResyncPeriod();

    /**
     * Returns whether this registration is still active.
     * A registration becomes inactive after being removed.
     *
     * @return true if the handler is still registered
     */
    boolean isActive();
}
