package io.elev8.core.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingHttpClientTest {

    @Mock
    private HttpClient delegate;

    @Mock
    private HttpClient.StreamHandler streamHandler;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldReturnDelegateWhenConfigIsNull() {
        final HttpClient client = RateLimitingHttpClient.wrap(delegate, (RateLimiterConfig) null);

        assertThat(client).isSameAs(delegate);
    }

    @Test
    void shouldDelegateGetRequest() throws HttpException {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(1000.0)
                .burstCapacity(100)
                .build();

        final HttpClient client = RateLimitingHttpClient.wrap(delegate, config);
        final HttpResponse successResponse = new HttpResponse(200, "{}", Map.of());
        when(delegate.get(any(), any())).thenReturn(successResponse);

        final HttpResponse response = client.get("http://test", Map.of());

        assertThat(response.isSuccessful()).isTrue();
        verify(delegate, times(1)).get(eq("http://test"), any());
    }

    @Test
    void shouldDelegatePostRequest() throws HttpException {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(1000.0)
                .burstCapacity(100)
                .build();

        final HttpClient client = RateLimitingHttpClient.wrap(delegate, config);
        final HttpResponse successResponse = new HttpResponse(201, "{}", Map.of());
        when(delegate.post(any(), any(), any())).thenReturn(successResponse);

        final HttpResponse response = client.post("http://test", Map.of(), "{}");

        assertThat(response.getStatusCode()).isEqualTo(201);
        verify(delegate, times(1)).post(eq("http://test"), any(), eq("{}"));
    }

    @Test
    void shouldDelegatePutRequest() throws HttpException {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(1000.0)
                .burstCapacity(100)
                .build();

        final HttpClient client = RateLimitingHttpClient.wrap(delegate, config);
        final HttpResponse successResponse = new HttpResponse(200, "{}", Map.of());
        when(delegate.put(any(), any(), any())).thenReturn(successResponse);

        final HttpResponse response = client.put("http://test", Map.of(), "{}");

        assertThat(response.isSuccessful()).isTrue();
        verify(delegate, times(1)).put(eq("http://test"), any(), eq("{}"));
    }

    @Test
    void shouldDelegatePatchRequest() throws HttpException {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(1000.0)
                .burstCapacity(100)
                .build();

        final HttpClient client = RateLimitingHttpClient.wrap(delegate, config);
        final HttpResponse successResponse = new HttpResponse(200, "{}", Map.of());
        when(delegate.patch(any(), any(), any())).thenReturn(successResponse);

        final HttpResponse response = client.patch("http://test", Map.of(), "{}");

        assertThat(response.isSuccessful()).isTrue();
        verify(delegate, times(1)).patch(eq("http://test"), any(), eq("{}"));
    }

    @Test
    void shouldDelegateDeleteRequest() throws HttpException {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(1000.0)
                .burstCapacity(100)
                .build();

        final HttpClient client = RateLimitingHttpClient.wrap(delegate, config);
        final HttpResponse successResponse = new HttpResponse(204, "", Map.of());
        when(delegate.delete(any(), any())).thenReturn(successResponse);

        final HttpResponse response = client.delete("http://test", Map.of());

        assertThat(response.getStatusCode()).isEqualTo(204);
        verify(delegate, times(1)).delete(eq("http://test"), any());
    }

    @Test
    void shouldDelegateStreamRequest() throws HttpException {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(1000.0)
                .burstCapacity(100)
                .build();

        final HttpClient client = RateLimitingHttpClient.wrap(delegate, config);

        client.stream("http://test", Map.of(), streamHandler);

        verify(delegate, times(1)).stream(eq("http://test"), any(), eq(streamHandler));
    }

    @Test
    void shouldDelegateClose() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(1000.0)
                .burstCapacity(100)
                .build();

        final HttpClient client = RateLimitingHttpClient.wrap(delegate, config);

        client.close();

        verify(delegate, times(1)).close();
    }

    @Test
    void shouldRateLimitRequests() throws HttpException {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(50.0)
                .burstCapacity(5)
                .build();

        final HttpClient client = RateLimitingHttpClient.wrap(delegate, config);
        final HttpResponse successResponse = new HttpResponse(200, "{}", Map.of());
        when(delegate.get(any(), any())).thenReturn(successResponse);

        final long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            client.get("http://test", Map.of());
        }

        final long elapsed = System.currentTimeMillis() - startTime;

        assertThat(elapsed).isGreaterThanOrEqualTo(50);
        verify(delegate, times(10)).get(any(), any());
    }

    @Test
    void shouldAllowBurstRequests() throws HttpException {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(10.0)
                .burstCapacity(5)
                .build();

        final HttpClient client = RateLimitingHttpClient.wrap(delegate, config);
        final HttpResponse successResponse = new HttpResponse(200, "{}", Map.of());
        when(delegate.get(any(), any())).thenReturn(successResponse);

        final long startTime = System.currentTimeMillis();

        for (int i = 0; i < 5; i++) {
            client.get("http://test", Map.of());
        }

        final long elapsed = System.currentTimeMillis() - startTime;

        assertThat(elapsed).isLessThan(100);
        verify(delegate, times(5)).get(any(), any());
    }

    @Test
    void shouldWrapWithPreConfiguredRateLimiter() throws HttpException {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(1000.0)
                .burstCapacity(100)
                .build();

        final TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(config);
        final HttpClient client = RateLimitingHttpClient.wrap(delegate, rateLimiter);
        final HttpResponse successResponse = new HttpResponse(200, "{}", Map.of());
        when(delegate.get(any(), any())).thenReturn(successResponse);

        final HttpResponse response = client.get("http://test", Map.of());

        assertThat(response.isSuccessful()).isTrue();
        verify(delegate, times(1)).get(any(), any());
    }
}
