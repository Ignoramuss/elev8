package io.elev8.core.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KubernetesClientExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        final KubernetesClientException exception = new KubernetesClientException("Test error");

        assertThat(exception.getMessage()).isEqualTo("Test error");
        assertThat(exception.hasStatusCode()).isFalse();
        assertThat(exception.getStatusCode()).isNull();
    }

    @Test
    void shouldCreateExceptionWithStatusCode() {
        final KubernetesClientException exception = new KubernetesClientException("Test error", 403);

        assertThat(exception.getMessage()).isEqualTo("Test error");
        assertThat(exception.hasStatusCode()).isTrue();
        assertThat(exception.getStatusCode()).isEqualTo(403);
    }

    @Test
    void shouldCreateExceptionWithCause() {
        final RuntimeException cause = new RuntimeException("Root cause");
        final KubernetesClientException exception = new KubernetesClientException("Test error", cause);

        assertThat(exception.getMessage()).isEqualTo("Test error");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.hasStatusCode()).isFalse();
    }

    @Test
    void shouldCreateExceptionWithStatusCodeAndCause() {
        final RuntimeException cause = new RuntimeException("Root cause");
        final KubernetesClientException exception = new KubernetesClientException("Test error", 500, cause);

        assertThat(exception.getMessage()).isEqualTo("Test error");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.hasStatusCode()).isTrue();
        assertThat(exception.getStatusCode()).isEqualTo(500);
    }
}
