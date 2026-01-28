package io.elev8.core.discovery;

import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientException;
import io.elev8.core.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class DefaultDiscoveryClientTest {

    @Mock
    private KubernetesClient kubernetesClient;

    private DefaultDiscoveryClient discoveryClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        discoveryClient = new DefaultDiscoveryClient(kubernetesClient);
    }

    private HttpResponse successResponse(final String body) {
        return new HttpResponse(200, body, Map.of());
    }

    private HttpResponse errorResponse(final int statusCode, final String body) {
        return new HttpResponse(statusCode, body, Map.of());
    }

    @Test
    void shouldGetServerGroups() throws Exception {
        final String response = """
                {
                    "kind": "APIGroupList",
                    "apiVersion": "v1",
                    "groups": [
                        {
                            "name": "apps",
                            "versions": [{"groupVersion": "apps/v1", "version": "v1"}],
                            "preferredVersion": {"groupVersion": "apps/v1", "version": "v1"}
                        },
                        {
                            "name": "batch",
                            "versions": [{"groupVersion": "batch/v1", "version": "v1"}],
                            "preferredVersion": {"groupVersion": "batch/v1", "version": "v1"}
                        }
                    ]
                }
                """;
        when(kubernetesClient.get("/apis")).thenReturn(successResponse(response));

        final APIGroupList groups = discoveryClient.getServerGroups();

        assertThat(groups.getGroups()).hasSize(2);
        assertThat(groups.findByName("apps")).isPresent();
        assertThat(groups.findByName("batch")).isPresent();
    }

    @Test
    void shouldThrowOnFailedGroupsFetch() throws Exception {
        when(kubernetesClient.get("/apis")).thenReturn(errorResponse(500, "Internal Server Error"));

        assertThatThrownBy(() -> discoveryClient.getServerGroups())
                .isInstanceOf(DiscoveryException.class)
                .hasMessageContaining("Failed to fetch API groups");
    }

    @Test
    void shouldThrowOnClientException() throws Exception {
        when(kubernetesClient.get("/apis")).thenThrow(new KubernetesClientException("Connection failed"));

        assertThatThrownBy(() -> discoveryClient.getServerGroups())
                .isInstanceOf(DiscoveryException.class)
                .hasMessageContaining("Failed to fetch API groups");
    }

    @Test
    void shouldGetServerResourcesForCoreApi() throws Exception {
        final String response = """
                {
                    "kind": "APIResourceList",
                    "groupVersion": "v1",
                    "resources": [
                        {"name": "pods", "singularName": "pod", "kind": "Pod", "namespaced": true, "verbs": ["create", "get", "list"]},
                        {"name": "services", "singularName": "service", "kind": "Service", "namespaced": true, "verbs": ["create", "get", "list"]}
                    ]
                }
                """;
        when(kubernetesClient.get("/api/v1")).thenReturn(successResponse(response));

        final APIResourceList resources = discoveryClient.getServerResources("v1");

        assertThat(resources.getGroupVersion()).isEqualTo("v1");
        assertThat(resources.getResources()).hasSize(2);
        assertThat(resources.findByKind("Pod")).isPresent();
        assertThat(resources.findByKind("Pod").get().getGroup()).isEmpty();
        assertThat(resources.findByKind("Pod").get().getVersion()).isEqualTo("v1");
    }

    @Test
    void shouldGetServerResourcesForGroupApi() throws Exception {
        final String response = """
                {
                    "kind": "APIResourceList",
                    "groupVersion": "apps/v1",
                    "resources": [
                        {"name": "deployments", "singularName": "deployment", "kind": "Deployment", "namespaced": true, "verbs": ["create", "get", "list"]}
                    ]
                }
                """;
        when(kubernetesClient.get("/apis/apps/v1")).thenReturn(successResponse(response));

        final APIResourceList resources = discoveryClient.getServerResources("apps/v1");

        assertThat(resources.getGroupVersion()).isEqualTo("apps/v1");
        assertThat(resources.findByKind("Deployment")).isPresent();
        assertThat(resources.findByKind("Deployment").get().getGroup()).isEqualTo("apps");
        assertThat(resources.findByKind("Deployment").get().getVersion()).isEqualTo("v1");
    }

    @Test
    void shouldThrowOnFailedResourcesFetch() throws Exception {
        when(kubernetesClient.get("/api/v1")).thenReturn(errorResponse(404, "Not Found"));

        assertThatThrownBy(() -> discoveryClient.getServerResources("v1"))
                .isInstanceOf(DiscoveryException.class)
                .hasMessageContaining("Failed to fetch API resources");
    }

    @Test
    void shouldGetCoreAPIVersions() throws Exception {
        final String response = """
                {
                    "kind": "APIVersions",
                    "versions": ["v1"]
                }
                """;
        when(kubernetesClient.get("/api")).thenReturn(successResponse(response));

        final List<String> versions = discoveryClient.getCoreAPIVersions();

        assertThat(versions).containsExactly("v1");
    }

    @Test
    void shouldReturnEmptyListWhenNoVersions() throws Exception {
        final String response = """
                {
                    "kind": "APIVersions"
                }
                """;
        when(kubernetesClient.get("/api")).thenReturn(successResponse(response));

        final List<String> versions = discoveryClient.getCoreAPIVersions();

        assertThat(versions).isEmpty();
    }

    @Test
    void shouldFindResourceByKind() throws Exception {
        final String coreResponse = """
                {
                    "kind": "APIVersions",
                    "versions": ["v1"]
                }
                """;
        final String resourcesResponse = """
                {
                    "kind": "APIResourceList",
                    "groupVersion": "v1",
                    "resources": [
                        {"name": "pods", "kind": "Pod", "namespaced": true, "verbs": ["get"]}
                    ]
                }
                """;
        when(kubernetesClient.get("/api")).thenReturn(successResponse(coreResponse));
        when(kubernetesClient.get("/api/v1")).thenReturn(successResponse(resourcesResponse));

        final Optional<APIResource> found = discoveryClient.findResource("Pod");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("pods");
    }

    @Test
    void shouldFindResourceInApiGroup() throws Exception {
        final String coreResponse = """
                {
                    "kind": "APIVersions",
                    "versions": ["v1"]
                }
                """;
        final String coreResourcesResponse = """
                {
                    "kind": "APIResourceList",
                    "groupVersion": "v1",
                    "resources": []
                }
                """;
        final String groupsResponse = """
                {
                    "kind": "APIGroupList",
                    "groups": [
                        {
                            "name": "apps",
                            "versions": [{"groupVersion": "apps/v1", "version": "v1"}],
                            "preferredVersion": {"groupVersion": "apps/v1", "version": "v1"}
                        }
                    ]
                }
                """;
        final String appsResourcesResponse = """
                {
                    "kind": "APIResourceList",
                    "groupVersion": "apps/v1",
                    "resources": [
                        {"name": "deployments", "kind": "Deployment", "namespaced": true, "verbs": ["get"]}
                    ]
                }
                """;
        when(kubernetesClient.get("/api")).thenReturn(successResponse(coreResponse));
        when(kubernetesClient.get("/api/v1")).thenReturn(successResponse(coreResourcesResponse));
        when(kubernetesClient.get("/apis")).thenReturn(successResponse(groupsResponse));
        when(kubernetesClient.get("/apis/apps/v1")).thenReturn(successResponse(appsResourcesResponse));

        final Optional<APIResource> found = discoveryClient.findResource("Deployment");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("deployments");
        assertThat(found.get().getGroup()).isEqualTo("apps");
    }

    @Test
    void shouldReturnEmptyWhenResourceNotFound() throws Exception {
        final String coreResponse = """
                {
                    "kind": "APIVersions",
                    "versions": ["v1"]
                }
                """;
        final String coreResourcesResponse = """
                {
                    "kind": "APIResourceList",
                    "groupVersion": "v1",
                    "resources": []
                }
                """;
        final String groupsResponse = """
                {
                    "kind": "APIGroupList",
                    "groups": []
                }
                """;
        when(kubernetesClient.get("/api")).thenReturn(successResponse(coreResponse));
        when(kubernetesClient.get("/api/v1")).thenReturn(successResponse(coreResourcesResponse));
        when(kubernetesClient.get("/apis")).thenReturn(successResponse(groupsResponse));

        final Optional<APIResource> found = discoveryClient.findResource("NotExists");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindResourceByGroupVersionKind() throws Exception {
        final String response = """
                {
                    "kind": "APIResourceList",
                    "groupVersion": "stable.example.com/v1",
                    "resources": [
                        {"name": "crontabs", "kind": "CronTab", "namespaced": true, "verbs": ["get"]}
                    ]
                }
                """;
        when(kubernetesClient.get("/apis/stable.example.com/v1")).thenReturn(successResponse(response));

        final Optional<APIResource> found = discoveryClient.findResource("stable.example.com", "v1", "CronTab");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("crontabs");
    }

    @Test
    void shouldFindCoreResourceByGroupVersionKind() throws Exception {
        final String response = """
                {
                    "kind": "APIResourceList",
                    "groupVersion": "v1",
                    "resources": [
                        {"name": "pods", "kind": "Pod", "namespaced": true, "verbs": ["get"]}
                    ]
                }
                """;
        when(kubernetesClient.get("/api/v1")).thenReturn(successResponse(response));

        final Optional<APIResource> found = discoveryClient.findResource("", "v1", "Pod");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("pods");
    }

    @Test
    void shouldCheckResourceAvailability() throws Exception {
        final String response = """
                {
                    "kind": "APIResourceList",
                    "groupVersion": "apps/v1",
                    "resources": [
                        {"name": "deployments", "kind": "Deployment", "namespaced": true, "verbs": ["get"]}
                    ]
                }
                """;
        when(kubernetesClient.get("/apis/apps/v1")).thenReturn(successResponse(response));

        assertThat(discoveryClient.isResourceAvailable("apps", "v1", "Deployment")).isTrue();
        assertThat(discoveryClient.isResourceAvailable("apps", "v1", "NotExists")).isFalse();
    }

    @Test
    void shouldGetPreferredResources() throws Exception {
        final String coreVersionsResponse = """
                {
                    "kind": "APIVersions",
                    "versions": ["v1"]
                }
                """;
        final String coreResourcesResponse = """
                {
                    "kind": "APIResourceList",
                    "groupVersion": "v1",
                    "resources": [
                        {"name": "pods", "kind": "Pod", "namespaced": true, "verbs": ["get"]}
                    ]
                }
                """;
        final String groupsResponse = """
                {
                    "kind": "APIGroupList",
                    "groups": [
                        {
                            "name": "apps",
                            "versions": [{"groupVersion": "apps/v1", "version": "v1"}],
                            "preferredVersion": {"groupVersion": "apps/v1", "version": "v1"}
                        }
                    ]
                }
                """;
        final String appsResourcesResponse = """
                {
                    "kind": "APIResourceList",
                    "groupVersion": "apps/v1",
                    "resources": [
                        {"name": "deployments", "kind": "Deployment", "namespaced": true, "verbs": ["get"]}
                    ]
                }
                """;
        when(kubernetesClient.get("/api")).thenReturn(successResponse(coreVersionsResponse));
        when(kubernetesClient.get("/api/v1")).thenReturn(successResponse(coreResourcesResponse));
        when(kubernetesClient.get("/apis")).thenReturn(successResponse(groupsResponse));
        when(kubernetesClient.get("/apis/apps/v1")).thenReturn(successResponse(appsResourcesResponse));

        final List<APIResource> resources = discoveryClient.getPreferredResources();

        assertThat(resources).hasSize(2);
        assertThat(resources.stream().map(APIResource::getKind))
                .containsExactlyInAnyOrder("Pod", "Deployment");
    }

    @Test
    void shouldHandleInvalidJsonInGroupsResponse() throws Exception {
        when(kubernetesClient.get("/apis")).thenReturn(successResponse("not json"));

        assertThatThrownBy(() -> discoveryClient.getServerGroups())
                .isInstanceOf(DiscoveryException.class)
                .hasMessageContaining("Failed to parse");
    }

    @Test
    void invalidateCacheShouldNotThrow() {
        discoveryClient.invalidateCache();
    }
}
