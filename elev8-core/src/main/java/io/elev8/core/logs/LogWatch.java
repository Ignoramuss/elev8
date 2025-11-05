package io.elev8.core.logs;

/**
 * Callback interface for consuming log streams from pod containers.
 * Implementations of this interface receive notifications as log lines are streamed.
 *
 * @see LogOptions
 */
public interface LogWatch extends AutoCloseable {
    /**
     * Called when a log line is received from the pod container.
     *
     * @param line the log line (may include timestamp if timestamps option was enabled)
     */
    void onLog(String line);

    /**
     * Called when an error occurs during the log streaming operation.
     * The log stream will be closed after this callback is invoked.
     *
     * @param exception the exception that caused the error
     */
    default void onError(Exception exception) {
        // Default implementation does nothing
    }

    /**
     * Called when the log stream is closed normally or due to an error.
     * This is always called after onError if an error occurred.
     */
    default void onClose() {
        // Default implementation does nothing
    }

    /**
     * Closes the log stream and releases any resources.
     * After this method is called, no more log lines will be received.
     */
    @Override
    default void close() {
        // Default implementation does nothing
    }
}
