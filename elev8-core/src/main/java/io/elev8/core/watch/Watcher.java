package io.elev8.core.watch;

/**
 * Callback interface for consuming watch events from the Kubernetes API server.
 * Implementations of this interface receive notifications when resources change.
 *
 * @param <T> the type of Kubernetes resource being watched
 */
public interface Watcher<T> extends AutoCloseable {
    /**
     * Called when a watch event is received from the API server.
     *
     * @param event the watch event containing the event type and resource object
     */
    void onEvent(WatchEvent<T> event);

    /**
     * Called when an error occurs during the watch operation.
     * The watch will be closed after this callback is invoked.
     *
     * @param exception the exception that caused the error
     */
    default void onError(Exception exception) {
        // Default implementation does nothing
    }

    /**
     * Called when the watch connection is closed normally or due to an error.
     * This is always called after onError if an error occurred.
     */
    default void onClose() {
        // Default implementation does nothing
    }

    /**
     * Closes the watcher and releases any resources.
     * After this method is called, no more events will be received.
     */
    @Override
    default void close() {
        // Default implementation does nothing
    }
}
