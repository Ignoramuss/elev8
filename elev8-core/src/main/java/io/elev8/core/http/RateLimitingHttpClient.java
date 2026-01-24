package io.elev8.core.http;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * HTTP client decorator that applies rate limiting before sending requests.
 * Uses a token bucket algorithm to control request throughput.
 */
@Slf4j
public final class RateLimitingHttpClient implements HttpClient {

    private final HttpClient delegate;
    private final TokenBucketRateLimiter rateLimiter;

    private RateLimitingHttpClient(final HttpClient delegate, final TokenBucketRateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Wraps an HttpClient with rate limiting behavior.
     *
     * @param delegate the underlying HttpClient to wrap
     * @param config   the rate limiter configuration (if null, returns delegate unchanged)
     * @return an HttpClient with rate limiting, or the original delegate if config is null
     */
    public static HttpClient wrap(final HttpClient delegate, final RateLimiterConfig config) {
        if (config == null) {
            return delegate;
        }
        return new RateLimitingHttpClient(delegate, new TokenBucketRateLimiter(config));
    }

    /**
     * Wraps an HttpClient with a pre-configured rate limiter.
     *
     * @param delegate    the underlying HttpClient to wrap
     * @param rateLimiter the rate limiter to use
     * @return an HttpClient with rate limiting
     */
    public static HttpClient wrap(final HttpClient delegate, final TokenBucketRateLimiter rateLimiter) {
        return new RateLimitingHttpClient(delegate, rateLimiter);
    }

    @Override
    public HttpResponse get(final String url, final Map<String, String> headers) throws HttpException {
        acquirePermit();
        return delegate.get(url, headers);
    }

    @Override
    public HttpResponse post(final String url, final Map<String, String> headers, final String body)
            throws HttpException {
        acquirePermit();
        return delegate.post(url, headers, body);
    }

    @Override
    public HttpResponse put(final String url, final Map<String, String> headers, final String body)
            throws HttpException {
        acquirePermit();
        return delegate.put(url, headers, body);
    }

    @Override
    public HttpResponse patch(final String url, final Map<String, String> headers, final String body)
            throws HttpException {
        acquirePermit();
        return delegate.patch(url, headers, body);
    }

    @Override
    public HttpResponse delete(final String url, final Map<String, String> headers) throws HttpException {
        acquirePermit();
        return delegate.delete(url, headers);
    }

    @Override
    public void stream(final String url, final Map<String, String> headers, final StreamHandler handler)
            throws HttpException {
        acquirePermit();
        delegate.stream(url, headers, handler);
    }

    @Override
    public void close() {
        delegate.close();
    }

    private void acquirePermit() {
        log.debug("Acquiring rate limiter permit");
        rateLimiter.acquire();
        log.debug("Rate limiter permit acquired");
    }
}
