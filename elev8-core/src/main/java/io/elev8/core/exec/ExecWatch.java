package io.elev8.core.exec;

/**
 * Callback interface for consuming exec streams from pod containers.
 * Implementations receive notifications for stdout, stderr, errors, and connection lifecycle.
 *
 * @see ExecOptions
 */
public interface ExecWatch extends AutoCloseable {
    /**
     * Called when data is received on the STDOUT channel.
     *
     * @param data the output data from the container
     */
    default void onStdout(String data) {
        // Default implementation does nothing
    }

    /**
     * Called when data is received on the STDERR channel.
     *
     * @param data the error output from the container
     */
    default void onStderr(String data) {
        // Default implementation does nothing
    }

    /**
     * Called when an error is received on the ERROR channel.
     *
     * @param error the error message
     */
    default void onError(String error) {
        // Default implementation does nothing
    }

    /**
     * Called when the exec session is closed.
     *
     * @param exitCode the command exit code (0 = success, non-zero = error)
     */
    default void onClose(int exitCode) {
        // Default implementation does nothing
    }

    /**
     * Called when an exception occurs during the exec operation.
     *
     * @param exception the exception that occurred
     */
    default void onFailure(Exception exception) {
        // Default implementation does nothing
    }

    /**
     * Write data to the STDIN channel of the exec session.
     * Only works if ExecOptions.stdin was set to true.
     *
     * @param data the data to write to STDIN
     */
    default void writeStdin(String data) {
        // Default implementation does nothing
    }

    /**
     * Closes the exec session and releases resources.
     */
    @Override
    default void close() {
        // Default implementation does nothing
    }
}
