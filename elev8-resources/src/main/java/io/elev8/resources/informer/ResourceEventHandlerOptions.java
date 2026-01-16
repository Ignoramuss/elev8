package io.elev8.resources.informer;

import java.time.Duration;

/**
 * Configuration options for event handler registration.
 * Allows per-handler customization such as resync period.
 */
public final class ResourceEventHandlerOptions {

    private final Duration resyncPeriod;

    private ResourceEventHandlerOptions(final Duration resyncPeriod) {
        this.resyncPeriod = resyncPeriod != null ? resyncPeriod : Duration.ZERO;
    }

    /**
     * Returns the resync period for this handler.
     * A period of zero means no periodic resync.
     *
     * @return the resync period
     */
    public Duration getResyncPeriod() {
        return resyncPeriod;
    }

    /**
     * Returns whether resync is enabled for this handler.
     *
     * @return true if resync period is greater than zero
     */
    public boolean hasResync() {
        return !resyncPeriod.isZero() && !resyncPeriod.isNegative();
    }

    /**
     * Creates default options with no resync.
     *
     * @return default options
     */
    public static ResourceEventHandlerOptions defaults() {
        return new ResourceEventHandlerOptions(Duration.ZERO);
    }

    /**
     * Creates options with the specified resync period.
     *
     * @param period the resync period (null or zero to disable)
     * @return options with the specified resync period
     */
    public static ResourceEventHandlerOptions withResyncPeriod(final Duration period) {
        return new ResourceEventHandlerOptions(period);
    }

    @Override
    public String toString() {
        return "ResourceEventHandlerOptions{resyncPeriod=" + resyncPeriod + "}";
    }
}
