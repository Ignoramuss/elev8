package io.elev8.core.client;

import io.elev8.core.auth.AuthenticationException;
import io.elev8.core.http.HttpClient;
import io.elev8.core.http.HttpException;
import io.elev8.core.http.HttpResponse;
import io.elev8.core.http.OkHttpClientImpl;
import io.elev8.core.logs.LogOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.watch.WatchOptions;
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
     * Execute a PATCH request to the Kubernetes API with patch options.
     * Supports JSON Patch (RFC 6902), JSON Merge Patch (RFC 7396),
     * and Strategic Merge Patch (Kubernetes-specific).
     *
     * @param path the API path
     * @param options patch options for configuring the patch type and behavior
     * @param body the patch body (format depends on patch type)
     * @return the HTTP response
     * @throws KubernetesClientException if the request fails
     */
    public HttpResponse patch(final String path, final PatchOptions options, final String body)
            throws KubernetesClientException {
        try {
            if (config.getAuthProvider().needsRefresh()) {
                log.debug("Refreshing authentication token for patch");
                config.getAuthProvider().refresh();
            }

            final String url = buildPatchUrl(path, options);

            final Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", config.getAuthProvider().getAuthHeader());
            headers.put("Accept", "application/json");

            if (body != null && options != null && options.getPatchType() != null) {
                headers.put("Content-Type", options.getPatchType().getContentType());
            } else if (body != null) {
                headers.put("Content-Type", "application/strategic-merge-patch+json");
            }

            final HttpResponse response = httpClient.patch(url, headers, body);

            if (response.isUnauthorized() || response.isForbidden()) {
                throw new KubernetesClientException(
                        "Authentication failed: " + response.getStatusCode() + " - " + response.getBody(),
                        response.getStatusCode());
            }

            return response;

        } catch (AuthenticationException e) {
            throw new KubernetesClientException("Failed to authenticate for patch operation", e);
        } catch (HttpException e) {
            throw new KubernetesClientException("Patch request failed: " + e.getMessage(), e);
        }
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

    /**
     * Execute a watch request to the Kubernetes API.
     * Watch requests stream resource changes as they occur.
     *
     * @param path the API path
     * @param options watch options for configuring the watch behavior
     * @param handler the stream handler to process watch events
     * @throws KubernetesClientException if the request fails
     */
    public void watch(final String path, final WatchOptions options, final HttpClient.StreamHandler handler)
            throws KubernetesClientException {
        try {
            if (config.getAuthProvider().needsRefresh()) {
                log.debug("Refreshing authentication token for watch");
                config.getAuthProvider().refresh();
            }

            final String url = buildWatchUrl(path, options);

            final Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", config.getAuthProvider().getAuthHeader());
            headers.put("Accept", "application/json");

            httpClient.stream(url, headers, handler);

        } catch (AuthenticationException e) {
            throw new KubernetesClientException("Failed to authenticate for watch operation", e);
        } catch (HttpException e) {
            throw new KubernetesClientException("Watch request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Execute a log streaming request to the Kubernetes API.
     * Log requests stream pod container logs as they are written.
     *
     * @param path the API path to the pod logs endpoint
     * @param options log options for configuring the log behavior
     * @param handler the stream handler to process log lines
     * @throws KubernetesClientException if the request fails
     */
    public void logs(final String path, final LogOptions options, final HttpClient.StreamHandler handler)
            throws KubernetesClientException {
        try {
            if (config.getAuthProvider().needsRefresh()) {
                log.debug("Refreshing authentication token for log streaming");
                config.getAuthProvider().refresh();
            }

            final String url = buildLogsUrl(path, options);

            final Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", config.getAuthProvider().getAuthHeader());
            headers.put("Accept", "text/plain");

            httpClient.stream(url, headers, handler);

        } catch (AuthenticationException e) {
            throw new KubernetesClientException("Failed to authenticate for log streaming operation", e);
        } catch (HttpException e) {
            throw new KubernetesClientException("Log streaming request failed: " + e.getMessage(), e);
        }
    }

    private String buildWatchUrl(final String path, final WatchOptions options) {
        final StringBuilder url = new StringBuilder(config.getApiServerUrl());
        url.append(path);
        url.append("?watch=true");

        if (options != null) {
            if (options.getResourceVersion() != null) {
                url.append("&resourceVersion=").append(options.getResourceVersion());
            }
            if (options.getTimeoutSeconds() != null) {
                url.append("&timeoutSeconds=").append(options.getTimeoutSeconds());
            }
            if (options.getAllowWatchBookmarks() != null && options.getAllowWatchBookmarks()) {
                url.append("&allowWatchBookmarks=true");
            }
            if (options.getLabelSelector() != null) {
                url.append("&labelSelector=").append(options.getLabelSelector());
            }
            if (options.getFieldSelector() != null) {
                url.append("&fieldSelector=").append(options.getFieldSelector());
            }
        }

        return url.toString();
    }

    private String buildLogsUrl(final String path, final LogOptions options) {
        final StringBuilder url = new StringBuilder(config.getApiServerUrl());
        url.append(path);

        boolean firstParam = true;

        if (options != null) {
            if (options.getFollow() != null && options.getFollow()) {
                url.append(firstParam ? "?" : "&").append("follow=true");
                firstParam = false;
            }
            if (options.getTailLines() != null) {
                url.append(firstParam ? "?" : "&").append("tailLines=").append(options.getTailLines());
                firstParam = false;
            }
            if (options.getTimestamps() != null && options.getTimestamps()) {
                url.append(firstParam ? "?" : "&").append("timestamps=true");
                firstParam = false;
            }
            if (options.getSinceSeconds() != null) {
                url.append(firstParam ? "?" : "&").append("sinceSeconds=").append(options.getSinceSeconds());
                firstParam = false;
            }
            if (options.getSinceTime() != null) {
                url.append(firstParam ? "?" : "&").append("sinceTime=").append(options.getSinceTime());
                firstParam = false;
            }
            if (options.getContainer() != null) {
                url.append(firstParam ? "?" : "&").append("container=").append(options.getContainer());
                firstParam = false;
            }
            if (options.getPrevious() != null && options.getPrevious()) {
                url.append(firstParam ? "?" : "&").append("previous=true");
                firstParam = false;
            }
            if (options.getLimitBytes() != null) {
                url.append(firstParam ? "?" : "&").append("limitBytes=").append(options.getLimitBytes());
                firstParam = false;
            }
        }

        return url.toString();
    }

    private String buildPatchUrl(final String path, final PatchOptions options) {
        final StringBuilder url = new StringBuilder(config.getApiServerUrl());
        url.append(path);

        boolean firstParam = true;

        if (options != null) {
            if (options.getDryRun() != null && options.getDryRun()) {
                url.append(firstParam ? "?" : "&").append("dryRun=All");
                firstParam = false;
            }
            if (options.getFieldManager() != null) {
                url.append(firstParam ? "?" : "&").append("fieldManager=").append(options.getFieldManager());
                firstParam = false;
            }
            if (options.getForce() != null && options.getForce()) {
                url.append(firstParam ? "?" : "&").append("force=true");
                firstParam = false;
            }
        }

        return url.toString();
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
