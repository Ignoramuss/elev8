package io.elev8.core.http;

import lombok.Builder;
import lombok.Value;

import java.time.Duration;

/**
 * Configuration for HTTP request retry behavior with exponential backoff.
 */
@Value
@Builder
public class RetryConfig {

    @Builder.Default
    int maxRetries = 3;

    @Builder.Default
    Duration baseDelay = Duration.ofMillis(100);

    @Builder.Default
    Duration maxDelay = Duration.ofSeconds(30);

    @Builder.Default
    double jitterFactor = 0.2;

    @Builder.Default
    boolean retryOnConnectionFailure = true;

    /**
     * Creates a RetryConfig with default values.
     *
     * @return default retry configuration
     */
    public static RetryConfig defaults() {
        return builder().build();
    }

    /**
     * Creates a RetryConfig with retry disabled.
     *
     * @return retry configuration with maxRetries set to 0
     */
    public static RetryConfig disabled() {
        return builder().maxRetries(0).build();
    }
}
