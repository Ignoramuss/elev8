package io.elev8.core.client;

import io.elev8.core.auth.AuthProvider;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.Duration;

/**
 * Configuration for Kubernetes client.
 */
@Value
@Builder
public class KubernetesClientConfig {

    @NonNull
    String apiServerUrl;

    @NonNull
    AuthProvider authProvider;

    String certificateAuthority;

    @Builder.Default
    boolean skipTlsVerify = false;

    @Builder.Default
    Duration connectTimeout = Duration.ofSeconds(30);

    @Builder.Default
    Duration readTimeout = Duration.ofSeconds(30);

    @Builder.Default
    String namespace = "default";
}
