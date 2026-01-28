package io.elev8.core.discovery;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscoveryExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        final DiscoveryException exception = new DiscoveryException("Discovery failed");

        assertThat(exception.getMessage()).isEqualTo("Discovery failed");
        assertThat(exception.hasStatusCode()).isFalse();
        assertThat(exception.getStatusCode()).isNull();
    }

    @Test
    void shouldCreateExceptionWithStatusCode() {
        final DiscoveryException exception = new DiscoveryException("Not found", 404);

        assertThat(exception.getMessage()).isEqualTo("Not found");
        assertThat(exception.hasStatusCode()).isTrue();
        assertThat(exception.getStatusCode()).isEqualTo(404);
    }

    @Test
    void shouldCreateExceptionWithCause() {
        final RuntimeException cause = new RuntimeException("Network error");
        final DiscoveryException exception = new DiscoveryException("Discovery failed", cause);

        assertThat(exception.getMessage()).isEqualTo("Discovery failed");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.hasStatusCode()).isFalse();
    }

    @Test
    void shouldCreateExceptionWithStatusCodeAndCause() {
        final RuntimeException cause = new RuntimeException("Server error");
        final DiscoveryException exception = new DiscoveryException("Discovery failed", 500, cause);

        assertThat(exception.getMessage()).isEqualTo("Discovery failed");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.hasStatusCode()).isTrue();
        assertThat(exception.getStatusCode()).isEqualTo(500);
    }
}
