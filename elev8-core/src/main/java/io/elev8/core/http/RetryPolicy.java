package io.elev8.core.http;

import java.time.Duration;

/**
 * Strategy interface for determining retry behavior for HTTP requests.
 */
public interface RetryPolicy {

    /**
     * Determines whether a request should be retried based on the response.
     *
     * @param response      the HTTP response received
     * @param attemptNumber the current attempt number (1-based)
     * @return true if the request should be retried
     */
    boolean shouldRetry(HttpResponse response, int attemptNumber);

    /**
     * Determines whether a request should be retried based on an exception.
     *
     * @param exception     the exception that occurred
     * @param attemptNumber the current attempt number (1-based)
     * @return true if the request should be retried
     */
    boolean shouldRetry(HttpException exception, int attemptNumber);

    /**
     * Calculates the delay before the next retry attempt.
     *
     * @param attemptNumber the current attempt number (1-based)
     * @param response      the HTTP response (may be null if retry due to exception)
     * @return the duration to wait before retrying
     */
    Duration getDelay(int attemptNumber, HttpResponse response);
}
