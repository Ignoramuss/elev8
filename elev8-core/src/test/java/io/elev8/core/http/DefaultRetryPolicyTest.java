package io.elev8.core.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRetryPolicyTest {

    private DefaultRetryPolicy policy;
    private RetryConfig config;

    @BeforeEach
    void setUp() {
        config = RetryConfig.builder()
                .maxRetries(3)
                .baseDelay(Duration.ofMillis(100))
                .maxDelay(Duration.ofSeconds(30))
                .jitterFactor(0.0)
                .retryOnConnectionFailure(true)
                .build();
        policy = new DefaultRetryPolicy(config);
    }

    @Test
    void shouldRetryOn429TooManyRequests() {
        final HttpResponse response = new HttpResponse(429, "", Map.of());

        assertThat(policy.shouldRetry(response, 1)).isTrue();
        assertThat(policy.shouldRetry(response, 2)).isTrue();
        assertThat(policy.shouldRetry(response, 3)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 502, 503, 504, 599})
    void shouldRetryOnServerErrors(final int statusCode) {
        final HttpResponse response = new HttpResponse(statusCode, "", Map.of());

        assertThat(policy.shouldRetry(response, 1)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 201, 204, 400, 401, 403, 404, 409})
    void shouldNotRetryOnNonRetryableStatusCodes(final int statusCode) {
        final HttpResponse response = new HttpResponse(statusCode, "", Map.of());

        assertThat(policy.shouldRetry(response, 1)).isFalse();
    }

    @Test
    void shouldNotRetryWhenMaxRetriesExceeded() {
        final HttpResponse response = new HttpResponse(503, "", Map.of());

        assertThat(policy.shouldRetry(response, 4)).isFalse();
    }

    @Test
    void shouldRetryOnIOExceptionWhenEnabled() {
        final HttpException exception = new HttpException("Connection failed", new IOException("Network error"));

        assertThat(policy.shouldRetry(exception, 1)).isTrue();
    }

    @Test
    void shouldNotRetryOnIOExceptionWhenDisabled() {
        final RetryConfig disabledConfig = RetryConfig.builder()
                .maxRetries(3)
                .retryOnConnectionFailure(false)
                .build();
        final DefaultRetryPolicy disabledPolicy = new DefaultRetryPolicy(disabledConfig);
        final HttpException exception = new HttpException("Connection failed", new IOException("Network error"));

        assertThat(disabledPolicy.shouldRetry(exception, 1)).isFalse();
    }

    @Test
    void shouldNotRetryOnNonIOException() {
        final HttpException exception = new HttpException("Bad request");

        assertThat(policy.shouldRetry(exception, 1)).isFalse();
    }

    @Test
    void shouldNotRetryExceptionWhenMaxRetriesExceeded() {
        final HttpException exception = new HttpException("Connection failed", new IOException("Network error"));

        assertThat(policy.shouldRetry(exception, 4)).isFalse();
    }

    @Test
    void shouldCalculateExponentialBackoff() {
        assertThat(policy.getDelay(1, null)).isEqualTo(Duration.ofMillis(100));
        assertThat(policy.getDelay(2, null)).isEqualTo(Duration.ofMillis(200));
        assertThat(policy.getDelay(3, null)).isEqualTo(Duration.ofMillis(400));
        assertThat(policy.getDelay(4, null)).isEqualTo(Duration.ofMillis(800));
    }

    @Test
    void shouldCapDelayAtMaxDelay() {
        final RetryConfig shortMaxConfig = RetryConfig.builder()
                .maxRetries(10)
                .baseDelay(Duration.ofSeconds(1))
                .maxDelay(Duration.ofSeconds(5))
                .jitterFactor(0.0)
                .build();
        final DefaultRetryPolicy shortMaxPolicy = new DefaultRetryPolicy(shortMaxConfig);

        assertThat(shortMaxPolicy.getDelay(5, null)).isEqualTo(Duration.ofSeconds(5));
        assertThat(shortMaxPolicy.getDelay(10, null)).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void shouldUseRetryAfterHeaderFor429() {
        final HttpResponse response = new HttpResponse(429, "", Map.of("Retry-After", "10"));

        final Duration delay = policy.getDelay(1, response);

        assertThat(delay).isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    void shouldUseExponentialBackoffWhenRetryAfterMissing() {
        final HttpResponse response = new HttpResponse(429, "", Map.of());

        final Duration delay = policy.getDelay(1, response);

        assertThat(delay).isEqualTo(Duration.ofMillis(100));
    }

    @Test
    void shouldUseExponentialBackoffWhenRetryAfterInvalid() {
        final HttpResponse response = new HttpResponse(429, "", Map.of("Retry-After", "invalid"));

        final Duration delay = policy.getDelay(1, response);

        assertThat(delay).isEqualTo(Duration.ofMillis(100));
    }

    @Test
    void shouldApplyJitterWhenConfigured() {
        final RetryConfig jitterConfig = RetryConfig.builder()
                .maxRetries(3)
                .baseDelay(Duration.ofMillis(1000))
                .jitterFactor(0.2)
                .build();
        final DefaultRetryPolicy jitterPolicy = new DefaultRetryPolicy(jitterConfig);

        final Duration delay = jitterPolicy.getDelay(1, null);

        assertThat(delay.toMillis()).isBetween(800L, 1200L);
    }
}
