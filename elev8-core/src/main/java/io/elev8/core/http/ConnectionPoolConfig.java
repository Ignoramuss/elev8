package io.elev8.core.http;

import java.time.Duration;
import java.util.Objects;

/**
 * Configuration for HTTP connection pool settings.
 * Provides tuning options for connection reuse and resource management.
 */
public final class ConnectionPoolConfig {

    private static final int DEFAULT_MAX_IDLE_CONNECTIONS = 5;
    private static final Duration DEFAULT_KEEP_ALIVE_DURATION = Duration.ofMinutes(5);

    private final int maxIdleConnections;
    private final Duration keepAliveDuration;

    private ConnectionPoolConfig(final int maxIdleConnections, final Duration keepAliveDuration) {
        this.maxIdleConnections = maxIdleConnections;
        this.keepAliveDuration = keepAliveDuration;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public Duration getKeepAliveDuration() {
        return keepAliveDuration;
    }

    /**
     * Returns a new builder with default values.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a configuration with default values.
     */
    public static ConnectionPoolConfig defaults() {
        return new Builder().build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ConnectionPoolConfig that = (ConnectionPoolConfig) o;
        return maxIdleConnections == that.maxIdleConnections
                && Objects.equals(keepAliveDuration, that.keepAliveDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxIdleConnections, keepAliveDuration);
    }

    @Override
    public String toString() {
        return "ConnectionPoolConfig{"
                + "maxIdleConnections=" + maxIdleConnections
                + ", keepAliveDuration=" + keepAliveDuration
                + '}';
    }

    public static final class Builder {
        private int maxIdleConnections = DEFAULT_MAX_IDLE_CONNECTIONS;
        private Duration keepAliveDuration = DEFAULT_KEEP_ALIVE_DURATION;

        private Builder() {
        }

        /**
         * Sets the maximum number of idle connections to keep in the pool.
         *
         * @param maxIdleConnections the maximum idle connections (must be non-negative)
         * @return this builder
         * @throws IllegalArgumentException if maxIdleConnections is negative
         */
        public Builder maxIdleConnections(final int maxIdleConnections) {
            if (maxIdleConnections < 0) {
                throw new IllegalArgumentException("maxIdleConnections must be non-negative, got: " + maxIdleConnections);
            }
            this.maxIdleConnections = maxIdleConnections;
            return this;
        }

        /**
         * Sets the duration that idle connections are kept alive in the pool.
         *
         * @param keepAliveDuration the keep alive duration (must not be null or negative)
         * @return this builder
         * @throws NullPointerException if keepAliveDuration is null
         * @throws IllegalArgumentException if keepAliveDuration is negative
         */
        public Builder keepAliveDuration(final Duration keepAliveDuration) {
            Objects.requireNonNull(keepAliveDuration, "keepAliveDuration must not be null");
            if (keepAliveDuration.isNegative()) {
                throw new IllegalArgumentException("keepAliveDuration must not be negative, got: " + keepAliveDuration);
            }
            this.keepAliveDuration = keepAliveDuration;
            return this;
        }

        /**
         * Builds the ConnectionPoolConfig with the configured values.
         *
         * @return a new immutable ConnectionPoolConfig instance
         */
        public ConnectionPoolConfig build() {
            return new ConnectionPoolConfig(maxIdleConnections, keepAliveDuration);
        }
    }
}
