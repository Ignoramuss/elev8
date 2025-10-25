package io.elev8.core.http;

import java.util.Map;

/**
 * HTTP client abstraction for Kubernetes API requests.
 */
public interface HttpClient {

    /**
     * Execute a GET request.
     *
     * @param url     the URL to request
     * @param headers request headers
     * @return the HTTP response
     * @throws HttpException if the request fails
     */
    HttpResponse get(String url, Map<String, String> headers) throws HttpException;

    /**
     * Execute a POST request.
     *
     * @param url     the URL to request
     * @param headers request headers
     * @param body    request body
     * @return the HTTP response
     * @throws HttpException if the request fails
     */
    HttpResponse post(String url, Map<String, String> headers, String body) throws HttpException;

    /**
     * Execute a PUT request.
     *
     * @param url     the URL to request
     * @param headers request headers
     * @param body    request body
     * @return the HTTP response
     * @throws HttpException if the request fails
     */
    HttpResponse put(String url, Map<String, String> headers, String body) throws HttpException;

    /**
     * Execute a PATCH request.
     *
     * @param url     the URL to request
     * @param headers request headers
     * @param body    request body
     * @return the HTTP response
     * @throws HttpException if the request fails
     */
    HttpResponse patch(String url, Map<String, String> headers, String body) throws HttpException;

    /**
     * Execute a DELETE request.
     *
     * @param url     the URL to request
     * @param headers request headers
     * @return the HTTP response
     * @throws HttpException if the request fails
     */
    HttpResponse delete(String url, Map<String, String> headers) throws HttpException;

    /**
     * Close the HTTP client and release resources.
     */
    void close();
}
