package io.elev8.core.http;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HttpResponseTest {

    @Test
    void shouldCreateHttpResponse() {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        final HttpResponse response = new HttpResponse(200, "{\"status\":\"ok\"}", headers);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("{\"status\":\"ok\"}");
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/json");
    }

    @Test
    void shouldReturnTrueForSuccessfulStatusCodes() {
        assertThat(new HttpResponse(200, "", Map.of()).isSuccessful()).isTrue();
        assertThat(new HttpResponse(201, "", Map.of()).isSuccessful()).isTrue();
        assertThat(new HttpResponse(204, "", Map.of()).isSuccessful()).isTrue();
        assertThat(new HttpResponse(299, "", Map.of()).isSuccessful()).isTrue();
    }

    @Test
    void shouldReturnFalseForNonSuccessfulStatusCodes() {
        assertThat(new HttpResponse(199, "", Map.of()).isSuccessful()).isFalse();
        assertThat(new HttpResponse(300, "", Map.of()).isSuccessful()).isFalse();
        assertThat(new HttpResponse(400, "", Map.of()).isSuccessful()).isFalse();
        assertThat(new HttpResponse(404, "", Map.of()).isSuccessful()).isFalse();
        assertThat(new HttpResponse(500, "", Map.of()).isSuccessful()).isFalse();
    }

    @Test
    void shouldIdentifyNotFoundStatus() {
        assertThat(new HttpResponse(404, "", Map.of()).isNotFound()).isTrue();
        assertThat(new HttpResponse(200, "", Map.of()).isNotFound()).isFalse();
        assertThat(new HttpResponse(403, "", Map.of()).isNotFound()).isFalse();
    }

    @Test
    void shouldIdentifyUnauthorizedStatus() {
        assertThat(new HttpResponse(401, "", Map.of()).isUnauthorized()).isTrue();
        assertThat(new HttpResponse(200, "", Map.of()).isUnauthorized()).isFalse();
        assertThat(new HttpResponse(403, "", Map.of()).isUnauthorized()).isFalse();
    }

    @Test
    void shouldIdentifyForbiddenStatus() {
        assertThat(new HttpResponse(403, "", Map.of()).isForbidden()).isTrue();
        assertThat(new HttpResponse(200, "", Map.of()).isForbidden()).isFalse();
        assertThat(new HttpResponse(401, "", Map.of()).isForbidden()).isFalse();
    }

    @Test
    void shouldIdentifyTooManyRequestsStatus() {
        assertThat(new HttpResponse(429, "", Map.of()).isTooManyRequests()).isTrue();
        assertThat(new HttpResponse(200, "", Map.of()).isTooManyRequests()).isFalse();
        assertThat(new HttpResponse(503, "", Map.of()).isTooManyRequests()).isFalse();
    }

    @Test
    void shouldIdentifyServerErrorStatus() {
        assertThat(new HttpResponse(500, "", Map.of()).isServerError()).isTrue();
        assertThat(new HttpResponse(502, "", Map.of()).isServerError()).isTrue();
        assertThat(new HttpResponse(503, "", Map.of()).isServerError()).isTrue();
        assertThat(new HttpResponse(504, "", Map.of()).isServerError()).isTrue();
        assertThat(new HttpResponse(599, "", Map.of()).isServerError()).isTrue();
        assertThat(new HttpResponse(499, "", Map.of()).isServerError()).isFalse();
        assertThat(new HttpResponse(600, "", Map.of()).isServerError()).isFalse();
        assertThat(new HttpResponse(200, "", Map.of()).isServerError()).isFalse();
    }

    @Test
    void shouldParseRetryAfterHeaderInSeconds() {
        final HttpResponse response = new HttpResponse(429, "", Map.of("Retry-After", "60"));

        assertThat(response.getRetryAfter()).isEqualTo(Duration.ofSeconds(60));
    }

    @Test
    void shouldReturnNullWhenRetryAfterHeaderMissing() {
        final HttpResponse response = new HttpResponse(429, "", Map.of());

        assertThat(response.getRetryAfter()).isNull();
    }

    @Test
    void shouldReturnNullWhenRetryAfterHeaderInvalid() {
        final HttpResponse response = new HttpResponse(429, "", Map.of("Retry-After", "invalid"));

        assertThat(response.getRetryAfter()).isNull();
    }

    @Test
    void shouldHandleWhitespaceInRetryAfterHeader() {
        final HttpResponse response = new HttpResponse(429, "", Map.of("Retry-After", "  30  "));

        assertThat(response.getRetryAfter()).isEqualTo(Duration.ofSeconds(30));
    }
}
