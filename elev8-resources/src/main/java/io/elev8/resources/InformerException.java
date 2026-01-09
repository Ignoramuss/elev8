package io.elev8.resources;

/**
 * Runtime exception thrown when an error occurs in an Informer operation.
 * This wraps checked exceptions from ResourceManager operations.
 */
public class InformerException extends RuntimeException {

    /**
     * Constructs a new InformerException with the specified message.
     *
     * @param message the detail message
     */
    public InformerException(final String message) {
        super(message);
    }

    /**
     * Constructs a new InformerException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public InformerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
