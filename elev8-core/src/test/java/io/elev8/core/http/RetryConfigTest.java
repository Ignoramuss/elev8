package io.elev8.core.http;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RetryConfigTest {

    @Test
    void shouldUseDefaultValues() {
        final RetryConfig config = RetryConfig.defaults();

        assertThat(config.getMaxRetries()).isEqualTo(3);
        assertThat(config.getBaseDelay()).isEqualTo(Duration.ofMillis(100));
        assertThat(config.getMaxDelay()).isEqualTo(Duration.ofSeconds(30));
        assertThat(config.getJitterFactor()).isEqualTo(0.2);
        assertThat(config.isRetryOnConnectionFailure()).isTrue();
    }

    @Test
    void shouldCreateDisabledConfig() {
        final RetryConfig config = RetryConfig.disabled();

        assertThat(config.getMaxRetries()).isEqualTo(0);
    }

    @Test
    void shouldAllowCustomMaxRetries() {
        final RetryConfig config = RetryConfig.builder()
                .maxRetries(5)
                .build();

        assertThat(config.getMaxRetries()).isEqualTo(5);
    }

    @Test
    void shouldAllowCustomBaseDelay() {
        final RetryConfig config = RetryConfig.builder()
                .baseDelay(Duration.ofMillis(500))
                .build();

        assertThat(config.getBaseDelay()).isEqualTo(Duration.ofMillis(500));
    }

    @Test
    void shouldAllowCustomMaxDelay() {
        final RetryConfig config = RetryConfig.builder()
                .maxDelay(Duration.ofMinutes(1))
                .build();

        assertThat(config.getMaxDelay()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void shouldAllowCustomJitterFactor() {
        final RetryConfig config = RetryConfig.builder()
                .jitterFactor(0.5)
                .build();

        assertThat(config.getJitterFactor()).isEqualTo(0.5);
    }

    @Test
    void shouldAllowDisablingRetryOnConnectionFailure() {
        final RetryConfig config = RetryConfig.builder()
                .retryOnConnectionFailure(false)
                .build();

        assertThat(config.isRetryOnConnectionFailure()).isFalse();
    }

    @Test
    void shouldAllowFullCustomization() {
        final RetryConfig config = RetryConfig.builder()
                .maxRetries(10)
                .baseDelay(Duration.ofSeconds(1))
                .maxDelay(Duration.ofMinutes(5))
                .jitterFactor(0.3)
                .retryOnConnectionFailure(false)
                .build();

        assertThat(config.getMaxRetries()).isEqualTo(10);
        assertThat(config.getBaseDelay()).isEqualTo(Duration.ofSeconds(1));
        assertThat(config.getMaxDelay()).isEqualTo(Duration.ofMinutes(5));
        assertThat(config.getJitterFactor()).isEqualTo(0.3);
        assertThat(config.isRetryOnConnectionFailure()).isFalse();
    }
}
