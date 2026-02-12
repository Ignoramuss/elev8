package io.elev8.core.client;

import io.elev8.core.auth.AuthProvider;
import io.elev8.core.auth.AuthenticationException;
import io.elev8.core.list.ListOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KubernetesClientTest {

    @Test
    void shouldThrowExceptionOnAuthenticationFailure() throws Exception {
        final AuthProvider authProvider = mock(AuthProvider.class);
        when(authProvider.needsRefresh()).thenReturn(false);
        when(authProvider.getAuthHeader()).thenThrow(new AuthenticationException("Auth failed"));

        final KubernetesClientConfig config = KubernetesClientConfig.builder()
                .apiServerUrl("https://test-api.example.com")
                .authProvider(authProvider)
                .skipTlsVerify(true)
                .build();

        final KubernetesClient client = new KubernetesClient(config);

        assertThatThrownBy(() -> client.get("/api/v1/pods"))
                .isInstanceOf(KubernetesClientException.class)
                .hasMessageContaining("Failed to authenticate");
    }

    @Test
    void shouldCloseSuccessfully() {
        final AuthProvider authProvider = mock(AuthProvider.class);

        final KubernetesClientConfig config = KubernetesClientConfig.builder()
                .apiServerUrl("https://test-api.example.com")
                .authProvider(authProvider)
                .skipTlsVerify(true)
                .build();

        final KubernetesClient client = new KubernetesClient(config);
        client.close();
    }

    @Test
    void shouldThrowExceptionOnAuthenticationFailureWithListOptions() throws Exception {
        final AuthProvider authProvider = mock(AuthProvider.class);
        when(authProvider.needsRefresh()).thenReturn(false);
        when(authProvider.getAuthHeader()).thenThrow(new AuthenticationException("Auth failed"));

        final KubernetesClientConfig config = KubernetesClientConfig.builder()
                .apiServerUrl("https://test-api.example.com")
                .authProvider(authProvider)
                .skipTlsVerify(true)
                .build();

        final KubernetesClient client = new KubernetesClient(config);
        final ListOptions options = ListOptions.withFieldSelector("status.phase=Running");

        assertThatThrownBy(() -> client.get("/api/v1/pods", options))
                .isInstanceOf(KubernetesClientException.class)
                .hasMessageContaining("Failed to authenticate");
    }

    @Test
    void shouldDelegateToSimpleGetWhenListOptionsAreNull() throws Exception {
        final AuthProvider authProvider = mock(AuthProvider.class);
        when(authProvider.needsRefresh()).thenReturn(false);
        when(authProvider.getAuthHeader()).thenThrow(new AuthenticationException("Auth failed"));

        final KubernetesClientConfig config = KubernetesClientConfig.builder()
                .apiServerUrl("https://test-api.example.com")
                .authProvider(authProvider)
                .skipTlsVerify(true)
                .build();

        final KubernetesClient client = new KubernetesClient(config);

        assertThatThrownBy(() -> client.get("/api/v1/pods", (ListOptions) null))
                .isInstanceOf(KubernetesClientException.class)
                .hasMessageContaining("Failed to authenticate");
    }

    @Test
    void shouldGetConfig() {
        final AuthProvider authProvider = mock(AuthProvider.class);

        final KubernetesClientConfig config = KubernetesClientConfig.builder()
                .apiServerUrl("https://test-api.example.com")
                .authProvider(authProvider)
                .skipTlsVerify(true)
                .build();

        final KubernetesClient client = new KubernetesClient(config);
        final KubernetesClientConfig retrievedConfig = client.getConfig();

        assertThat(retrievedConfig).isNotNull();
        assertThat(retrievedConfig.getApiServerUrl()).isEqualTo("https://test-api.example.com");
    }
}
