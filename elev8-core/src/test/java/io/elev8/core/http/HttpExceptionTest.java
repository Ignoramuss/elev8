package io.elev8.core.http;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        final HttpException exception = new HttpException("Test error");

        assertThat(exception.getMessage()).isEqualTo("Test error");
        assertThat(exception.hasStatusCode()).isFalse();
        assertThat(exception.getStatusCode()).isNull();
    }

    @Test
    void shouldCreateExceptionWithStatusCode() {
        final HttpException exception = new HttpException("Test error", 404);

        assertThat(exception.getMessage()).isEqualTo("Test error");
        assertThat(exception.hasStatusCode()).isTrue();
        assertThat(exception.getStatusCode()).isEqualTo(404);
    }

    @Test
    void shouldCreateExceptionWithCause() {
        final RuntimeException cause = new RuntimeException("Root cause");
        final HttpException exception = new HttpException("Test error", cause);

        assertThat(exception.getMessage()).isEqualTo("Test error");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.hasStatusCode()).isFalse();
    }

    @Test
    void shouldCreateExceptionWithStatusCodeAndCause() {
        final RuntimeException cause = new RuntimeException("Root cause");
        final HttpException exception = new HttpException("Test error", 500, cause);

        assertThat(exception.getMessage()).isEqualTo("Test error");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.hasStatusCode()).isTrue();
        assertThat(exception.getStatusCode()).isEqualTo(500);
    }
}
