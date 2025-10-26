package io.elev8.eks;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EksClientTest {

    @Test
    void shouldThrowExceptionWhenClusterNameIsNull() {
        assertThatThrownBy(() -> EksClient.builder()
                .region(Region.US_EAST_1)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cluster name is required");
    }

    @Test
    void shouldThrowExceptionWhenRegionIsNull() {
        assertThatThrownBy(() -> EksClient.builder()
                .clusterName("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Region is required");
    }

    @Test
    void shouldBuildClientWithMinimalConfiguration() {
        final EksClient client = EksClient.builder()
                .clusterName("test-cluster")
                .region(Region.US_EAST_1)
                .apiServerUrl("https://example.eks.amazonaws.com")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .build();

        assertThat(client).isNotNull();
        assertThat(client.getClusterName()).isEqualTo("test-cluster");
        assertThat(client.getRegion()).isEqualTo(Region.US_EAST_1);

        client.close();
    }

    @Test
    void shouldConfigureAssumeRole() {
        final EksClient client = EksClient.builder()
                .clusterName("test-cluster")
                .region(Region.US_EAST_1)
                .apiServerUrl("https://example.eks.amazonaws.com")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .roleArn("arn:aws:iam::123456789012:role/TestRole")
                .baseCredentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-key", "test-secret")))
                .build();

        assertThat(client).isNotNull();
        assertThat(client.getClusterName()).isEqualTo("test-cluster");
        assertThat(client.getRegion()).isEqualTo(Region.US_EAST_1);

        client.close();
    }

    @Test
    void shouldConfigureAssumeRoleWithCustomSessionName() {
        final EksClient client = EksClient.builder()
                .clusterName("test-cluster")
                .region(Region.US_EAST_1)
                .apiServerUrl("https://example.eks.amazonaws.com")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .roleArn("arn:aws:iam::123456789012:role/TestRole")
                .sessionName("custom-session-name")
                .baseCredentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-key", "test-secret")))
                .build();

        assertThat(client).isNotNull();
        assertThat(client.getClusterName()).isEqualTo("test-cluster");
        assertThat(client.getRegion()).isEqualTo(Region.US_EAST_1);

        client.close();
    }

    @Test
    void shouldConfigureAssumeRoleWithCustomCredentialsProvider() {
        final EksClient client = EksClient.builder()
                .clusterName("test-cluster")
                .region(Region.US_EAST_1)
                .apiServerUrl("https://example.eks.amazonaws.com")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .roleArn("arn:aws:iam::123456789012:role/TestRole")
                .baseCredentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-key", "test-secret")))
                .build();

        assertThat(client).isNotNull();
        assertThat(client.getClusterName()).isEqualTo("test-cluster");
        assertThat(client.getRegion()).isEqualTo(Region.US_EAST_1);

        client.close();
    }

    @Test
    void shouldUseDefaultSessionNameWhenNotSpecified() {
        final EksClient client = EksClient.builder()
                .clusterName("test-cluster")
                .region(Region.US_EAST_1)
                .apiServerUrl("https://example.eks.amazonaws.com")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .roleArn("arn:aws:iam::123456789012:role/TestRole")
                .baseCredentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-key", "test-secret")))
                .build();

        assertThat(client).isNotNull();

        client.close();
    }

    @Test
    void shouldSupportResourceManagers() {
        final EksClient client = EksClient.builder()
                .clusterName("test-cluster")
                .region(Region.US_EAST_1)
                .apiServerUrl("https://example.eks.amazonaws.com")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .build();

        assertThat(client.pods()).isNotNull();
        assertThat(client.services()).isNotNull();
        assertThat(client.deployments()).isNotNull();
        assertThat(client.getKubernetesClient()).isNotNull();

        client.close();
    }
}
