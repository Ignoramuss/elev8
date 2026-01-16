package io.elev8.resources.informer;

import io.elev8.resources.KubernetesResource;

/**
 * A SharedInformer provides a shared cache of resources that multiple consumers can use.
 * Unlike regular Informer, a SharedInformer is designed to be shared across multiple
 * event handlers, reducing API server load by maintaining a single watch per resource type.
 *
 * <p>Multiple controllers can register handlers with the same SharedInformer, and all will
 * receive events from a single underlying watch connection. This is more efficient than
 * each controller maintaining its own Informer.</p>
 *
 * @param <T> the type of Kubernetes resource
 */
public interface SharedInformer<T extends KubernetesResource> {

    /**
     * Returns the read-only store (cache) of resources.
     * The store is shared across all handlers registered with this informer.
     *
     * @return the resource store
     */
    Store<T> getStore();

    /**
     * Returns whether the informer has completed its initial sync.
     * The cache is fully populated after initial sync completes.
     *
     * @return true if initial list has completed and cache is populated
     */
    boolean hasSynced();

    /**
     * Returns whether the informer is currently running.
     *
     * @return true if the informer is running
     */
    boolean isRunning();

    /**
     * Adds an event handler that will be notified of resource changes.
     * If the informer has already synced, the handler will receive onAdd
     * events for all existing resources in the cache.
     *
     * @param handler the event handler to add
     * @return a registration handle that can be used to remove the handler
     */
    EventHandlerRegistration<T> addEventHandler(ResourceEventHandler<T> handler);

    /**
     * Adds an event handler with configuration options.
     *
     * @param handler the event handler to add
     * @param options handler configuration including resync period
     * @return a registration handle that can be used to remove the handler
     */
    EventHandlerRegistration<T> addEventHandler(
            ResourceEventHandler<T> handler,
            ResourceEventHandlerOptions options);

    /**
     * Removes a previously registered event handler.
     *
     * @param registration the registration handle returned from addEventHandler
     */
    void removeEventHandler(EventHandlerRegistration<T> registration);

    /**
     * Returns the resource version from the last successful sync.
     *
     * @return the last sync resource version, or empty string if not synced
     */
    String getLastSyncResourceVersion();

    /**
     * Starts the informer. Called internally by SharedInformerFactory.
     * Should not be called directly when using a factory.
     */
    void run();

    /**
     * Stops the informer gracefully.
     */
    void shutdown();
}
