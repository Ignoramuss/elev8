package io.elev8.codegen;

/**
 * Exception thrown when CRD parsing fails.
 */
public class CrdParseException extends Exception {

    public CrdParseException(final String message) {
        super(message);
    }

    public CrdParseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
