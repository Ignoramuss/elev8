package io.elev8.core.auth;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationException extends Exception {

    public AuthenticationException(final String message) {
        super(message);
    }

    public AuthenticationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(final Throwable cause) {
        super(cause);
    }
}
