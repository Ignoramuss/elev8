package io.elev8.resources.generic;

import io.elev8.core.client.KubernetesClient;
import io.elev8.core.http.HttpResponse;
import io.elev8.core.list.ListOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.patch.PatchType;
import io.elev8.core.patch.ApplyOptions;
import io.elev8.resources.ResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenericClusterResourceManagerTest {

    private KubernetesClient client;
    private GenericClusterResourceManager manager;
    private GenericResourceContext context;

    @BeforeEach
    void setUp() {
        client = Mockito.mock(KubernetesClient.class);
        context = GenericResourceContext.forClusterResource(
                "example.com", "v1", "ClusterPolicy", "clusterpolicies");
        manager = new GenericClusterResourceManager(client, context);
    }

    @Test
    void shouldInitializeWithContext() {
        assertThat(manager).isNotNull();
        assertThat(manager.getContext()).isEqualTo(context);
        assertThat(manager.getApiPath()).isEqualTo("/apis/example.com/v1");
    }

    @Test
    void shouldInitializeWithInlineParameters() {
        final GenericClusterResourceManager inlineManager = new GenericClusterResourceManager(
                client, "example.com", "v1", "ClusterPolicy", "clusterpolicies");

        assertThat(inlineManager).isNotNull();
        assertThat(inlineManager.getContext().getGroup()).isEqualTo("example.com");
        assertThat(inlineManager.getContext().getKind()).isEqualTo("ClusterPolicy");
    }

    @Test
    void shouldRejectNamespaceScopedContext() {
        final GenericResourceContext namespacedContext = GenericResourceContext.forNamespacedResource(
                "stable.example.com", "v1", "CronTab", "crontabs");

        assertThatThrownBy(() -> new GenericClusterResourceManager(client, namespacedContext))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cluster-scoped");
    }

    @Test
    void shouldListClusterResources() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "example.com/v1",
                    "kind": "ClusterPolicyList",
                    "items": [
                        {
                            "apiVersion": "example.com/v1",
                            "kind": "ClusterPolicy",
                            "metadata": {"name": "policy1"},
                            "spec": {"enforce": true}
                        },
                        {
                            "apiVersion": "example.com/v1",
                            "kind": "ClusterPolicy",
                            "metadata": {"name": "policy2"},
                            "spec": {"enforce": false}
                        }
                    ]
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseBody);
        when(client.get("/apis/example.com/v1/clusterpolicies")).thenReturn(response);

        final List<GenericKubernetesResource> resources = manager.list();

        assertThat(resources).hasSize(2);
        assertThat(resources.get(0).getName()).isEqualTo("policy1");
        assertThat(resources.get(1).getName()).isEqualTo("policy2");
    }

    @Test
    void shouldListClusterResourcesWithOptions() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "example.com/v1",
                    "kind": "ClusterPolicyList",
                    "items": [
                        {
                            "apiVersion": "example.com/v1",
                            "kind": "ClusterPolicy",
                            "metadata": {"name": "policy1"},
                            "spec": {"enforce": true}
                        }
                    ]
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseBody);

        final ListOptions options = ListOptions.withFieldSelector("metadata.name=policy1");
        when(client.get("/apis/example.com/v1/clusterpolicies", options)).thenReturn(response);

        final List<GenericKubernetesResource> resources = manager.list(options);

        assertThat(resources).hasSize(1);
        assertThat(resources.get(0).getName()).isEqualTo("policy1");
        verify(client).get("/apis/example.com/v1/clusterpolicies", options);
    }

    @Test
    void shouldGetClusterResourceByName() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "example.com/v1",
                    "kind": "ClusterPolicy",
                    "metadata": {"name": "my-policy"},
                    "spec": {"enforce": true, "rules": ["rule1", "rule2"]}
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.isNotFound()).thenReturn(false);
        when(response.getBody()).thenReturn(responseBody);
        when(client.get("/apis/example.com/v1/clusterpolicies/my-policy")).thenReturn(response);

        final GenericKubernetesResource resource = manager.get("my-policy");

        assertThat(resource.getName()).isEqualTo("my-policy");
        assertThat(resource.getSpec("enforce")).isEqualTo(true);
    }

    @Test
    void shouldThrowExceptionWhenClusterResourceNotFound() throws Exception {
        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isNotFound()).thenReturn(true);
        when(client.get("/apis/example.com/v1/clusterpolicies/missing")).thenReturn(response);

        assertThatThrownBy(() -> manager.get("missing"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldCreateClusterResource() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "example.com/v1",
                    "kind": "ClusterPolicy",
                    "metadata": {"name": "new-policy", "uid": "xyz789"},
                    "spec": {"enforce": true}
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseBody);
        when(client.post(eq("/apis/example.com/v1/clusterpolicies"), any())).thenReturn(response);

        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("example.com/v1")
                .kind("ClusterPolicy")
                .name("new-policy")
                .specField("enforce", true)
                .build();

        final GenericKubernetesResource created = manager.create(resource);

        assertThat(created.getName()).isEqualTo("new-policy");
        assertThat(created.getMetadata().getUid()).isEqualTo("xyz789");
    }

    @Test
    void shouldUpdateClusterResource() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "example.com/v1",
                    "kind": "ClusterPolicy",
                    "metadata": {"name": "my-policy", "resourceVersion": "2"},
                    "spec": {"enforce": false}
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseBody);
        when(client.put(eq("/apis/example.com/v1/clusterpolicies/my-policy"), any())).thenReturn(response);

        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("example.com/v1")
                .kind("ClusterPolicy")
                .name("my-policy")
                .specField("enforce", false)
                .build();

        final GenericKubernetesResource updated = manager.update(resource);

        assertThat(updated.getSpec("enforce")).isEqualTo(false);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithoutName() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("example.com/v1")
                .kind("ClusterPolicy")
                .build();

        assertThatThrownBy(() -> manager.update(resource))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("name is required");
    }

    @Test
    void shouldDeleteClusterResource() throws Exception {
        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.isNotFound()).thenReturn(false);
        when(client.delete("/apis/example.com/v1/clusterpolicies/my-policy")).thenReturn(response);

        manager.delete("my-policy");

        verify(client).delete("/apis/example.com/v1/clusterpolicies/my-policy");
    }

    @Test
    void shouldNotThrowWhenDeletingNonExistentClusterResource() throws Exception {
        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(false);
        when(response.isNotFound()).thenReturn(true);
        when(client.delete("/apis/example.com/v1/clusterpolicies/missing")).thenReturn(response);

        manager.delete("missing");

        verify(client).delete("/apis/example.com/v1/clusterpolicies/missing");
    }

    @Test
    void shouldPatchClusterResource() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "example.com/v1",
                    "kind": "ClusterPolicy",
                    "metadata": {"name": "my-policy"},
                    "spec": {"enforce": true, "level": "strict"}
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseBody);
        when(client.patch(eq("/apis/example.com/v1/clusterpolicies/my-policy"),
                any(PatchOptions.class), any()))
                .thenReturn(response);

        final PatchOptions options = PatchOptions.builder()
                .patchType(PatchType.MERGE_PATCH)
                .build();
        final String patchBody = "{\"spec\":{\"level\":\"strict\"}}";

        final GenericKubernetesResource patched = manager.patch("my-policy", options, patchBody);

        assertThat(patched.getSpec("level")).isEqualTo("strict");
    }

    @Test
    void shouldApplyClusterResource() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "example.com/v1",
                    "kind": "ClusterPolicy",
                    "metadata": {"name": "my-policy"},
                    "spec": {"enforce": true}
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseBody);
        when(client.patch(eq("/apis/example.com/v1/clusterpolicies/my-policy"),
                any(PatchOptions.class), any()))
                .thenReturn(response);

        final ApplyOptions options = ApplyOptions.of("test-manager");
        final String manifest = """
                apiVersion: example.com/v1
                kind: ClusterPolicy
                metadata:
                  name: my-policy
                spec:
                  enforce: true
                """;

        final GenericKubernetesResource applied = manager.apply("my-policy", options, manifest);

        assertThat(applied.getSpec("enforce")).isEqualTo(true);
    }

    @Test
    void shouldThrowExceptionWhenApplyingWithoutOptions() {
        assertThatThrownBy(() -> manager.apply("my-policy", null, "{}"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("ApplyOptions are required");
    }

    @Test
    void shouldWorkWithApiextensionsResources() throws Exception {
        final GenericResourceContext crdContext = GenericResourceContext.forClusterResource(
                "apiextensions.k8s.io", "v1", "CustomResourceDefinition", "customresourcedefinitions");
        final GenericClusterResourceManager crdManager = new GenericClusterResourceManager(client, crdContext);

        final String responseBody = """
                {
                    "apiVersion": "apiextensions.k8s.io/v1",
                    "kind": "CustomResourceDefinition",
                    "metadata": {"name": "crontabs.stable.example.com"},
                    "spec": {"group": "stable.example.com"}
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.isNotFound()).thenReturn(false);
        when(response.getBody()).thenReturn(responseBody);
        when(client.get("/apis/apiextensions.k8s.io/v1/customresourcedefinitions/crontabs.stable.example.com"))
                .thenReturn(response);

        final GenericKubernetesResource resource = crdManager.get("crontabs.stable.example.com");

        assertThat(resource.getApiVersion()).isEqualTo("apiextensions.k8s.io/v1");
        assertThat(resource.getKind()).isEqualTo("CustomResourceDefinition");
    }

    @Test
    void shouldHandleResourceWithNestedSpec() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "example.com/v1",
                    "kind": "ClusterPolicy",
                    "metadata": {"name": "my-policy"},
                    "spec": {
                        "config": {
                            "database": {
                                "host": "localhost",
                                "port": 5432
                            }
                        }
                    }
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.isNotFound()).thenReturn(false);
        when(response.getBody()).thenReturn(responseBody);
        when(client.get("/apis/example.com/v1/clusterpolicies/my-policy")).thenReturn(response);

        final GenericKubernetesResource resource = manager.get("my-policy");

        assertThat(resource.getSpec("config.database.host")).isEqualTo("localhost");
        assertThat(resource.getSpec("config.database.port")).isEqualTo(5432);
    }
}
