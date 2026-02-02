package io.elev8.auth.gcp;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import io.elev8.core.auth.AuthProvider;
import io.elev8.core.auth.AuthenticationException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;

/**
 * GCP-based authentication provider for GKE.
 * Supports Application Default Credentials, explicit credentials, service account key files,
 * and Workload Identity (automatic via ADC when running in GKE).
 */
@Slf4j
public final class GcpAuthProvider implements AuthProvider {

    private static final String CLOUD_PLATFORM_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
    private static final Duration REFRESH_THRESHOLD = Duration.ofMinutes(1);

    private final GoogleCredentials credentials;
    private volatile AccessToken cachedToken;

    private GcpAuthProvider(final GoogleCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public String getToken() throws AuthenticationException {
        if (cachedToken == null || needsRefresh()) {
            refresh();
        }
        return cachedToken.getTokenValue();
    }

    @Override
    public boolean needsRefresh() {
        if (cachedToken == null) {
            return true;
        }
        final Date expirationTime = cachedToken.getExpirationTime();
        if (expirationTime == null) {
            return true;
        }
        return Instant.now().plus(REFRESH_THRESHOLD).isAfter(expirationTime.toInstant());
    }

    @Override
    public void refresh() throws AuthenticationException {
        try {
            log.debug("Refreshing GCP authentication token");
            credentials.refresh();
            cachedToken = credentials.getAccessToken();
            log.debug("Successfully refreshed GCP token, expires at: {}",
                    cachedToken.getExpirationTime());
        } catch (IOException e) {
            throw new AuthenticationException("Failed to refresh GCP authentication token", e);
        }
    }

    @Override
    public void close() {
        // No resources to close
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private GoogleCredentials credentials;
        private String serviceAccountKeyPath;

        public Builder credentials(final GoogleCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public Builder serviceAccountKeyPath(final String path) {
            this.serviceAccountKeyPath = path;
            return this;
        }

        public GcpAuthProvider build() {
            try {
                final GoogleCredentials resolvedCredentials = resolveCredentials();
                final GoogleCredentials scoped = resolvedCredentials
                        .createScoped(Collections.singletonList(CLOUD_PLATFORM_SCOPE));
                return new GcpAuthProvider(scoped);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to initialize GCP credentials", e);
            }
        }

        private GoogleCredentials resolveCredentials() throws IOException {
            if (credentials != null) {
                return credentials;
            }
            if (serviceAccountKeyPath != null && !serviceAccountKeyPath.isEmpty()) {
                log.debug("Loading service account credentials from: {}", serviceAccountKeyPath);
                try (final FileInputStream keyStream = new FileInputStream(serviceAccountKeyPath)) {
                    return ServiceAccountCredentials.fromStream(keyStream);
                }
            }
            log.debug("Using Application Default Credentials");
            return GoogleCredentials.getApplicationDefault();
        }
    }
}
