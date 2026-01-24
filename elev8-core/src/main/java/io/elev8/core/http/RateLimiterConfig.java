package io.elev8.core.http;

import lombok.Builder;
import lombok.Value;

/**
 * Configuration for client-side HTTP request rate limiting using a token bucket algorithm.
 */
@Value
@Builder
public class RateLimiterConfig {

    @Builder.Default
    double requestsPerSecond = 10.0;

    @Builder.Default
    int burstCapacity = 20;

    /**
     * Creates a RateLimiterConfig with default values.
     *
     * @return default rate limiter configuration
     */
    public static RateLimiterConfig defaults() {
        return builder().build();
    }

    /**
     * Creates a RateLimiterConfig with rate limiting disabled (unlimited).
     *
     * @return rate limiter configuration with unlimited throughput
     */
    public static RateLimiterConfig unlimited() {
        return builder()
                .requestsPerSecond(Double.MAX_VALUE)
                .burstCapacity(Integer.MAX_VALUE)
                .build();
    }

    /**
     * Validates the configuration values.
     *
     * @throws IllegalArgumentException if any configuration value is invalid
     */
    public void validate() {
        if (requestsPerSecond <= 0) {
            throw new IllegalArgumentException("requestsPerSecond must be positive, got: " + requestsPerSecond);
        }
        if (burstCapacity <= 0) {
            throw new IllegalArgumentException("burstCapacity must be positive, got: " + burstCapacity);
        }
    }
}
