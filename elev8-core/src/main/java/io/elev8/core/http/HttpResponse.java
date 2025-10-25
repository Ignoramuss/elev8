package io.elev8.core.http;

import lombok.Value;

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
}
