package io.elev8.gke;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import io.elev8.auth.gcp.GcpAuthProvider;
import io.elev8.resources.cloud.CloudKubernetesClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

class GkeClientTest {

    @Test
    void shouldThrowExceptionWhenProjectIdIsNull() {
        assertThatThrownBy(() -> GkeClient.builder()
                .location("us-central1-a")
                .clusterName("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Project ID is required");
    }

    @Test
    void shouldThrowExceptionWhenProjectIdIsEmpty() {
        assertThatThrownBy(() -> GkeClient.builder()
                .projectId("")
                .location("us-central1-a")
                .clusterName("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Project ID is required");
    }

    @Test
    void shouldThrowExceptionWhenLocationIsNull() {
        assertThatThrownBy(() -> GkeClient.builder()
                .projectId("my-project")
                .clusterName("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Location is required");
    }

    @Test
    void shouldThrowExceptionWhenLocationIsEmpty() {
        assertThatThrownBy(() -> GkeClient.builder()
                .projectId("my-project")
                .location("")
                .clusterName("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Location is required");
    }

    @Test
    void shouldThrowExceptionWhenClusterNameIsNull() {
        assertThatThrownBy(() -> GkeClient.builder()
                .projectId("my-project")
                .location("us-central1-a")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cluster name is required");
    }

    @Test
    void shouldThrowExceptionWhenClusterNameIsEmpty() {
        assertThatThrownBy(() -> GkeClient.builder()
                .projectId("my-project")
                .location("us-central1-a")
                .clusterName("")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cluster name is required");
    }

    @Test
    void shouldBuildClientWithPreSuppliedEndpointAndCA() throws IOException {
        final GcpAuthProvider authProvider = GcpAuthProvider.builder()
                .credentials(createMockCredentials())
                .build();

        final GkeClient client = GkeClient.builder()
                .projectId("my-project")
                .location("us-central1-a")
                .clusterName("test-cluster")
                .apiServerUrl("https://10.0.0.1")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .authProvider(authProvider)
                .build();

        assertThat(client).isNotNull();
        assertThat(client).isInstanceOf(CloudKubernetesClient.class);
        assertThat(client.getProjectId()).isEqualTo("my-project");
        assertThat(client.getLocation()).isEqualTo("us-central1-a");
        assertThat(client.getClusterName()).isEqualTo("test-cluster");

        client.close();
    }

    @Test
    void shouldBuildClientWithCustomCredentials() throws IOException {
        final GoogleCredentials mockCredentials = createMockCredentials();

        final GkeClient client = GkeClient.builder()
                .projectId("my-project")
                .location("us-central1-a")
                .clusterName("test-cluster")
                .apiServerUrl("https://10.0.0.1")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .credentials(mockCredentials)
                .build();

        assertThat(client).isNotNull();
        assertThat(client.getProjectId()).isEqualTo("my-project");

        client.close();
    }

    @Test
    void shouldSupportAllResourceManagers() throws IOException {
        final GcpAuthProvider authProvider = GcpAuthProvider.builder()
                .credentials(createMockCredentials())
                .build();

        final GkeClient client = GkeClient.builder()
                .projectId("my-project")
                .location("us-central1-a")
                .clusterName("test-cluster")
                .apiServerUrl("https://10.0.0.1")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .authProvider(authProvider)
                .build();

        assertThat(client.pods()).isNotNull();
        assertThat(client.services()).isNotNull();
        assertThat(client.deployments()).isNotNull();
        assertThat(client.daemonSets()).isNotNull();
        assertThat(client.events()).isNotNull();
        assertThat(client.jobs()).isNotNull();
        assertThat(client.leases()).isNotNull();
        assertThat(client.cronJobs()).isNotNull();
        assertThat(client.statefulSets()).isNotNull();
        assertThat(client.replicaSets()).isNotNull();
        assertThat(client.ingresses()).isNotNull();
        assertThat(client.networkPolicies()).isNotNull();
        assertThat(client.horizontalPodAutoscalers()).isNotNull();
        assertThat(client.verticalPodAutoscalers()).isNotNull();
        assertThat(client.limitRanges()).isNotNull();
        assertThat(client.podDisruptionBudgets()).isNotNull();
        assertThat(client.serviceAccounts()).isNotNull();
        assertThat(client.roles()).isNotNull();
        assertThat(client.roleBindings()).isNotNull();
        assertThat(client.clusterRoles()).isNotNull();
        assertThat(client.clusterRoleBindings()).isNotNull();
        assertThat(client.persistentVolumes()).isNotNull();
        assertThat(client.persistentVolumeClaims()).isNotNull();
        assertThat(client.configMaps()).isNotNull();
        assertThat(client.secrets()).isNotNull();
        assertThat(client.resourceQuotas()).isNotNull();
        assertThat(client.namespaces()).isNotNull();
        assertThat(client.customResourceDefinitions()).isNotNull();
        assertThat(client.getKubernetesClient()).isNotNull();

        client.close();
    }

    @Test
    void shouldSupportGenericResources() throws IOException {
        final GcpAuthProvider authProvider = GcpAuthProvider.builder()
                .credentials(createMockCredentials())
                .build();

        final GkeClient client = GkeClient.builder()
                .projectId("my-project")
                .location("us-central1-a")
                .clusterName("test-cluster")
                .apiServerUrl("https://10.0.0.1")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .authProvider(authProvider)
                .build();

        assertThat(client.genericResources("stable.example.com", "v1", "CronTab", "crontabs"))
                .isNotNull();
        assertThat(client.genericClusterResources("example.com", "v1", "ClusterPolicy", "clusterpolicies"))
                .isNotNull();

        client.close();
    }

    @Test
    void shouldSupportDynamicClient() throws IOException {
        final GcpAuthProvider authProvider = GcpAuthProvider.builder()
                .credentials(createMockCredentials())
                .build();

        final GkeClient client = GkeClient.builder()
                .projectId("my-project")
                .location("us-central1-a")
                .clusterName("test-cluster")
                .apiServerUrl("https://10.0.0.1")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .authProvider(authProvider)
                .build();

        assertThat(client.dynamic()).isNotNull();

        client.close();
    }

    @Test
    void shouldCloseWithoutError() throws IOException {
        final GcpAuthProvider authProvider = GcpAuthProvider.builder()
                .credentials(createMockCredentials())
                .build();

        final GkeClient client = GkeClient.builder()
                .projectId("my-project")
                .location("us-central1-a")
                .clusterName("test-cluster")
                .apiServerUrl("https://10.0.0.1")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .authProvider(authProvider)
                .build();

        client.close();
        client.close(); // double close should not throw
    }

    private GoogleCredentials createMockCredentials() throws IOException {
        final GoogleCredentials baseCredentials = mock(GoogleCredentials.class);
        final GoogleCredentials scopedCredentials = mock(GoogleCredentials.class);
        when(baseCredentials.createScoped(anyCollection())).thenReturn(scopedCredentials);

        final AccessToken accessToken = new AccessToken("test-token",
                Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        doNothing().when(scopedCredentials).refresh();
        when(scopedCredentials.getAccessToken()).thenReturn(accessToken);

        return baseCredentials;
    }
}
