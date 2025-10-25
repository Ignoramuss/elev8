package io.elev8.core.http;

import lombok.Getter;

/**
 * Exception thrown when an HTTP request fails.
 */
@Getter
public class HttpException extends Exception {

    private final Integer statusCode;

    public HttpException(final String message) {
        super(message);
        this.statusCode = null;
    }

    public HttpException(final String message, final int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpException(final String message, final Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    public HttpException(final String message, final int statusCode, final Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public boolean hasStatusCode() {
        return statusCode != null;
    }
}
