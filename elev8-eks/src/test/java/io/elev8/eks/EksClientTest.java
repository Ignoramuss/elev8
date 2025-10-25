package io.elev8.eks;

import io.elev8.core.auth.AuthProvider;
import io.elev8.resources.deployment.DeploymentManager;
import io.elev8.resources.pod.PodManager;
import io.elev8.resources.service.ServiceManager;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class EksClientTest {

    @Test
    void shouldThrowExceptionWhenClusterNameIsNull() {
        assertThatThrownBy(() -> EksClient.builder()
                .region("us-east-1")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cluster name is required");
    }

    @Test
    void shouldThrowExceptionWhenRegionIsNull() {
        assertThatThrownBy(() -> EksClient.builder()
                .cluster("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Region is required");
    }

    @Test
    void shouldThrowExceptionWhenAuthProviderNotConfigured() {
        assertThatThrownBy(() -> EksClient.builder()
                .cluster("test-cluster")
                .region("us-east-1")
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Authentication provider must be configured");
    }

    @Test
    void shouldThrowExceptionWhenIamAuthCalledWithoutCluster() {
        assertThatThrownBy(() -> EksClient.builder()
                .region("us-east-1")
                .iamAuth())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cluster name must be set before configuring IAM auth");
    }

    @Test
    void shouldThrowExceptionWhenIamAuthCalledWithoutRegion() {
        assertThatThrownBy(() -> EksClient.builder()
                .cluster("test-cluster")
                .iamAuth())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Region must be set before configuring IAM auth");
    }

    @Test
    void shouldAcceptRegionAsString() {
        final AuthProvider authProvider = mock(AuthProvider.class);

        final EksClient.Builder builder = EksClient.builder()
                .cluster("test-cluster")
                .region("us-west-2")
                .authProvider(authProvider);

        assertThat(builder).isNotNull();
    }

    @Test
    void shouldAcceptRegionAsEnum() {
        final AuthProvider authProvider = mock(AuthProvider.class);

        final EksClient.Builder builder = EksClient.builder()
                .cluster("test-cluster")
                .region(Region.US_WEST_2)
                .authProvider(authProvider);

        assertThat(builder).isNotNull();
    }
}
