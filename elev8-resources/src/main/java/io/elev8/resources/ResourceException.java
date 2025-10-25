package io.elev8.resources;

public class ResourceException extends Exception {

    private final Integer statusCode;

    public ResourceException(String message) {
        super(message);
        this.statusCode = null;
    }

    public ResourceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ResourceException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    public ResourceException(String message, int statusCode, Throwable cause) {
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
