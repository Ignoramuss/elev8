package io.elev8.core.client;

import io.elev8.core.auth.AuthProvider;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class KubernetesClientConfigTest {

    @Test
    void shouldBuildConfigWithRequiredFields() {
        final AuthProvider authProvider = mock(AuthProvider.class);

        final KubernetesClientConfig config = KubernetesClientConfig.builder()
                .apiServerUrl("https://api.example.com")
                .authProvider(authProvider)
                .build();

        assertThat(config.getApiServerUrl()).isEqualTo("https://api.example.com");
        assertThat(config.getAuthProvider()).isEqualTo(authProvider);
        assertThat(config.isSkipTlsVerify()).isFalse();
        assertThat(config.getConnectTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(config.getReadTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(config.getNamespace()).isEqualTo("default");
    }

    @Test
    void shouldBuildConfigWithAllFields() {
        final AuthProvider authProvider = mock(AuthProvider.class);
        final Duration connectTimeout = Duration.ofSeconds(60);
        final Duration readTimeout = Duration.ofSeconds(90);

        final KubernetesClientConfig config = KubernetesClientConfig.builder()
                .apiServerUrl("https://api.example.com")
                .authProvider(authProvider)
                .certificateAuthority("base64cert")
                .skipTlsVerify(true)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .namespace("custom")
                .build();

        assertThat(config.getApiServerUrl()).isEqualTo("https://api.example.com");
        assertThat(config.getAuthProvider()).isEqualTo(authProvider);
        assertThat(config.getCertificateAuthority()).isEqualTo("base64cert");
        assertThat(config.isSkipTlsVerify()).isTrue();
        assertThat(config.getConnectTimeout()).isEqualTo(connectTimeout);
        assertThat(config.getReadTimeout()).isEqualTo(readTimeout);
        assertThat(config.getNamespace()).isEqualTo("custom");
    }

    @Test
    void shouldThrowExceptionWhenApiServerUrlIsNull() {
        final AuthProvider authProvider = mock(AuthProvider.class);

        assertThatThrownBy(() -> KubernetesClientConfig.builder()
                .authProvider(authProvider)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("apiServerUrl is marked non-null but is null");
    }

    @Test
    void shouldThrowExceptionWhenAuthProviderIsNull() {
        assertThatThrownBy(() -> KubernetesClientConfig.builder()
                .apiServerUrl("https://api.example.com")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("authProvider is marked non-null but is null");
    }
}
