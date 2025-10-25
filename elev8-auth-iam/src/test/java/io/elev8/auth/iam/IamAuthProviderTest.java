package io.elev8.auth.iam;

import io.elev8.core.auth.AuthenticationException;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IamAuthProviderTest {

    private IamAuthProvider createAuthProvider() {
        final AwsBasicCredentials credentials = AwsBasicCredentials.create(
                "AKIAIOSFODNN7EXAMPLE",
                "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        final AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        return IamAuthProvider.builder()
                .clusterName("test-cluster")
                .region(Region.US_EAST_1)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Test
    void shouldGenerateToken() throws AuthenticationException {
        final IamAuthProvider authProvider = createAuthProvider();
        final String token = authProvider.getToken();

        assertThat(token).isNotNull();
        assertThat(token).startsWith("k8s-aws-v1.");
    }

    @Test
    void shouldCacheToken() throws AuthenticationException {
        final IamAuthProvider authProvider = createAuthProvider();
        final String token1 = authProvider.getToken();
        final String token2 = authProvider.getToken();

        assertThat(token1).isEqualTo(token2);
    }

    @Test
    void shouldNeedRefreshInitially() {
        final IamAuthProvider authProvider = createAuthProvider();
        assertThat(authProvider.needsRefresh()).isTrue();
    }

    @Test
    void shouldNotNeedRefreshAfterGeneration() throws AuthenticationException {
        final IamAuthProvider authProvider = createAuthProvider();
        authProvider.getToken();
        assertThat(authProvider.needsRefresh()).isFalse();
    }

    @Test
    void shouldRefreshToken() throws AuthenticationException {
        final IamAuthProvider authProvider = createAuthProvider();
        final String token1 = authProvider.getToken();
        authProvider.refresh();
        final String token2 = authProvider.getToken();

        assertThat(token2).isNotNull();
        assertThat(token2).startsWith("k8s-aws-v1.");
    }

    @Test
    void shouldThrowExceptionWhenClusterNameIsNull() {
        assertThatThrownBy(() -> IamAuthProvider.builder()
                .region(Region.US_EAST_1)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cluster name is required");
    }

    @Test
    void shouldThrowExceptionWhenClusterNameIsEmpty() {
        assertThatThrownBy(() -> IamAuthProvider.builder()
                .clusterName("")
                .region(Region.US_EAST_1)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cluster name is required");
    }

    @Test
    void shouldUseDefaultRegionWhenNotSpecified() {
        final AwsBasicCredentials credentials = AwsBasicCredentials.create("test", "test");
        final AwsCredentialsProvider provider = StaticCredentialsProvider.create(credentials);

        final IamAuthProvider provider1 = IamAuthProvider.builder()
                .clusterName("test-cluster")
                .credentialsProvider(provider)
                .build();

        assertThat(provider1).isNotNull();
    }

    @Test
    void shouldAcceptRegionAsString() {
        final AwsBasicCredentials credentials = AwsBasicCredentials.create("test", "test");
        final AwsCredentialsProvider provider = StaticCredentialsProvider.create(credentials);

        final IamAuthProvider provider1 = IamAuthProvider.builder()
                .clusterName("test-cluster")
                .region("us-west-2")
                .credentialsProvider(provider)
                .build();

        assertThat(provider1).isNotNull();
    }
}
