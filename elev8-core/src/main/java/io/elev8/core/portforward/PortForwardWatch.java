package io.elev8.core.portforward;

/**
 * Callback interface for consuming port forward streams from pod containers.
 * Implementations receive notifications for data, errors, and connection lifecycle.
 *
 * @see PortForwardOptions
 */
public interface PortForwardWatch extends AutoCloseable {
    /**
     * Called when data is received from a forwarded port.
     *
     * @param port the port number
     * @param data the binary data received from the pod
     */
    default void onData(int port, byte[] data) {
        // Default implementation does nothing
    }

    /**
     * Called when an error is received for a specific port.
     *
     * @param port the port number
     * @param error the error message
     */
    default void onError(int port, String error) {
        // Default implementation does nothing
    }

    /**
     * Called when the port forward session is closed.
     */
    default void onClose() {
        // Default implementation does nothing
    }

    /**
     * Called when an exception occurs during the port forward operation.
     *
     * @param exception the exception that occurred
     */
    default void onFailure(Exception exception) {
        // Default implementation does nothing
    }

    /**
     * Write binary data to a forwarded port.
     * The data will be sent to the corresponding port in the pod.
     *
     * @param port the port number
     * @param data the binary data to send
     */
    default void writeData(int port, byte[] data) {
        // Default implementation does nothing
    }

    /**
     * Closes the port forward session and releases resources.
     */
    @Override
    default void close() {
        // Default implementation does nothing
    }
}
