package io.elev8.resources;

import lombok.Getter;

@Getter
public class ResourceException extends Exception {

    private final Integer statusCode;

    public ResourceException(final String message) {
        super(message);
        this.statusCode = null;
    }

    public ResourceException(final String message, final int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ResourceException(final String message, final Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    public ResourceException(final String message, final int statusCode, final Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public boolean hasStatusCode() {
        return statusCode != null;
    }
}
