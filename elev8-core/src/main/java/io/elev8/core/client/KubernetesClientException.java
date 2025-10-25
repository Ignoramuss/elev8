package io.elev8.core.client;

/**
 * Exception thrown when a Kubernetes API request fails.
 */
public class KubernetesClientException extends Exception {

    private final Integer statusCode;

    public KubernetesClientException(String message) {
        super(message);
        this.statusCode = null;
    }

    public KubernetesClientException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public KubernetesClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    public KubernetesClientException(String message, int statusCode, Throwable cause) {
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
