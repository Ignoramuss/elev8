package io.elev8.core.http;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimiterConfigTest {

    @Test
    void shouldUseDefaultValues() {
        final RateLimiterConfig config = RateLimiterConfig.defaults();

        assertThat(config.getRequestsPerSecond()).isEqualTo(10.0);
        assertThat(config.getBurstCapacity()).isEqualTo(20);
    }

    @Test
    void shouldCreateUnlimitedConfig() {
        final RateLimiterConfig config = RateLimiterConfig.unlimited();

        assertThat(config.getRequestsPerSecond()).isEqualTo(Double.MAX_VALUE);
        assertThat(config.getBurstCapacity()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void shouldAllowCustomRequestsPerSecond() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(50.0)
                .build();

        assertThat(config.getRequestsPerSecond()).isEqualTo(50.0);
    }

    @Test
    void shouldAllowCustomBurstCapacity() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .burstCapacity(100)
                .build();

        assertThat(config.getBurstCapacity()).isEqualTo(100);
    }

    @Test
    void shouldAllowFullCustomization() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(25.0)
                .burstCapacity(50)
                .build();

        assertThat(config.getRequestsPerSecond()).isEqualTo(25.0);
        assertThat(config.getBurstCapacity()).isEqualTo(50);
    }

    @Test
    void shouldValidatePositiveRequestsPerSecond() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(0)
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requestsPerSecond must be positive");
    }

    @Test
    void shouldValidateNegativeRequestsPerSecond() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(-5.0)
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requestsPerSecond must be positive");
    }

    @Test
    void shouldValidatePositiveBurstCapacity() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .burstCapacity(0)
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("burstCapacity must be positive");
    }

    @Test
    void shouldValidateNegativeBurstCapacity() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .burstCapacity(-10)
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("burstCapacity must be positive");
    }

    @Test
    void shouldPassValidationWithValidConfig() {
        final RateLimiterConfig config = RateLimiterConfig.defaults();

        config.validate();
    }
}
