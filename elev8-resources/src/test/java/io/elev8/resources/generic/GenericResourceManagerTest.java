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

class GenericResourceManagerTest {

    private KubernetesClient client;
    private GenericResourceManager manager;
    private GenericResourceContext context;

    @BeforeEach
    void setUp() {
        client = Mockito.mock(KubernetesClient.class);
        context = GenericResourceContext.forNamespacedResource(
                "stable.example.com", "v1", "CronTab", "crontabs");
        manager = new GenericResourceManager(client, context);
    }

    @Test
    void shouldInitializeWithContext() {
        assertThat(manager).isNotNull();
        assertThat(manager.getContext()).isEqualTo(context);
        assertThat(manager.getApiPath()).isEqualTo("/apis/stable.example.com/v1");
    }

    @Test
    void shouldInitializeWithInlineParameters() {
        final GenericResourceManager inlineManager = new GenericResourceManager(
                client, "stable.example.com", "v1", "CronTab", "crontabs");

        assertThat(inlineManager).isNotNull();
        assertThat(inlineManager.getContext().getGroup()).isEqualTo("stable.example.com");
        assertThat(inlineManager.getContext().getKind()).isEqualTo("CronTab");
    }

    @Test
    void shouldRejectClusterScopedContext() {
        final GenericResourceContext clusterContext = GenericResourceContext.forClusterResource(
                "example.com", "v1", "ClusterPolicy", "clusterpolicies");

        assertThatThrownBy(() -> new GenericResourceManager(client, clusterContext))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("namespace-scoped");
    }

    @Test
    void shouldListResourcesInNamespace() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "stable.example.com/v1",
                    "kind": "CronTabList",
                    "items": [
                        {
                            "apiVersion": "stable.example.com/v1",
                            "kind": "CronTab",
                            "metadata": {"name": "cron1", "namespace": "default"},
                            "spec": {"cronSpec": "* * * * */5"}
                        },
                        {
                            "apiVersion": "stable.example.com/v1",
                            "kind": "CronTab",
                            "metadata": {"name": "cron2", "namespace": "default"},
                            "spec": {"cronSpec": "0 * * * *"}
                        }
                    ]
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseBody);
        when(client.get("/apis/stable.example.com/v1/namespaces/default/crontabs"))
                .thenReturn(response);

        final List<GenericKubernetesResource> resources = manager.list("default");

        assertThat(resources).hasSize(2);
        assertThat(resources.get(0).getName()).isEqualTo("cron1");
        assertThat(resources.get(1).getName()).isEqualTo("cron2");
    }

    @Test
    void shouldListResourcesAcrossAllNamespaces() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "stable.example.com/v1",
                    "kind": "CronTabList",
                    "items": [
                        {
                            "apiVersion": "stable.example.com/v1",
                            "kind": "CronTab",
                            "metadata": {"name": "cron1", "namespace": "default"}
                        },
                        {
                            "apiVersion": "stable.example.com/v1",
                            "kind": "CronTab",
                            "metadata": {"name": "cron2", "namespace": "kube-system"}
                        }
                    ]
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseBody);
        when(client.get("/apis/stable.example.com/v1/crontabs")).thenReturn(response);

        final List<GenericKubernetesResource> resources = manager.listAllNamespaces();

        assertThat(resources).hasSize(2);
        assertThat(resources.get(0).getNamespace()).isEqualTo("default");
        assertThat(resources.get(1).getNamespace()).isEqualTo("kube-system");
    }

    @Test
    void shouldListResourcesInNamespaceWithOptions() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "stable.example.com/v1",
                    "kind": "CronTabList",
                    "items": [
                        {
                            "apiVersion": "stable.example.com/v1",
                            "kind": "CronTab",
                            "metadata": {"name": "cron1", "namespace": "default"},
                            "spec": {"cronSpec": "* * * * */5"}
                        }
                    ]
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseBody);

        final ListOptions options = ListOptions.withFieldSelector("metadata.name=cron1");
        when(client.get("/apis/stable.example.com/v1/namespaces/default/crontabs", options))
                .thenReturn(response);

        final List<GenericKubernetesResource> resources = manager.list("default", options);

        assertThat(resources).hasSize(1);
        assertThat(resources.get(0).getName()).isEqualTo("cron1");
        verify(client).get("/apis/stable.example.com/v1/namespaces/default/crontabs", options);
    }

    @Test
    void shouldListAllNamespacesWithOptions() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "stable.example.com/v1",
                    "kind": "CronTabList",
                    "items": [
                        {
                            "apiVersion": "stable.example.com/v1",
                            "kind": "CronTab",
                            "metadata": {"name": "cron1", "namespace": "default"}
                        }
                    ]
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseBody);

        final ListOptions options = ListOptions.withLabelSelector("app=myapp");
        when(client.get("/apis/stable.example.com/v1/crontabs", options))
                .thenReturn(response);

        final List<GenericKubernetesResource> resources = manager.listAllNamespaces(options);

        assertThat(resources).hasSize(1);
        verify(client).get("/apis/stable.example.com/v1/crontabs", options);
    }

    @Test
    void shouldGetResourceByName() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "stable.example.com/v1",
                    "kind": "CronTab",
                    "metadata": {"name": "my-cron", "namespace": "default"},
                    "spec": {"cronSpec": "* * * * */5", "replicas": 3}
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.isNotFound()).thenReturn(false);
        when(response.getBody()).thenReturn(responseBody);
        when(client.get("/apis/stable.example.com/v1/namespaces/default/crontabs/my-cron"))
                .thenReturn(response);

        final GenericKubernetesResource resource = manager.get("default", "my-cron");

        assertThat(resource.getName()).isEqualTo("my-cron");
        assertThat(resource.getSpec("cronSpec")).isEqualTo("* * * * */5");
        assertThat(resource.getSpec("replicas")).isEqualTo(3);
    }

    @Test
    void shouldThrowExceptionWhenResourceNotFound() throws Exception {
        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isNotFound()).thenReturn(true);
        when(client.get("/apis/stable.example.com/v1/namespaces/default/crontabs/missing"))
                .thenReturn(response);

        assertThatThrownBy(() -> manager.get("default", "missing"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldCreateResource() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "stable.example.com/v1",
                    "kind": "CronTab",
                    "metadata": {"name": "new-cron", "namespace": "default", "uid": "abc123"},
                    "spec": {"cronSpec": "* * * * */5"}
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseBody);
        when(client.post(eq("/apis/stable.example.com/v1/namespaces/default/crontabs"), any()))
                .thenReturn(response);

        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("new-cron")
                .namespace("default")
                .specField("cronSpec", "* * * * */5")
                .build();

        final GenericKubernetesResource created = manager.create(resource);

        assertThat(created.getName()).isEqualTo("new-cron");
        assertThat(created.getMetadata().getUid()).isEqualTo("abc123");
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithoutNamespace() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("new-cron")
                .build();

        assertThatThrownBy(() -> manager.create(resource))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("namespace is required");
    }

    @Test
    void shouldUpdateResource() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "stable.example.com/v1",
                    "kind": "CronTab",
                    "metadata": {"name": "my-cron", "namespace": "default", "resourceVersion": "2"},
                    "spec": {"cronSpec": "0 * * * *", "replicas": 5}
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseBody);
        when(client.put(eq("/apis/stable.example.com/v1/namespaces/default/crontabs/my-cron"), any()))
                .thenReturn(response);

        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .namespace("default")
                .specField("cronSpec", "0 * * * *")
                .specField("replicas", 5)
                .build();

        final GenericKubernetesResource updated = manager.update(resource);

        assertThat(updated.getSpec("cronSpec")).isEqualTo("0 * * * *");
        assertThat(updated.getSpec("replicas")).isEqualTo(5);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithoutNamespaceOrName() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .build();

        assertThatThrownBy(() -> manager.update(resource))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("namespace and name are required");
    }

    @Test
    void shouldDeleteResource() throws Exception {
        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.isNotFound()).thenReturn(false);
        when(client.delete("/apis/stable.example.com/v1/namespaces/default/crontabs/my-cron"))
                .thenReturn(response);

        manager.delete("default", "my-cron");

        verify(client).delete("/apis/stable.example.com/v1/namespaces/default/crontabs/my-cron");
    }

    @Test
    void shouldNotThrowWhenDeletingNonExistentResource() throws Exception {
        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(false);
        when(response.isNotFound()).thenReturn(true);
        when(client.delete("/apis/stable.example.com/v1/namespaces/default/crontabs/missing"))
                .thenReturn(response);

        manager.delete("default", "missing");

        verify(client).delete("/apis/stable.example.com/v1/namespaces/default/crontabs/missing");
    }

    @Test
    void shouldPatchResource() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "stable.example.com/v1",
                    "kind": "CronTab",
                    "metadata": {"name": "my-cron", "namespace": "default"},
                    "spec": {"cronSpec": "0 0 * * *", "replicas": 3}
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseBody);
        when(client.patch(eq("/apis/stable.example.com/v1/namespaces/default/crontabs/my-cron"),
                any(PatchOptions.class), any()))
                .thenReturn(response);

        final PatchOptions options = PatchOptions.builder()
                .patchType(PatchType.MERGE_PATCH)
                .build();
        final String patchBody = "{\"spec\":{\"cronSpec\":\"0 0 * * *\"}}";

        final GenericKubernetesResource patched = manager.patch("default", "my-cron", options, patchBody);

        assertThat(patched.getSpec("cronSpec")).isEqualTo("0 0 * * *");
    }

    @Test
    void shouldApplyResource() throws Exception {
        final String responseBody = """
                {
                    "apiVersion": "stable.example.com/v1",
                    "kind": "CronTab",
                    "metadata": {"name": "my-cron", "namespace": "default"},
                    "spec": {"cronSpec": "0 0 * * *"}
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseBody);
        when(client.patch(eq("/apis/stable.example.com/v1/namespaces/default/crontabs/my-cron"),
                any(PatchOptions.class), any()))
                .thenReturn(response);

        final ApplyOptions options = ApplyOptions.of("test-manager");
        final String manifest = """
                apiVersion: stable.example.com/v1
                kind: CronTab
                metadata:
                  name: my-cron
                  namespace: default
                spec:
                  cronSpec: "0 0 * * *"
                """;

        final GenericKubernetesResource applied = manager.apply("default", "my-cron", options, manifest);

        assertThat(applied.getSpec("cronSpec")).isEqualTo("0 0 * * *");
    }

    @Test
    void shouldThrowExceptionWhenApplyingWithoutOptions() {
        assertThatThrownBy(() -> manager.apply("default", "my-cron", null, "{}"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("ApplyOptions are required");
    }

    @Test
    void shouldWorkWithCoreApiResources() throws Exception {
        final GenericResourceContext coreContext = GenericResourceContext.forCoreResource(
                "v1", "ConfigMap", "configmaps", GenericResourceContext.ResourceScope.NAMESPACED);
        final GenericResourceManager coreManager = new GenericResourceManager(client, coreContext);

        final String responseBody = """
                {
                    "apiVersion": "v1",
                    "kind": "ConfigMap",
                    "metadata": {"name": "my-config", "namespace": "default"},
                    "data": {"key": "value"}
                }
                """;

        final HttpResponse response = Mockito.mock(HttpResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.isNotFound()).thenReturn(false);
        when(response.getBody()).thenReturn(responseBody);
        when(client.get("/api/v1/namespaces/default/configmaps/my-config")).thenReturn(response);

        final GenericKubernetesResource resource = coreManager.get("default", "my-config");

        assertThat(resource.getApiVersion()).isEqualTo("v1");
        assertThat(resource.getKind()).isEqualTo("ConfigMap");
    }
}
