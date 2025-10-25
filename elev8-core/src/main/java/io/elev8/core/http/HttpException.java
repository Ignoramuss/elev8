package io.elev8.core.http;

/**
 * Exception thrown when an HTTP request fails.
 */
public class HttpException extends Exception {

    private final Integer statusCode;

    public HttpException(String message) {
        super(message);
        this.statusCode = null;
    }

    public HttpException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    public HttpException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public boolean hasStatusCode() {
        return statusCode != null;
    }
}
