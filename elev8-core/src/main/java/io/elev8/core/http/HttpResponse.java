package io.elev8.core.http;

import lombok.Value;

import java.time.Duration;
import java.util.Map;

@Value
public class HttpResponse {
    int statusCode;
    String body;
    Map<String, String> headers;

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public boolean isNotFound() {
        return statusCode == 404;
    }

    public boolean isUnauthorized() {
        return statusCode == 401;
    }

    public boolean isForbidden() {
        return statusCode == 403;
    }

    public boolean isTooManyRequests() {
        return statusCode == 429;
    }

    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }

    public Duration getRetryAfter() {
        final String retryAfter = headers.get("Retry-After");
        if (retryAfter == null) {
            return null;
        }
        try {
            final long seconds = Long.parseLong(retryAfter.trim());
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
