package io.elev8.cloud;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.cloud.CloudKubernetesClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AbstractCloudKubernetesClientTest {

    private KubernetesClient mockKubernetesClient;
    private TestCloudClient client;

    @BeforeEach
    void setUp() {
        mockKubernetesClient = mock(KubernetesClient.class);
        client = new TestCloudClient(mockKubernetesClient);
    }

    @Test
    void shouldImplementCloudKubernetesClient() {
        assertThat(client).isInstanceOf(CloudKubernetesClient.class);
    }

    @Test
    void shouldReturnInjectedKubernetesClient() {
        assertThat(client.getKubernetesClient()).isSameAs(mockKubernetesClient);
    }

    @Test
    void shouldInitializeAllResourceManagers() {
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
    }

    @Test
    void shouldReturnSameManagerInstanceOnMultipleCalls() {
        assertThat(client.pods()).isSameAs(client.pods());
        assertThat(client.services()).isSameAs(client.services());
        assertThat(client.deployments()).isSameAs(client.deployments());
    }

    @Test
    void shouldCreateGenericResourceManager() {
        assertThat(client.genericResources("stable.example.com", "v1", "CronTab", "crontabs"))
                .isNotNull();
    }

    @Test
    void shouldCreateGenericClusterResourceManager() {
        assertThat(client.genericClusterResources("example.com", "v1", "ClusterPolicy", "clusterpolicies"))
                .isNotNull();
    }

    @Test
    void shouldCreateDynamicClient() {
        assertThat(client.dynamic()).isNotNull();
    }

    @Test
    void shouldDelegateCloseToKubernetesClient() {
        client.close();

        verify(mockKubernetesClient).close();
    }

    @Test
    void shouldHandleDoubleCloseWithoutError() {
        client.close();
        client.close();

        verify(mockKubernetesClient, times(2)).close();
    }

    private static class TestCloudClient extends AbstractCloudKubernetesClient {
        TestCloudClient(final KubernetesClient kubernetesClient) {
            super(kubernetesClient);
        }
    }
}
