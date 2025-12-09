package io.elev8.resources;

import io.elev8.core.watch.ResourceChangeEvent;
import io.elev8.core.watch.ResourceChangeStream;
import io.elev8.core.watch.WatchEvent;
import io.elev8.core.watch.WatchStreamException;
import io.elev8.core.watch.Watcher;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Internal adapter that bridges the callback-based Watcher interface to the
 * queue-based ResourceChangeStream.
 *
 * <p>This adapter:</p>
 * <ul>
 *   <li>Receives WatchEvent callbacks from the Kubernetes watch API</li>
 *   <li>Converts them to ResourceChangeEvent instances</li>
 *   <li>Optionally tracks previous state for change detection</li>
 *   <li>Enqueues events to the ResourceChangeStream</li>
 * </ul>
 *
 * @param <T> the type of Kubernetes resource being watched
 */
class WatchStreamAdapter<T extends KubernetesResource> implements Watcher<T> {

    private final ResourceChangeStream<T> stream;
    private final boolean trackPreviousState;
    private final Map<String, T> stateCache;

    /**
     * Creates a new WatchStreamAdapter.
     *
     * @param stream the stream to enqueue events to
     * @param trackPreviousState whether to maintain a cache of previous resource states
     */
    WatchStreamAdapter(final ResourceChangeStream<T> stream, final boolean trackPreviousState) {
        this.stream = stream;
        this.trackPreviousState = trackPreviousState;
        this.stateCache = trackPreviousState ? new ConcurrentHashMap<>() : null;
    }

    @Override
    public void onEvent(final WatchEvent<T> event) {
        if (event == null) {
            return;
        }

        if (event.isError()) {
            stream.setError(new WatchStreamException("Watch error event received"));
            return;
        }

        if (event.isBookmark()) {
            stream.enqueue(ResourceChangeEvent.from(event, null));
            return;
        }

        T previousState = null;
        final String key = getCacheKey(event.getObject());

        if (trackPreviousState && key != null) {
            if (event.isAdded()) {
                stateCache.put(key, event.getObject());
            } else if (event.isModified()) {
                previousState = stateCache.put(key, event.getObject());
            } else if (event.isDeleted()) {
                previousState = stateCache.remove(key);
            }
        }

        stream.enqueue(ResourceChangeEvent.from(event, previousState));
    }

    @Override
    public void onError(final Exception exception) {
        stream.setError(exception);
    }

    @Override
    public void onClose() {
        stream.signalClose();
        if (stateCache != null) {
            stateCache.clear();
        }
    }

    @Override
    public void close() {
        stream.close();
        if (stateCache != null) {
            stateCache.clear();
        }
    }

    /**
     * Generates a cache key for a resource based on its namespace and name.
     *
     * @param resource the resource to generate a key for
     * @return the cache key, or null if the resource has no name
     */
    private String getCacheKey(final T resource) {
        if (resource == null) {
            return null;
        }
        final String name = resource.getName();
        if (name == null) {
            return null;
        }
        final String namespace = resource.getNamespace();
        return namespace != null ? namespace + "/" + name : name;
    }
}
