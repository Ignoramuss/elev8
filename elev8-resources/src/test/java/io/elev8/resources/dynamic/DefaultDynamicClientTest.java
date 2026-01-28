package io.elev8.resources.dynamic;

import io.elev8.core.client.KubernetesClient;
import io.elev8.core.discovery.APIResource;
import io.elev8.core.discovery.DiscoveryClient;
import io.elev8.core.discovery.DiscoveryException;
import io.elev8.resources.generic.GenericClusterResourceManager;
import io.elev8.resources.generic.GenericResourceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultDynamicClientTest {

    @Mock
    private KubernetesClient kubernetesClient;

    @Mock
    private DiscoveryClient discoveryClient;

    private DefaultDynamicClient dynamicClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dynamicClient = new DefaultDynamicClient(kubernetesClient, discoveryClient);
    }

    @Test
    void shouldReturnDiscoveryClient() {
        assertThat(dynamicClient.discovery()).isSameAs(discoveryClient);
    }

    @Test
    void shouldCreateResourceManager() {
        final GenericResourceManager manager = dynamicClient.resources("apps", "v1", "deployments");

        assertThat(manager).isNotNull();
        assertThat(manager.getContext().getGroup()).isEqualTo("apps");
        assertThat(manager.getContext().getVersion()).isEqualTo("v1");
        assertThat(manager.getContext().getPlural()).isEqualTo("deployments");
    }

    @Test
    void shouldCacheResourceManagers() {
        final GenericResourceManager first = dynamicClient.resources("apps", "v1", "deployments");
        final GenericResourceManager second = dynamicClient.resources("apps", "v1", "deployments");

        assertThat(first).isSameAs(second);
    }

    @Test
    void shouldCreateDifferentManagersForDifferentResources() {
        final GenericResourceManager deployments = dynamicClient.resources("apps", "v1", "deployments");
        final GenericResourceManager statefulsets = dynamicClient.resources("apps", "v1", "statefulsets");

        assertThat(deployments).isNotSameAs(statefulsets);
    }

    @Test
    void shouldCreateClusterResourceManager() {
        final GenericClusterResourceManager manager = dynamicClient.clusterResources("", "v1", "nodes");

        assertThat(manager).isNotNull();
        assertThat(manager.getContext().getPlural()).isEqualTo("nodes");
    }

    @Test
    void shouldCacheClusterResourceManagers() {
        final GenericClusterResourceManager first = dynamicClient.clusterResources("", "v1", "nodes");
        final GenericClusterResourceManager second = dynamicClient.clusterResources("", "v1", "nodes");

        assertThat(first).isSameAs(second);
    }

    @Test
    void shouldResourcesForKindReturnManager() throws Exception {
        final APIResource podResource = APIResource.builder()
                .name("pods")
                .kind("Pod")
                .group("")
                .version("v1")
                .namespaced(true)
                .build();
        when(discoveryClient.findResource("Pod")).thenReturn(Optional.of(podResource));

        final GenericResourceManager manager = dynamicClient.resourcesForKind("Pod");

        assertThat(manager).isNotNull();
        assertThat(manager.getContext().getPlural()).isEqualTo("pods");
        assertThat(manager.getContext().getVersion()).isEqualTo("v1");
    }

    @Test
    void shouldResourcesForKindThrowWhenNotFound() throws Exception {
        when(discoveryClient.findResource("NotExists")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dynamicClient.resourcesForKind("NotExists"))
                .isInstanceOf(DiscoveryException.class)
                .hasMessageContaining("Resource kind not found");
    }

    @Test
    void shouldResourcesForKindThrowWhenClusterScoped() throws Exception {
        final APIResource nodeResource = APIResource.builder()
                .name("nodes")
                .kind("Node")
                .group("")
                .version("v1")
                .namespaced(false)
                .build();
        when(discoveryClient.findResource("Node")).thenReturn(Optional.of(nodeResource));

        assertThatThrownBy(() -> dynamicClient.resourcesForKind("Node"))
                .isInstanceOf(DiscoveryException.class)
                .hasMessageContaining("cluster-scoped");
    }

    @Test
    void shouldClusterResourcesForKindReturnManager() throws Exception {
        final APIResource nodeResource = APIResource.builder()
                .name("nodes")
                .kind("Node")
                .group("")
                .version("v1")
                .namespaced(false)
                .build();
        when(discoveryClient.findResource("Node")).thenReturn(Optional.of(nodeResource));

        final GenericClusterResourceManager manager = dynamicClient.clusterResourcesForKind("Node");

        assertThat(manager).isNotNull();
        assertThat(manager.getContext().getPlural()).isEqualTo("nodes");
    }

    @Test
    void shouldClusterResourcesForKindThrowWhenNamespaceScoped() throws Exception {
        final APIResource podResource = APIResource.builder()
                .name("pods")
                .kind("Pod")
                .group("")
                .version("v1")
                .namespaced(true)
                .build();
        when(discoveryClient.findResource("Pod")).thenReturn(Optional.of(podResource));

        assertThatThrownBy(() -> dynamicClient.clusterResourcesForKind("Pod"))
                .isInstanceOf(DiscoveryException.class)
                .hasMessageContaining("namespace-scoped");
    }

    @Test
    void shouldHandleCoreApiResources() {
        final GenericResourceManager manager = dynamicClient.resources("", "v1", "pods");

        assertThat(manager.getContext().getGroup()).isEmpty();
        assertThat(manager.getContext().getApiPath()).isEqualTo("/api/v1");
    }

    @Test
    void shouldHandleGroupApiResources() {
        final GenericResourceManager manager = dynamicClient.resources("stable.example.com", "v1", "crontabs");

        assertThat(manager.getContext().getGroup()).isEqualTo("stable.example.com");
        assertThat(manager.getContext().getApiPath()).isEqualTo("/apis/stable.example.com/v1");
    }

    @Test
    void shouldHandleNullGroup() {
        final GenericResourceManager manager = dynamicClient.resources(null, "v1", "pods");

        assertThat(manager.getContext().getGroup()).isNull();
        assertThat(manager.getContext().getApiPath()).isEqualTo("/api/v1");
    }

    @Test
    void shouldResourcesForKindFindCustomResources() throws Exception {
        final APIResource crontabResource = APIResource.builder()
                .name("crontabs")
                .kind("CronTab")
                .group("stable.example.com")
                .version("v1")
                .namespaced(true)
                .build();
        when(discoveryClient.findResource("CronTab")).thenReturn(Optional.of(crontabResource));

        final GenericResourceManager manager = dynamicClient.resourcesForKind("CronTab");

        assertThat(manager.getContext().getGroup()).isEqualTo("stable.example.com");
        assertThat(manager.getContext().getPlural()).isEqualTo("crontabs");
    }
}
