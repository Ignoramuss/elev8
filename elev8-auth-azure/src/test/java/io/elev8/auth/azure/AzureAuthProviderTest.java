package io.elev8.auth.azure;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import io.elev8.core.auth.AuthenticationException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AzureAuthProviderTest {

    @Test
    void shouldBuildWithExplicitCredentialAndGetToken() throws Exception {
        final TokenCredential mockCredential = createMockCredential(
                "test-token", OffsetDateTime.now().plusHours(1));

        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .credential(mockCredential)
                .build();

        assertThat(provider).isNotNull();
        assertThat(provider.getToken()).isEqualTo("test-token");
        provider.close();
    }

    @Test
    void shouldReturnBearerAuthType() throws Exception {
        final TokenCredential mockCredential = createMockCredential(
                "test-token", OffsetDateTime.now().plusHours(1));

        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .credential(mockCredential)
                .build();

        assertThat(provider.getAuthType()).isEqualTo("Bearer");
        provider.close();
    }

    @Test
    void shouldReturnBearerAuthHeader() throws Exception {
        final TokenCredential mockCredential = createMockCredential(
                "test-token", OffsetDateTime.now().plusHours(1));

        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .credential(mockCredential)
                .build();

        assertThat(provider.getAuthHeader()).isEqualTo("Bearer test-token");
        provider.close();
    }

    @Test
    void shouldReturnTrueForNeedsRefreshWhenNoToken() throws Exception {
        final TokenCredential mockCredential = createMockCredential(
                "test-token", OffsetDateTime.now().plusHours(1));

        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .credential(mockCredential)
                .build();

        assertThat(provider.needsRefresh()).isTrue();
        provider.close();
    }

    @Test
    void shouldReturnFalseForNeedsRefreshWhenTokenIsFresh() throws Exception {
        final TokenCredential mockCredential = createMockCredential(
                "test-token", OffsetDateTime.now().plusHours(1));

        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .credential(mockCredential)
                .build();

        provider.getToken();

        assertThat(provider.needsRefresh()).isFalse();
        provider.close();
    }

    @Test
    void shouldReturnTrueForNeedsRefreshWhenTokenIsAboutToExpire() throws Exception {
        final TokenCredential mockCredential = createMockCredential(
                "test-token", OffsetDateTime.now().plusSeconds(30));

        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .credential(mockCredential)
                .build();

        provider.getToken();

        assertThat(provider.needsRefresh()).isTrue();
        provider.close();
    }

    @Test
    void shouldReturnTrueForNeedsRefreshWhenTokenHasNoExpiration() throws Exception {
        final TokenCredential mockCredential = mock(TokenCredential.class);
        final AccessToken tokenWithoutExpiry = new AccessToken("no-expiry-token", null);
        when(mockCredential.getToken(any(TokenRequestContext.class)))
                .thenReturn(Mono.just(tokenWithoutExpiry));

        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .credential(mockCredential)
                .build();

        provider.getToken();

        assertThat(provider.needsRefresh()).isTrue();
        provider.close();
    }

    @Test
    void shouldRefreshToken() throws Exception {
        final TokenCredential mockCredential = mock(TokenCredential.class);
        final AccessToken firstToken = new AccessToken("first-token",
                OffsetDateTime.now().plusHours(1));
        final AccessToken secondToken = new AccessToken("second-token",
                OffsetDateTime.now().plusHours(2));

        when(mockCredential.getToken(any(TokenRequestContext.class)))
                .thenReturn(Mono.just(firstToken))
                .thenReturn(Mono.just(secondToken));

        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .credential(mockCredential)
                .build();

        assertThat(provider.getToken()).isEqualTo("first-token");

        provider.refresh();
        assertThat(provider.getToken()).isEqualTo("second-token");

        provider.close();
    }

    @Test
    void shouldCacheTokenAcrossMultipleCalls() throws Exception {
        final TokenCredential mockCredential = createMockCredential(
                "cached-token", OffsetDateTime.now().plusHours(1));

        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .credential(mockCredential)
                .build();

        assertThat(provider.getToken()).isEqualTo("cached-token");
        assertThat(provider.getToken()).isEqualTo("cached-token");
        assertThat(provider.getToken()).isEqualTo("cached-token");

        verify(mockCredential, times(1)).getToken(any(TokenRequestContext.class));

        provider.close();
    }

    @Test
    void shouldThrowWhenCredentialReturnsNullToken() {
        final TokenCredential mockCredential = mock(TokenCredential.class);
        when(mockCredential.getToken(any(TokenRequestContext.class)))
                .thenReturn(Mono.empty());

        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .credential(mockCredential)
                .build();

        assertThatThrownBy(provider::getToken)
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Azure credential returned no token");

        provider.close();
    }

    @Test
    void shouldWrapExceptionInAuthenticationException() {
        final TokenCredential mockCredential = mock(TokenCredential.class);
        when(mockCredential.getToken(any(TokenRequestContext.class)))
                .thenReturn(Mono.error(new RuntimeException("Network error")));

        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .credential(mockCredential)
                .build();

        assertThatThrownBy(provider::getToken)
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Failed to refresh Azure authentication token");

        provider.close();
    }

    @Test
    void shouldBuildWithClientSecretParams() {
        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .tenantId("test-tenant")
                .clientId("test-client")
                .clientSecret("test-secret")
                .build();

        assertThat(provider).isNotNull();
        provider.close();
    }

    @Test
    void shouldBuildWithManagedIdentityClientId() {
        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .managedIdentityClientId("mi-client-id")
                .build();

        assertThat(provider).isNotNull();
        provider.close();
    }

    @Test
    void shouldPreferExplicitCredentialOverClientSecret() throws Exception {
        final TokenCredential mockCredential = createMockCredential(
                "explicit-token", OffsetDateTime.now().plusHours(1));

        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .credential(mockCredential)
                .tenantId("test-tenant")
                .clientId("test-client")
                .clientSecret("test-secret")
                .build();

        assertThat(provider.getToken()).isEqualTo("explicit-token");
        provider.close();
    }

    @Test
    void shouldFallbackToDefaultCredential() {
        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .build();

        assertThat(provider).isNotNull();
        provider.close();
    }

    @Test
    void shouldCloseWithoutError() throws Exception {
        final TokenCredential mockCredential = createMockCredential(
                "test-token", OffsetDateTime.now().plusHours(1));

        final AzureAuthProvider provider = AzureAuthProvider.builder()
                .credential(mockCredential)
                .build();

        provider.close();
        provider.close();
    }

    private TokenCredential createMockCredential(final String tokenValue, final OffsetDateTime expiresAt) {
        final TokenCredential mockCredential = mock(TokenCredential.class);
        final AccessToken accessToken = new AccessToken(tokenValue, expiresAt);
        when(mockCredential.getToken(any(TokenRequestContext.class)))
                .thenReturn(Mono.just(accessToken));
        return mockCredential;
    }
}
