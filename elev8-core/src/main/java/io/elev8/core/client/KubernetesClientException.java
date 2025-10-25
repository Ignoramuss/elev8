package io.elev8.core.client;

import lombok.Getter;

/**
 * Exception thrown when a Kubernetes API request fails.
 */
@Getter
public class KubernetesClientException extends Exception {

    private final Integer statusCode;

    public KubernetesClientException(final String message) {
        super(message);
        this.statusCode = null;
    }

    public KubernetesClientException(final String message, final int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public KubernetesClientException(final String message, final Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    public KubernetesClientException(final String message, final int statusCode, final Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public boolean hasStatusCode() {
        return statusCode != null;
    }
}
