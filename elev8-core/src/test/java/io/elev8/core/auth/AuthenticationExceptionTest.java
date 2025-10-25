package io.elev8.core.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        final AuthenticationException exception = new AuthenticationException("Auth failed");

        assertThat(exception.getMessage()).isEqualTo("Auth failed");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateExceptionWithCause() {
        final RuntimeException cause = new RuntimeException("Root cause");
        final AuthenticationException exception = new AuthenticationException("Auth failed", cause);

        assertThat(exception.getMessage()).isEqualTo("Auth failed");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldCreateExceptionWithCauseOnly() {
        final RuntimeException cause = new RuntimeException("Root cause");
        final AuthenticationException exception = new AuthenticationException(cause);

        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
