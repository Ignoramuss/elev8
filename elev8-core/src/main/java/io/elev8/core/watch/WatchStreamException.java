package io.elev8.core.watch;

/**
 * Exception thrown when an error occurs during watch stream operations.
 * This is a runtime exception to allow clean integration with Iterator and Stream APIs.
 */
public class WatchStreamException extends RuntimeException {

    /**
     * Creates a new WatchStreamException with the specified message.
     *
     * @param message the detail message
     */
    public WatchStreamException(final String message) {
        super(message);
    }

    /**
     * Creates a new WatchStreamException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public WatchStreamException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
