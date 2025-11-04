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
     * Execute a streaming GET request for watch operations.
     * The response body will be streamed line by line to the handler.
     *
     * @param url the URL to request
     * @param headers request headers
     * @param handler callback to process each line of the response
     * @throws HttpException if the request fails
     */
    void stream(String url, Map<String, String> headers, StreamHandler handler) throws HttpException;

    /**
     * Close the HTTP client and release resources.
     */
    void close();

    /**
     * Handler interface for processing streaming HTTP responses.
     */
    interface StreamHandler {
        /**
         * Called for each line received in the streaming response.
         *
         * @param line the response line
         */
        void onLine(String line);

        /**
         * Called when an error occurs during streaming.
         *
         * @param exception the error that occurred
         */
        void onError(Exception exception);

        /**
         * Called when the stream is closed.
         */
        void onClose();
    }
}
