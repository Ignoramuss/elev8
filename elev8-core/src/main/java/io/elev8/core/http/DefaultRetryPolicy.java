package io.elev8.core.http;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Default retry policy that retries on:
 * - 429 Too Many Requests
 * - 5xx Server Errors (500, 502, 503, 504)
 * - IOException (network failures) when retryOnConnectionFailure is enabled
 *
 * Uses exponential backoff with jitter for delay calculation.
 */
public final class DefaultRetryPolicy implements RetryPolicy {

    private final RetryConfig config;

    public DefaultRetryPolicy(final RetryConfig config) {
        this.config = config;
    }

    @Override
    public boolean shouldRetry(final HttpResponse response, final int attemptNumber) {
        if (attemptNumber > config.getMaxRetries()) {
            return false;
        }
        return response.isTooManyRequests() || response.isServerError();
    }

    @Override
    public boolean shouldRetry(final HttpException exception, final int attemptNumber) {
        if (attemptNumber > config.getMaxRetries()) {
            return false;
        }
        if (!config.isRetryOnConnectionFailure()) {
            return false;
        }
        return isConnectionFailure(exception);
    }

    @Override
    public Duration getDelay(final int attemptNumber, final HttpResponse response) {
        if (response != null && response.isTooManyRequests()) {
            final Duration retryAfter = response.getRetryAfter();
            if (retryAfter != null) {
                return retryAfter;
            }
        }
        return calculateExponentialBackoff(attemptNumber);
    }

    private boolean isConnectionFailure(final HttpException exception) {
        final Throwable cause = exception.getCause();
        return cause instanceof IOException;
    }

    private Duration calculateExponentialBackoff(final int attemptNumber) {
        final long baseDelayMs = config.getBaseDelay().toMillis();
        final long maxDelayMs = config.getMaxDelay().toMillis();

        final long exponentialDelay = (long) (baseDelayMs * Math.pow(2, attemptNumber - 1));
        final long cappedDelay = Math.min(exponentialDelay, maxDelayMs);

        final double jitter = config.getJitterFactor();
        final double jitterMultiplier = 1 + (ThreadLocalRandom.current().nextDouble() * 2 - 1) * jitter;
        final long jitteredDelay = (long) (cappedDelay * jitterMultiplier);

        return Duration.ofMillis(Math.max(0, jitteredDelay));
    }
}
