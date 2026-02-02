package io.elev8.auth.gcp;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import io.elev8.core.auth.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

class GcpAuthProviderTest {

    @Test
    void shouldBuildWithExplicitCredentials() throws Exception {
        final GoogleCredentials mockCredentials = createMockCredentials(
                "test-token", Instant.now().plus(1, ChronoUnit.HOURS));

        final GcpAuthProvider provider = GcpAuthProvider.builder()
                .credentials(mockCredentials)
                .build();

        assertThat(provider).isNotNull();
        assertThat(provider.getToken()).isEqualTo("test-token");
        provider.close();
    }

    @Test
    void shouldReturnBearerAuthType() throws Exception {
        final GoogleCredentials mockCredentials = createMockCredentials(
                "test-token", Instant.now().plus(1, ChronoUnit.HOURS));

        final GcpAuthProvider provider = GcpAuthProvider.builder()
                .credentials(mockCredentials)
                .build();

        assertThat(provider.getAuthType()).isEqualTo("Bearer");
        provider.close();
    }

    @Test
    void shouldReturnBearerAuthHeader() throws Exception {
        final GoogleCredentials mockCredentials = createMockCredentials(
                "test-token", Instant.now().plus(1, ChronoUnit.HOURS));

        final GcpAuthProvider provider = GcpAuthProvider.builder()
                .credentials(mockCredentials)
                .build();

        assertThat(provider.getAuthHeader()).isEqualTo("Bearer test-token");
        provider.close();
    }

    @Test
    void shouldReturnTrueForNeedsRefreshWhenNoToken() throws Exception {
        final GoogleCredentials mockCredentials = createMockCredentials(
                "test-token", Instant.now().plus(1, ChronoUnit.HOURS));

        final GcpAuthProvider provider = GcpAuthProvider.builder()
                .credentials(mockCredentials)
                .build();

        assertThat(provider.needsRefresh()).isTrue();
        provider.close();
    }

    @Test
    void shouldReturnFalseForNeedsRefreshWhenTokenIsFresh() throws Exception {
        final GoogleCredentials mockCredentials = createMockCredentials(
                "test-token", Instant.now().plus(1, ChronoUnit.HOURS));

        final GcpAuthProvider provider = GcpAuthProvider.builder()
                .credentials(mockCredentials)
                .build();

        provider.getToken();

        assertThat(provider.needsRefresh()).isFalse();
        provider.close();
    }

    @Test
    void shouldReturnTrueForNeedsRefreshWhenTokenIsAboutToExpire() throws Exception {
        final GoogleCredentials mockCredentials = createMockCredentials(
                "test-token", Instant.now().plus(30, ChronoUnit.SECONDS));

        final GcpAuthProvider provider = GcpAuthProvider.builder()
                .credentials(mockCredentials)
                .build();

        provider.getToken();

        assertThat(provider.needsRefresh()).isTrue();
        provider.close();
    }

    @Test
    void shouldReturnTrueForNeedsRefreshWhenTokenHasNoExpiration() throws Exception {
        final GoogleCredentials baseCredentials = mock(GoogleCredentials.class);
        final GoogleCredentials scopedCredentials = mock(GoogleCredentials.class);
        when(baseCredentials.createScoped(anyCollection())).thenReturn(scopedCredentials);

        final AccessToken tokenWithoutExpiry = new AccessToken("no-expiry-token", null);
        doNothing().when(scopedCredentials).refresh();
        when(scopedCredentials.getAccessToken()).thenReturn(tokenWithoutExpiry);

        final GcpAuthProvider provider = GcpAuthProvider.builder()
                .credentials(baseCredentials)
                .build();

        provider.getToken();

        assertThat(provider.needsRefresh()).isTrue();
        provider.close();
    }

    @Test
    void shouldRefreshToken() throws Exception {
        final GoogleCredentials baseCredentials = mock(GoogleCredentials.class);
        final GoogleCredentials scopedCredentials = mock(GoogleCredentials.class);
        when(baseCredentials.createScoped(anyCollection())).thenReturn(scopedCredentials);

        final AccessToken firstToken = new AccessToken("first-token",
                Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        final AccessToken secondToken = new AccessToken("second-token",
                Date.from(Instant.now().plus(2, ChronoUnit.HOURS)));

        doNothing().when(scopedCredentials).refresh();
        when(scopedCredentials.getAccessToken())
                .thenReturn(firstToken)
                .thenReturn(secondToken);

        final GcpAuthProvider provider = GcpAuthProvider.builder()
                .credentials(baseCredentials)
                .build();

        assertThat(provider.getToken()).isEqualTo("first-token");

        provider.refresh();
        assertThat(provider.getToken()).isEqualTo("second-token");

        provider.close();
    }

    @Test
    void shouldCacheTokenAcrossMultipleCalls() throws Exception {
        final GoogleCredentials mockCredentials = createMockCredentials(
                "cached-token", Instant.now().plus(1, ChronoUnit.HOURS));

        final GcpAuthProvider provider = GcpAuthProvider.builder()
                .credentials(mockCredentials)
                .build();

        assertThat(provider.getToken()).isEqualTo("cached-token");
        assertThat(provider.getToken()).isEqualTo("cached-token");
        assertThat(provider.getToken()).isEqualTo("cached-token");

        final GoogleCredentials scopedCredentials = extractScopedCredentials(mockCredentials);
        verify(scopedCredentials, times(1)).refresh();

        provider.close();
    }

    @Test
    void shouldWrapIOExceptionInAuthenticationException() throws Exception {
        final GoogleCredentials baseCredentials = mock(GoogleCredentials.class);
        final GoogleCredentials scopedCredentials = mock(GoogleCredentials.class);
        when(baseCredentials.createScoped(anyCollection())).thenReturn(scopedCredentials);
        doThrow(new IOException("Network error")).when(scopedCredentials).refresh();

        final GcpAuthProvider provider = GcpAuthProvider.builder()
                .credentials(baseCredentials)
                .build();

        assertThatThrownBy(provider::getToken)
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Failed to refresh GCP authentication token")
                .hasCauseInstanceOf(IOException.class);

        provider.close();
    }

    @Test
    void shouldThrowOnInvalidServiceAccountKeyPath() {
        assertThatThrownBy(() -> GcpAuthProvider.builder()
                .serviceAccountKeyPath("/nonexistent/path/key.json")
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to initialize GCP credentials");
    }

    @Test
    void shouldThrowOnInvalidServiceAccountKeyContent(@TempDir Path tempDir) throws Exception {
        final Path keyFile = tempDir.resolve("invalid-key.json");
        Files.writeString(keyFile, "not valid json");

        assertThatThrownBy(() -> GcpAuthProvider.builder()
                .serviceAccountKeyPath(keyFile.toString())
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to initialize GCP credentials");
    }

    @Test
    void shouldPreferExplicitCredentialsOverKeyPath() throws Exception {
        final GoogleCredentials mockCredentials = createMockCredentials(
                "explicit-token", Instant.now().plus(1, ChronoUnit.HOURS));

        final GcpAuthProvider provider = GcpAuthProvider.builder()
                .credentials(mockCredentials)
                .serviceAccountKeyPath("/some/path/key.json")
                .build();

        assertThat(provider.getToken()).isEqualTo("explicit-token");
        provider.close();
    }

    @Test
    void shouldCloseWithoutError() throws Exception {
        final GoogleCredentials mockCredentials = createMockCredentials(
                "test-token", Instant.now().plus(1, ChronoUnit.HOURS));

        final GcpAuthProvider provider = GcpAuthProvider.builder()
                .credentials(mockCredentials)
                .build();

        provider.close();
        provider.close(); // double close should not throw
    }

    private GoogleCredentials createMockCredentials(final String tokenValue, final Instant expiry)
            throws IOException {
        final GoogleCredentials baseCredentials = mock(GoogleCredentials.class);
        final GoogleCredentials scopedCredentials = mock(GoogleCredentials.class);
        when(baseCredentials.createScoped(anyCollection())).thenReturn(scopedCredentials);

        final AccessToken accessToken = new AccessToken(tokenValue, Date.from(expiry));
        doNothing().when(scopedCredentials).refresh();
        when(scopedCredentials.getAccessToken()).thenReturn(accessToken);

        return baseCredentials;
    }

    private GoogleCredentials extractScopedCredentials(final GoogleCredentials baseCredentials) {
        try {
            return baseCredentials.createScoped(anyCollection());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
