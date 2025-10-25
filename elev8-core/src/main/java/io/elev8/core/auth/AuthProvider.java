package io.elev8.core.auth;

/**
 * Interface for Kubernetes authentication providers.
 * Implementations provide authentication tokens for Kubernetes API requests.
 */
public interface AuthProvider {

    /**
     * Get an authentication token for Kubernetes API requests.
     *
     * @return the authentication token (e.g., Bearer token)
     * @throws AuthenticationException if unable to obtain a valid token
     */
    String getToken() throws AuthenticationException;

    /**
     * Check if the current token needs to be refreshed.
     *
     * @return true if the token should be refreshed, false otherwise
     */
    boolean needsRefresh();

    /**
     * Refresh the authentication token.
     *
     * @throws AuthenticationException if unable to refresh the token
     */
    void refresh() throws AuthenticationException;

    /**
     * Get the authentication type (e.g., "Bearer", "Basic").
     *
     * @return the authentication type
     */
    default String getAuthType() {
        return "Bearer";
    }

    /**
     * Get the full authentication header value.
     *
     * @return the authentication header value (e.g., "Bearer <token>")
     * @throws AuthenticationException if unable to obtain authentication
     */
    default String getAuthHeader() throws AuthenticationException {
        return getAuthType() + " " + getToken();
    }

    /**
     * Close and release any resources held by this auth provider.
     * Default implementation does nothing.
     */
    default void close() {
        // Default: no-op
    }
}
