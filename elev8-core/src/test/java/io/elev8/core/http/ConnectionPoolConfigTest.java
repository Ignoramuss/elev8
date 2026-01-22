package io.elev8.core.http;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConnectionPoolConfigTest {

    @Test
    void shouldUseDefaultValues() {
        final ConnectionPoolConfig config = ConnectionPoolConfig.defaults();

        assertThat(config.getMaxIdleConnections()).isEqualTo(5);
        assertThat(config.getKeepAliveDuration()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void shouldUseDefaultValuesFromBuilder() {
        final ConnectionPoolConfig config = ConnectionPoolConfig.builder().build();

        assertThat(config.getMaxIdleConnections()).isEqualTo(5);
        assertThat(config.getKeepAliveDuration()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void shouldAllowCustomMaxIdleConnections() {
        final ConnectionPoolConfig config = ConnectionPoolConfig.builder()
                .maxIdleConnections(10)
                .build();

        assertThat(config.getMaxIdleConnections()).isEqualTo(10);
    }

    @Test
    void shouldAllowZeroMaxIdleConnections() {
        final ConnectionPoolConfig config = ConnectionPoolConfig.builder()
                .maxIdleConnections(0)
                .build();

        assertThat(config.getMaxIdleConnections()).isEqualTo(0);
    }

    @Test
    void shouldRejectNegativeMaxIdleConnections() {
        assertThatThrownBy(() -> ConnectionPoolConfig.builder().maxIdleConnections(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxIdleConnections must be non-negative");
    }

    @Test
    void shouldAllowCustomKeepAliveDuration() {
        final ConnectionPoolConfig config = ConnectionPoolConfig.builder()
                .keepAliveDuration(Duration.ofMinutes(10))
                .build();

        assertThat(config.getKeepAliveDuration()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void shouldAllowZeroKeepAliveDuration() {
        final ConnectionPoolConfig config = ConnectionPoolConfig.builder()
                .keepAliveDuration(Duration.ZERO)
                .build();

        assertThat(config.getKeepAliveDuration()).isEqualTo(Duration.ZERO);
    }

    @Test
    void shouldRejectNullKeepAliveDuration() {
        assertThatThrownBy(() -> ConnectionPoolConfig.builder().keepAliveDuration(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("keepAliveDuration must not be null");
    }

    @Test
    void shouldRejectNegativeKeepAliveDuration() {
        assertThatThrownBy(() -> ConnectionPoolConfig.builder().keepAliveDuration(Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("keepAliveDuration must not be negative");
    }

    @Test
    void shouldAllowFullCustomization() {
        final ConnectionPoolConfig config = ConnectionPoolConfig.builder()
                .maxIdleConnections(20)
                .keepAliveDuration(Duration.ofMinutes(15))
                .build();

        assertThat(config.getMaxIdleConnections()).isEqualTo(20);
        assertThat(config.getKeepAliveDuration()).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void shouldImplementEqualsCorrectly() {
        final ConnectionPoolConfig config1 = ConnectionPoolConfig.builder()
                .maxIdleConnections(10)
                .keepAliveDuration(Duration.ofMinutes(5))
                .build();

        final ConnectionPoolConfig config2 = ConnectionPoolConfig.builder()
                .maxIdleConnections(10)
                .keepAliveDuration(Duration.ofMinutes(5))
                .build();

        final ConnectionPoolConfig config3 = ConnectionPoolConfig.builder()
                .maxIdleConnections(20)
                .keepAliveDuration(Duration.ofMinutes(5))
                .build();

        assertThat(config1).isEqualTo(config2);
        assertThat(config1).isNotEqualTo(config3);
        assertThat(config1).isNotEqualTo(null);
        assertThat(config1).isNotEqualTo("not a config");
    }

    @Test
    void shouldImplementHashCodeCorrectly() {
        final ConnectionPoolConfig config1 = ConnectionPoolConfig.builder()
                .maxIdleConnections(10)
                .keepAliveDuration(Duration.ofMinutes(5))
                .build();

        final ConnectionPoolConfig config2 = ConnectionPoolConfig.builder()
                .maxIdleConnections(10)
                .keepAliveDuration(Duration.ofMinutes(5))
                .build();

        assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
    }

    @Test
    void shouldImplementToStringCorrectly() {
        final ConnectionPoolConfig config = ConnectionPoolConfig.builder()
                .maxIdleConnections(10)
                .keepAliveDuration(Duration.ofMinutes(5))
                .build();

        final String toString = config.toString();
        assertThat(toString).contains("maxIdleConnections=10");
        assertThat(toString).contains("keepAliveDuration=PT5M");
    }
}
