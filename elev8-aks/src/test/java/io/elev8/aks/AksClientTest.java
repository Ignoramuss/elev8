package io.elev8.aks;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import io.elev8.auth.azure.AzureAuthProvider;
import io.elev8.resources.cloud.CloudKubernetesClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AksClientTest {

    @Test
    void shouldThrowExceptionWhenSubscriptionIdIsNull() {
        assertThatThrownBy(() -> AksClient.builder()
                .resourceGroupName("my-rg")
                .clusterName("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Subscription ID is required");
    }

    @Test
    void shouldThrowExceptionWhenSubscriptionIdIsEmpty() {
        assertThatThrownBy(() -> AksClient.builder()
                .subscriptionId("")
                .resourceGroupName("my-rg")
                .clusterName("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Subscription ID is required");
    }

    @Test
    void shouldThrowExceptionWhenResourceGroupNameIsNull() {
        assertThatThrownBy(() -> AksClient.builder()
                .subscriptionId("sub-123")
                .clusterName("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Resource group name is required");
    }

    @Test
    void shouldThrowExceptionWhenResourceGroupNameIsEmpty() {
        assertThatThrownBy(() -> AksClient.builder()
                .subscriptionId("sub-123")
                .resourceGroupName("")
                .clusterName("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Resource group name is required");
    }

    @Test
    void shouldThrowExceptionWhenClusterNameIsNull() {
        assertThatThrownBy(() -> AksClient.builder()
                .subscriptionId("sub-123")
                .resourceGroupName("my-rg")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cluster name is required");
    }

    @Test
    void shouldThrowExceptionWhenClusterNameIsEmpty() {
        assertThatThrownBy(() -> AksClient.builder()
                .subscriptionId("sub-123")
                .resourceGroupName("my-rg")
                .clusterName("")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cluster name is required");
    }

    @Test
    void shouldBuildClientWithPreSuppliedEndpointAndCA() {
        final AzureAuthProvider authProvider = AzureAuthProvider.builder()
                .credential(createMockCredential())
                .build();

        final AksClient client = AksClient.builder()
                .subscriptionId("sub-123")
                .resourceGroupName("my-rg")
                .clusterName("test-cluster")
                .apiServerUrl("https://10.0.0.1")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .authProvider(authProvider)
                .build();

        assertThat(client).isNotNull();
        assertThat(client).isInstanceOf(CloudKubernetesClient.class);
        assertThat(client.getSubscriptionId()).isEqualTo("sub-123");
        assertThat(client.getResourceGroupName()).isEqualTo("my-rg");
        assertThat(client.getClusterName()).isEqualTo("test-cluster");

        client.close();
    }

    @Test
    void shouldBuildClientWithCustomCredential() {
        final TokenCredential mockCredential = createMockCredential();

        final AksClient client = AksClient.builder()
                .subscriptionId("sub-123")
                .resourceGroupName("my-rg")
                .clusterName("test-cluster")
                .apiServerUrl("https://10.0.0.1")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .credential(mockCredential)
                .build();

        assertThat(client).isNotNull();
        assertThat(client.getSubscriptionId()).isEqualTo("sub-123");

        client.close();
    }

    @Test
    void shouldSupportAllResourceManagers() {
        final AzureAuthProvider authProvider = AzureAuthProvider.builder()
                .credential(createMockCredential())
                .build();

        final AksClient client = AksClient.builder()
                .subscriptionId("sub-123")
                .resourceGroupName("my-rg")
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
    void shouldSupportGenericResources() {
        final AzureAuthProvider authProvider = AzureAuthProvider.builder()
                .credential(createMockCredential())
                .build();

        final AksClient client = AksClient.builder()
                .subscriptionId("sub-123")
                .resourceGroupName("my-rg")
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
    void shouldSupportDynamicClient() {
        final AzureAuthProvider authProvider = AzureAuthProvider.builder()
                .credential(createMockCredential())
                .build();

        final AksClient client = AksClient.builder()
                .subscriptionId("sub-123")
                .resourceGroupName("my-rg")
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
    void shouldCloseWithoutError() {
        final AzureAuthProvider authProvider = AzureAuthProvider.builder()
                .credential(createMockCredential())
                .build();

        final AksClient client = AksClient.builder()
                .subscriptionId("sub-123")
                .resourceGroupName("my-rg")
                .clusterName("test-cluster")
                .apiServerUrl("https://10.0.0.1")
                .certificateAuthority("")
                .skipTlsVerify(true)
                .authProvider(authProvider)
                .build();

        client.close();
        client.close();
    }

    @Test
    void shouldExtractCaCertificateFromKubeconfig() {
        final String caCertContent = "-----BEGIN CERTIFICATE-----\nMIIBkTCB+wIJAL...\n-----END CERTIFICATE-----";
        final String base64Ca = Base64.getEncoder().encodeToString(caCertContent.getBytes(StandardCharsets.UTF_8));
        final String kubeconfig = "apiVersion: v1\nclusters:\n- cluster:\n    certificate-authority-data: " + base64Ca + "\n    server: https://test.hcp.eastus.azmk8s.io:443\n";

        final String result = AksClient.extractCaCertificate(List.of(kubeconfig.getBytes(StandardCharsets.UTF_8)));

        assertThat(result).isEqualTo(caCertContent);
    }

    @Test
    void shouldThrowWhenKubeconfigListIsEmpty() {
        assertThatThrownBy(() -> AksClient.extractCaCertificate(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No kubeconfig data");
    }

    @Test
    void shouldThrowWhenKubeconfigListIsNull() {
        assertThatThrownBy(() -> AksClient.extractCaCertificate(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No kubeconfig data");
    }

    @Test
    void shouldThrowWhenCaCertNotFoundInKubeconfig() {
        final String kubeconfig = "apiVersion: v1\nclusters:\n- cluster:\n    server: https://test.hcp.eastus.azmk8s.io:443\n";

        assertThatThrownBy(() -> AksClient.extractCaCertificate(List.of(kubeconfig.getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("certificate-authority-data not found");
    }

    private TokenCredential createMockCredential() {
        final TokenCredential mockCredential = mock(TokenCredential.class);
        final AccessToken accessToken = new AccessToken("test-token",
                OffsetDateTime.now().plusHours(1));
        when(mockCredential.getToken(any(TokenRequestContext.class)))
                .thenReturn(Mono.just(accessToken));
        return mockCredential;
    }
}
