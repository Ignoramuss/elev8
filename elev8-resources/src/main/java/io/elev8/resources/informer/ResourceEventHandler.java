package io.elev8.resources.informer;

/**
 * Handler for receiving notifications about resource changes from an Informer.
 * Implement this interface to react to add, update, and delete events for cached resources.
 *
 * <p>Event handlers are invoked on the Informer's event processing thread.
 * Implementations should be thread-safe if they modify shared state.</p>
 *
 * @param <T> the type of resource being watched
 */
public interface ResourceEventHandler<T> {

    /**
     * Called when a resource is added to the cache.
     * This includes both newly created resources and resources discovered during initial list.
     *
     * @param resource the added resource
     */
    void onAdd(T resource);

    /**
     * Called when a cached resource is updated.
     *
     * @param oldResource the previous state of the resource
     * @param newResource the new state of the resource
     */
    void onUpdate(T oldResource, T newResource);

    /**
     * Called when a resource is deleted.
     *
     * @param resource the deleted resource (last known state)
     */
    void onDelete(T resource);
}
