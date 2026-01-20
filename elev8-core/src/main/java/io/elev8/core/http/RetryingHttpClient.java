package io.elev8.core.http;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;

/**
 * HTTP client decorator that adds retry behavior with exponential backoff.
 * Wraps an existing HttpClient and retries failed requests according to the configured policy.
 */
@Slf4j
public final class RetryingHttpClient implements HttpClient {

    private final HttpClient delegate;
    private final RetryPolicy retryPolicy;
    private final RetryConfig config;

    private RetryingHttpClient(final HttpClient delegate, final RetryPolicy retryPolicy, final RetryConfig config) {
        this.delegate = delegate;
        this.retryPolicy = retryPolicy;
        this.config = config;
    }

    /**
     * Wraps an HttpClient with retry behavior.
     *
     * @param delegate the underlying HttpClient to wrap
     * @param config   the retry configuration (if null or maxRetries <= 0, returns delegate unchanged)
     * @return an HttpClient with retry behavior, or the original delegate if retry is disabled
     */
    public static HttpClient wrap(final HttpClient delegate, final RetryConfig config) {
        if (config == null || config.getMaxRetries() <= 0) {
            return delegate;
        }
        return new RetryingHttpClient(delegate, new DefaultRetryPolicy(config), config);
    }

    /**
     * Wraps an HttpClient with a custom retry policy.
     *
     * @param delegate    the underlying HttpClient to wrap
     * @param retryPolicy the custom retry policy
     * @param config      the retry configuration
     * @return an HttpClient with retry behavior
     */
    public static HttpClient wrap(final HttpClient delegate, final RetryPolicy retryPolicy, final RetryConfig config) {
        if (config == null || config.getMaxRetries() <= 0) {
            return delegate;
        }
        return new RetryingHttpClient(delegate, retryPolicy, config);
    }

    @Override
    public HttpResponse get(final String url, final Map<String, String> headers) throws HttpException {
        return executeWithRetry(() -> delegate.get(url, headers));
    }

    @Override
    public HttpResponse post(final String url, final Map<String, String> headers, final String body)
            throws HttpException {
        return executeWithRetry(() -> delegate.post(url, headers, body));
    }

    @Override
    public HttpResponse put(final String url, final Map<String, String> headers, final String body)
            throws HttpException {
        return executeWithRetry(() -> delegate.put(url, headers, body));
    }

    @Override
    public HttpResponse patch(final String url, final Map<String, String> headers, final String body)
            throws HttpException {
        return executeWithRetry(() -> delegate.patch(url, headers, body));
    }

    @Override
    public HttpResponse delete(final String url, final Map<String, String> headers) throws HttpException {
        return executeWithRetry(() -> delegate.delete(url, headers));
    }

    @Override
    public void stream(final String url, final Map<String, String> headers, final StreamHandler handler)
            throws HttpException {
        delegate.stream(url, headers, handler);
    }

    @Override
    public void close() {
        delegate.close();
    }

    private HttpResponse executeWithRetry(final HttpOperation operation) throws HttpException {
        int attempt = 1;
        HttpResponse lastResponse = null;
        HttpException lastException = null;

        while (attempt <= config.getMaxRetries() + 1) {
            try {
                final HttpResponse response = operation.execute();

                if (response.isSuccessful() || !retryPolicy.shouldRetry(response, attempt)) {
                    return response;
                }

                lastResponse = response;
                log.debug("Request returned status {}, attempt {}/{}", response.getStatusCode(),
                        attempt, config.getMaxRetries() + 1);

            } catch (HttpException e) {
                if (!retryPolicy.shouldRetry(e, attempt)) {
                    throw e;
                }

                lastException = e;
                log.debug("Request failed with exception, attempt {}/{}: {}",
                        attempt, config.getMaxRetries() + 1, e.getMessage());
            }

            if (attempt <= config.getMaxRetries()) {
                final Duration delay = retryPolicy.getDelay(attempt, lastResponse);
                sleep(delay);
            }

            attempt++;
        }

        if (lastException != null) {
            throw lastException;
        }

        return lastResponse;
    }

    private void sleep(final Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Retry sleep interrupted");
        }
    }

    @FunctionalInterface
    private interface HttpOperation {
        HttpResponse execute() throws HttpException;
    }
}
