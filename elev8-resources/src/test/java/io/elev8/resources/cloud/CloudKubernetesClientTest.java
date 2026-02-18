package io.elev8.resources.cloud;

import io.elev8.resources.aggregation.ResourceAggregator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CloudKubernetesClientTest {

    @Test
    void shouldExtendAutoCloseable() {
        assertThat(AutoCloseable.class).isAssignableFrom(CloudKubernetesClient.class);
    }

    @Test
    void shouldBeImplementableAsMock() {
        final CloudKubernetesClient client = mock(CloudKubernetesClient.class);
        assertThat(client).isNotNull();
    }

    @Test
    void shouldDefineAllResourceManagerAccessors() {
        final CloudKubernetesClient client = mock(CloudKubernetesClient.class);

        client.pods();
        client.services();
        client.deployments();
        client.daemonSets();
        client.events();
        client.jobs();
        client.leases();
        client.cronJobs();
        client.statefulSets();
        client.replicaSets();
        client.ingresses();
        client.networkPolicies();
        client.horizontalPodAutoscalers();
        client.verticalPodAutoscalers();
        client.limitRanges();
        client.podDisruptionBudgets();
        client.serviceAccounts();
        client.roles();
        client.roleBindings();
        client.clusterRoles();
        client.clusterRoleBindings();
        client.persistentVolumes();
        client.persistentVolumeClaims();
        client.configMaps();
        client.secrets();
        client.resourceQuotas();
        client.namespaces();
        client.customResourceDefinitions();

        verify(client).pods();
        verify(client).services();
        verify(client).deployments();
        verify(client).daemonSets();
        verify(client).events();
        verify(client).jobs();
        verify(client).leases();
        verify(client).cronJobs();
        verify(client).statefulSets();
        verify(client).replicaSets();
        verify(client).ingresses();
        verify(client).networkPolicies();
        verify(client).horizontalPodAutoscalers();
        verify(client).verticalPodAutoscalers();
        verify(client).limitRanges();
        verify(client).podDisruptionBudgets();
        verify(client).serviceAccounts();
        verify(client).roles();
        verify(client).roleBindings();
        verify(client).clusterRoles();
        verify(client).clusterRoleBindings();
        verify(client).persistentVolumes();
        verify(client).persistentVolumeClaims();
        verify(client).configMaps();
        verify(client).secrets();
        verify(client).resourceQuotas();
        verify(client).namespaces();
        verify(client).customResourceDefinitions();
    }

    @Test
    void shouldDefineGenericResourceMethods() {
        final CloudKubernetesClient client = mock(CloudKubernetesClient.class);

        client.genericResources("group", "v1", "Kind", "kinds");
        client.genericClusterResources("group", "v1", "Kind", "kinds");

        verify(client).genericResources("group", "v1", "Kind", "kinds");
        verify(client).genericClusterResources("group", "v1", "Kind", "kinds");
    }

    @Test
    void shouldDefineDynamicMethod() {
        final CloudKubernetesClient client = mock(CloudKubernetesClient.class);

        client.dynamic();

        verify(client).dynamic();
    }

    @Test
    void shouldDefineGetKubernetesClientMethod() {
        final CloudKubernetesClient client = mock(CloudKubernetesClient.class);

        client.getKubernetesClient();

        verify(client).getKubernetesClient();
    }

    @Test
    void shouldDefineCloseMethod() {
        final CloudKubernetesClient client = mock(CloudKubernetesClient.class);

        client.close();

        verify(client).close();
    }

    @Test
    void aggregateShouldReturnResourceAggregator() {
        final CloudKubernetesClient client = mock(CloudKubernetesClient.class,
                org.mockito.Mockito.withSettings().defaultAnswer(org.mockito.Mockito.CALLS_REAL_METHODS));

        final ResourceAggregator aggregator = client.aggregate();

        assertThat(aggregator).isNotNull();
    }
}
