package io.elev8.core.client;

import io.elev8.core.auth.AuthenticationException;
import io.elev8.core.http.HttpClient;
import io.elev8.core.http.HttpException;
import io.elev8.core.http.HttpResponse;
import io.elev8.core.http.OkHttpClientImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Core Kubernetes client for making API requests.
 */
@Slf4j
public final class KubernetesClient implements AutoCloseable {

    @Getter
    private final KubernetesClientConfig config;
    private final HttpClient httpClient;

    public KubernetesClient(final KubernetesClientConfig config) {
        this.config = config;
        this.httpClient = createHttpClient();
    }

    /**
     * Execute a GET request to the Kubernetes API.
     *
     * @param path the API path (e.g., "/api/v1/namespaces/default/pods")
     * @return the HTTP response
     * @throws KubernetesClientException if the request fails
     */
    public HttpResponse get(final String path) throws KubernetesClientException {
        return execute("GET", path, null);
    }

    /**
     * Execute a POST request to the Kubernetes API.
     *
     * @param path the API path
     * @param body the request body (JSON)
     * @return the HTTP response
     * @throws KubernetesClientException if the request fails
     */
    public HttpResponse post(final String path, final String body) throws KubernetesClientException {
        return execute("POST", path, body);
    }

    /**
     * Execute a PUT request to the Kubernetes API.
     *
     * @param path the API path
     * @param body the request body (JSON)
     * @return the HTTP response
     * @throws KubernetesClientException if the request fails
     */
    public HttpResponse put(final String path, final String body) throws KubernetesClientException {
        return execute("PUT", path, body);
    }

    /**
     * Execute a PATCH request to the Kubernetes API.
     *
     * @param path the API path
     * @param body the request body (JSON)
     * @return the HTTP response
     * @throws KubernetesClientException if the request fails
     */
    public HttpResponse patch(final String path, final String body) throws KubernetesClientException {
        return execute("PATCH", path, body);
    }

    /**
     * Execute a DELETE request to the Kubernetes API.
     *
     * @param path the API path
     * @return the HTTP response
     * @throws KubernetesClientException if the request fails
     */
    public HttpResponse delete(final String path) throws KubernetesClientException {
        return execute("DELETE", path, null);
    }

    private HttpResponse execute(final String method, final String path, final String body) throws KubernetesClientException {
        try {
            if (config.getAuthProvider().needsRefresh()) {
                log.debug("Refreshing authentication token");
                config.getAuthProvider().refresh();
            }

            final String url = config.getApiServerUrl() + path;

            final Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", config.getAuthProvider().getAuthHeader());
            headers.put("Accept", "application/json");
            if (body != null) {
                headers.put("Content-Type", "application/json");
            }

            final HttpResponse response = switch (method) {
                case "GET" -> httpClient.get(url, headers);
                case "POST" -> httpClient.post(url, headers, body);
                case "PUT" -> httpClient.put(url, headers, body);
                case "PATCH" -> httpClient.patch(url, headers, body);
                case "DELETE" -> httpClient.delete(url, headers);
                default -> throw new KubernetesClientException("Unsupported HTTP method: " + method);
            };

            if (response.isUnauthorized() || response.isForbidden()) {
                throw new KubernetesClientException(
                        "Authentication failed: " + response.getStatusCode() + " - " + response.getBody(),
                        response.getStatusCode());
            }

            return response;

        } catch (AuthenticationException e) {
            throw new KubernetesClientException("Failed to authenticate with Kubernetes API", e);
        } catch (HttpException e) {
            throw new KubernetesClientException("HTTP request failed: " + e.getMessage(), e);
        }
    }

    private HttpClient createHttpClient() {
        return OkHttpClientImpl.builder()
                .connectTimeout(config.getConnectTimeout())
                .readTimeout(config.getReadTimeout())
                .certificateAuthority(config.getCertificateAuthority())
                .skipTlsVerify(config.isSkipTlsVerify())
                .build();
    }

    @Override
    public void close() {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
