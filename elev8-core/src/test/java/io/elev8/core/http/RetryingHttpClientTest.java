package io.elev8.core.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryingHttpClientTest {

    @Mock
    private HttpClient delegate;

    @Mock
    private HttpClient.StreamHandler streamHandler;

    private HttpClient retryingClient;
    private RetryConfig config;

    @BeforeEach
    void setUp() {
        config = RetryConfig.builder()
                .maxRetries(3)
                .baseDelay(Duration.ofMillis(1))
                .maxDelay(Duration.ofMillis(10))
                .jitterFactor(0.0)
                .retryOnConnectionFailure(true)
                .build();
        retryingClient = RetryingHttpClient.wrap(delegate, config);
    }

    @Test
    void shouldReturnDelegateWhenConfigIsNull() {
        final HttpClient client = RetryingHttpClient.wrap(delegate, null);

        assertThat(client).isSameAs(delegate);
    }

    @Test
    void shouldReturnDelegateWhenMaxRetriesIsZero() {
        final HttpClient client = RetryingHttpClient.wrap(delegate, RetryConfig.disabled());

        assertThat(client).isSameAs(delegate);
    }

    @Test
    void shouldReturnSuccessfulResponseWithoutRetry() throws HttpException {
        final HttpResponse successResponse = new HttpResponse(200, "{}", Map.of());
        when(delegate.get(any(), any())).thenReturn(successResponse);

        final HttpResponse response = retryingClient.get("http://test", Map.of());

        assertThat(response.isSuccessful()).isTrue();
        verify(delegate, times(1)).get(any(), any());
    }

    @Test
    void shouldRetryOn503AndEventuallySucceed() throws HttpException {
        final HttpResponse errorResponse = new HttpResponse(503, "Service Unavailable", Map.of());
        final HttpResponse successResponse = new HttpResponse(200, "{}", Map.of());
        when(delegate.get(any(), any()))
                .thenReturn(errorResponse)
                .thenReturn(errorResponse)
                .thenReturn(successResponse);

        final HttpResponse response = retryingClient.get("http://test", Map.of());

        assertThat(response.isSuccessful()).isTrue();
        verify(delegate, times(3)).get(any(), any());
    }

    @Test
    void shouldRetryOn429WithRetryAfterHeader() throws HttpException {
        final HttpResponse rateLimitResponse = new HttpResponse(429, "Too Many Requests",
                Map.of("Retry-After", "1"));
        final HttpResponse successResponse = new HttpResponse(200, "{}", Map.of());
        when(delegate.post(any(), any(), any()))
                .thenReturn(rateLimitResponse)
                .thenReturn(successResponse);

        final HttpResponse response = retryingClient.post("http://test", Map.of(), "{}");

        assertThat(response.isSuccessful()).isTrue();
        verify(delegate, times(2)).post(any(), any(), any());
    }

    @Test
    void shouldNotRetryOn400BadRequest() throws HttpException {
        final HttpResponse badRequestResponse = new HttpResponse(400, "Bad Request", Map.of());
        when(delegate.put(any(), any(), any())).thenReturn(badRequestResponse);

        final HttpResponse response = retryingClient.put("http://test", Map.of(), "{}");

        assertThat(response.getStatusCode()).isEqualTo(400);
        verify(delegate, times(1)).put(any(), any(), any());
    }

    @Test
    void shouldNotRetryOn404NotFound() throws HttpException {
        final HttpResponse notFoundResponse = new HttpResponse(404, "Not Found", Map.of());
        when(delegate.delete(any(), any())).thenReturn(notFoundResponse);

        final HttpResponse response = retryingClient.delete("http://test", Map.of());

        assertThat(response.isNotFound()).isTrue();
        verify(delegate, times(1)).delete(any(), any());
    }

    @Test
    void shouldExhaustRetriesAndReturnLastResponse() throws HttpException {
        final HttpResponse errorResponse = new HttpResponse(503, "Service Unavailable", Map.of());
        when(delegate.patch(any(), any(), any())).thenReturn(errorResponse);

        final HttpResponse response = retryingClient.patch("http://test", Map.of(), "{}");

        assertThat(response.getStatusCode()).isEqualTo(503);
        verify(delegate, times(4)).patch(any(), any(), any());
    }

    @Test
    void shouldRetryOnIOExceptionAndEventuallySucceed() throws HttpException {
        final HttpException ioException = new HttpException("Connection failed", new IOException("Network error"));
        final HttpResponse successResponse = new HttpResponse(200, "{}", Map.of());
        when(delegate.get(any(), any()))
                .thenThrow(ioException)
                .thenThrow(ioException)
                .thenReturn(successResponse);

        final HttpResponse response = retryingClient.get("http://test", Map.of());

        assertThat(response.isSuccessful()).isTrue();
        verify(delegate, times(3)).get(any(), any());
    }

    @Test
    void shouldExhaustRetriesOnIOExceptionAndThrow() throws HttpException {
        final HttpException ioException = new HttpException("Connection failed", new IOException("Network error"));
        when(delegate.get(any(), any())).thenThrow(ioException);

        assertThatThrownBy(() -> retryingClient.get("http://test", Map.of()))
                .isInstanceOf(HttpException.class)
                .hasMessageContaining("Connection failed");
        verify(delegate, times(4)).get(any(), any());
    }

    @Test
    void shouldNotRetryNonIOException() throws HttpException {
        final HttpException nonRetryableException = new HttpException("Bad request");
        when(delegate.get(any(), any())).thenThrow(nonRetryableException);

        assertThatThrownBy(() -> retryingClient.get("http://test", Map.of()))
                .isInstanceOf(HttpException.class)
                .hasMessageContaining("Bad request");
        verify(delegate, times(1)).get(any(), any());
    }

    @Test
    void shouldPassThroughStreamWithoutRetry() throws HttpException {
        retryingClient.stream("http://test", Map.of(), streamHandler);

        verify(delegate, times(1)).stream(eq("http://test"), eq(Map.of()), eq(streamHandler));
    }

    @Test
    void shouldDelegateClose() {
        retryingClient.close();

        verify(delegate, times(1)).close();
    }

    @Test
    void shouldRetryAllHttpMethodsWithBody() throws HttpException {
        final HttpResponse errorResponse = new HttpResponse(503, "", Map.of());
        final HttpResponse successResponse = new HttpResponse(200, "{}", Map.of());

        when(delegate.post(any(), any(), any()))
                .thenReturn(errorResponse)
                .thenReturn(successResponse);
        when(delegate.put(any(), any(), any()))
                .thenReturn(errorResponse)
                .thenReturn(successResponse);
        when(delegate.patch(any(), any(), any()))
                .thenReturn(errorResponse)
                .thenReturn(successResponse);

        assertThat(retryingClient.post("http://test", Map.of(), "{}").isSuccessful()).isTrue();
        assertThat(retryingClient.put("http://test", Map.of(), "{}").isSuccessful()).isTrue();
        assertThat(retryingClient.patch("http://test", Map.of(), "{}").isSuccessful()).isTrue();
    }

    @Test
    void shouldRetryAllHttpMethodsWithoutBody() throws HttpException {
        final HttpResponse errorResponse = new HttpResponse(503, "", Map.of());
        final HttpResponse successResponse = new HttpResponse(200, "{}", Map.of());

        when(delegate.get(any(), any()))
                .thenReturn(errorResponse)
                .thenReturn(successResponse);
        when(delegate.delete(any(), any()))
                .thenReturn(errorResponse)
                .thenReturn(successResponse);

        assertThat(retryingClient.get("http://test", Map.of()).isSuccessful()).isTrue();
        assertThat(retryingClient.delete("http://test", Map.of()).isSuccessful()).isTrue();
    }

    @Test
    void shouldSupportCustomRetryPolicy() throws HttpException {
        final RetryPolicy customPolicy = new RetryPolicy() {
            @Override
            public boolean shouldRetry(final HttpResponse response, final int attemptNumber) {
                return response.getStatusCode() == 418 && attemptNumber <= 2;
            }

            @Override
            public boolean shouldRetry(final HttpException exception, final int attemptNumber) {
                return false;
            }

            @Override
            public Duration getDelay(final int attemptNumber, final HttpResponse response) {
                return Duration.ofMillis(1);
            }
        };

        final HttpClient clientWithCustomPolicy = RetryingHttpClient.wrap(delegate, customPolicy, config);
        final HttpResponse teapotResponse = new HttpResponse(418, "I'm a teapot", Map.of());
        final HttpResponse successResponse = new HttpResponse(200, "{}", Map.of());
        when(delegate.get(any(), any()))
                .thenReturn(teapotResponse)
                .thenReturn(successResponse);

        final HttpResponse response = clientWithCustomPolicy.get("http://test", Map.of());

        assertThat(response.isSuccessful()).isTrue();
        verify(delegate, times(2)).get(any(), any());
    }
}
