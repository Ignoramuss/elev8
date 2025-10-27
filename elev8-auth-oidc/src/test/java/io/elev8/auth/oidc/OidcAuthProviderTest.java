package io.elev8.auth.oidc;

import io.elev8.core.auth.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleWithWebIdentityRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleWithWebIdentityResponse;
import software.amazon.awssdk.services.sts.model.Credentials;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OidcAuthProviderTest {

    @Mock
    private StsClient stsClient;

    @TempDir
    private Path tempDir;

    private Path tokenFile;
    private static final String TEST_ROLE_ARN = "arn:aws:iam::123456789012:role/TestRole";
    private static final String TEST_CLUSTER_NAME = "test-cluster";
    private static final String TEST_WEB_IDENTITY_TOKEN = "test-token-content";

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        tokenFile = tempDir.resolve("web-identity-token");
        Files.writeString(tokenFile, TEST_WEB_IDENTITY_TOKEN);
    }

    private OidcAuthProvider createAuthProvider() {
        final Credentials credentials = Credentials.builder()
                .accessKeyId("AKIAIOSFODNN7EXAMPLE")
                .secretAccessKey("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
                .sessionToken("FwoGZXIvYXdzEDoaDFXAMPLETOKEN")
                .expiration(Instant.now().plusSeconds(3600))
                .build();

        final AssumeRoleWithWebIdentityResponse response = AssumeRoleWithWebIdentityResponse.builder()
                .credentials(credentials)
                .build();

        when(stsClient.assumeRoleWithWebIdentity(any(AssumeRoleWithWebIdentityRequest.class)))
                .thenReturn(response);

        return OidcAuthProvider.builder()
                .clusterName(TEST_CLUSTER_NAME)
                .region(Region.US_EAST_1)
                .roleArn(TEST_ROLE_ARN)
                .webIdentityTokenFile(tokenFile.toString())
                .roleSessionName("test-session")
                .stsClient(stsClient)
                .build();
    }

    @Test
    void shouldGenerateToken() throws AuthenticationException {
        final OidcAuthProvider authProvider = createAuthProvider();
        final String token = authProvider.getToken();

        assertThat(token).isNotNull();
        assertThat(token).startsWith("k8s-aws-v1.");
    }

    @Test
    void shouldCacheToken() throws AuthenticationException {
        final OidcAuthProvider authProvider = createAuthProvider();
        final String token1 = authProvider.getToken();
        final String token2 = authProvider.getToken();

        assertThat(token1).isEqualTo(token2);
        verify(stsClient, times(1)).assumeRoleWithWebIdentity(any(AssumeRoleWithWebIdentityRequest.class));
    }

    @Test
    void shouldNeedRefreshInitially() {
        final OidcAuthProvider authProvider = createAuthProvider();
        assertThat(authProvider.needsRefresh()).isTrue();
    }

    @Test
    void shouldNotNeedRefreshAfterGeneration() throws AuthenticationException {
        final OidcAuthProvider authProvider = createAuthProvider();
        authProvider.getToken();
        assertThat(authProvider.needsRefresh()).isFalse();
    }

    @Test
    void shouldRefreshToken() throws AuthenticationException {
        final OidcAuthProvider authProvider = createAuthProvider();
        final String token1 = authProvider.getToken();
        authProvider.refresh();
        final String token2 = authProvider.getToken();

        assertThat(token2).isNotNull();
        assertThat(token2).startsWith("k8s-aws-v1.");
    }

    @Test
    void shouldThrowExceptionWhenClusterNameIsNull() {
        assertThatThrownBy(() -> OidcAuthProvider.builder()
                .region(Region.US_EAST_1)
                .roleArn(TEST_ROLE_ARN)
                .webIdentityTokenFile(tokenFile.toString())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cluster name is required");
    }

    @Test
    void shouldThrowExceptionWhenClusterNameIsEmpty() {
        assertThatThrownBy(() -> OidcAuthProvider.builder()
                .clusterName("")
                .region(Region.US_EAST_1)
                .roleArn(TEST_ROLE_ARN)
                .webIdentityTokenFile(tokenFile.toString())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cluster name is required");
    }

    @Test
    void shouldThrowExceptionWhenRoleArnIsNull() {
        assertThatThrownBy(() -> OidcAuthProvider.builder()
                .clusterName(TEST_CLUSTER_NAME)
                .region(Region.US_EAST_1)
                .webIdentityTokenFile(tokenFile.toString())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role ARN is required");
    }

    @Test
    void shouldThrowExceptionWhenWebIdentityTokenFileIsNull() {
        assertThatThrownBy(() -> OidcAuthProvider.builder()
                .clusterName(TEST_CLUSTER_NAME)
                .region(Region.US_EAST_1)
                .roleArn(TEST_ROLE_ARN)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Web identity token file is required");
    }

    @Test
    void shouldThrowExceptionWhenTokenFileDoesNotExist() {
        final OidcAuthProvider authProvider = OidcAuthProvider.builder()
                .clusterName(TEST_CLUSTER_NAME)
                .region(Region.US_EAST_1)
                .roleArn(TEST_ROLE_ARN)
                .webIdentityTokenFile("/non/existent/file")
                .stsClient(stsClient)
                .build();

        assertThatThrownBy(authProvider::getToken)
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Failed to generate OIDC authentication token");
    }

    @Test
    void shouldAcceptRegionAsString() {
        final OidcAuthProvider provider = OidcAuthProvider.builder()
                .clusterName(TEST_CLUSTER_NAME)
                .region("us-west-2")
                .roleArn(TEST_ROLE_ARN)
                .webIdentityTokenFile(tokenFile.toString())
                .stsClient(stsClient)
                .build();

        assertThat(provider).isNotNull();
    }

    @Test
    void shouldUseDefaultRegionWhenNotSpecified() {
        final OidcAuthProvider provider = OidcAuthProvider.builder()
                .clusterName(TEST_CLUSTER_NAME)
                .roleArn(TEST_ROLE_ARN)
                .webIdentityTokenFile(tokenFile.toString())
                .stsClient(stsClient)
                .build();

        assertThat(provider).isNotNull();
    }

    @Test
    void shouldAssumeRoleWithWebIdentity() throws AuthenticationException {
        final OidcAuthProvider authProvider = createAuthProvider();
        authProvider.getToken();

        verify(stsClient).assumeRoleWithWebIdentity(any(AssumeRoleWithWebIdentityRequest.class));
    }

    @Test
    void shouldCloseResources() {
        final OidcAuthProvider authProvider = createAuthProvider();
        authProvider.close();

        verify(stsClient).close();
    }
}
