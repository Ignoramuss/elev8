package io.elev8.core.client;

import io.elev8.core.auth.AuthProvider;

import java.time.Duration;
import java.util.Objects;

/**
 * Configuration for Kubernetes client.
 */
public class KubernetesClientConfig {

    private final String apiServerUrl;
    private final AuthProvider authProvider;
    private final String certificateAuthority;
    private final boolean skipTlsVerify;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final String namespace;

    private KubernetesClientConfig(Builder builder) {
        this.apiServerUrl = Objects.requireNonNull(builder.apiServerUrl, "API server URL is required");
        this.authProvider = Objects.requireNonNull(builder.authProvider, "Auth provider is required");
        this.certificateAuthority = builder.certificateAuthority;
        this.skipTlsVerify = builder.skipTlsVerify;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.namespace = builder.namespace;
    }

    public String getApiServerUrl() {
        return apiServerUrl;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public String getCertificateAuthority() {
        return certificateAuthority;
    }

    public boolean isSkipTlsVerify() {
        return skipTlsVerify;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public String getNamespace() {
        return namespace;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String apiServerUrl;
        private AuthProvider authProvider;
        private String certificateAuthority;
        private boolean skipTlsVerify = false;
        private Duration connectTimeout = Duration.ofSeconds(30);
        private Duration readTimeout = Duration.ofSeconds(30);
        private String namespace = "default";

        public Builder apiServerUrl(String apiServerUrl) {
            this.apiServerUrl = apiServerUrl;
            return this;
        }

        public Builder authProvider(AuthProvider authProvider) {
            this.authProvider = authProvider;
            return this;
        }

        public Builder certificateAuthority(String certificateAuthority) {
            this.certificateAuthority = certificateAuthority;
            return this;
        }

        public Builder skipTlsVerify(boolean skipTlsVerify) {
            this.skipTlsVerify = skipTlsVerify;
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public KubernetesClientConfig build() {
            return new KubernetesClientConfig(this);
        }
    }
}
