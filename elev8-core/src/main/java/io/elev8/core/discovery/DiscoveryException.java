package io.elev8.core.discovery;

import lombok.Getter;

@Getter
public class DiscoveryException extends Exception {

    private final Integer statusCode;

    public DiscoveryException(final String message) {
        super(message);
        this.statusCode = null;
    }

    public DiscoveryException(final String message, final int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public DiscoveryException(final String message, final Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    public DiscoveryException(final String message, final int statusCode, final Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public boolean hasStatusCode() {
        return statusCode != null;
    }
}
