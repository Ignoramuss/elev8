package io.elev8.resources.metrics;

import io.elev8.core.client.KubernetesClient;
import io.elev8.core.http.HttpResponse;
import io.elev8.core.patch.ApplyOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.patch.PatchType;
import io.elev8.resources.ResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PodMetricsManagerTest {

    private static final int METHOD_NOT_ALLOWED = 405;

    private KubernetesClient mockClient;
    private PodMetricsManager manager;

    @BeforeEach
    void setUp() {
        mockClient = mock(KubernetesClient.class);
        manager = new PodMetricsManager(mockClient);
    }

    @Test
    void shouldInitializeWithCorrectApiPath() {
        assertThat(manager.getApiPath()).isEqualTo("/apis/metrics.k8s.io/v1beta1");
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        assertThat(manager.getResourceTypePlural()).isEqualTo("pods");
    }

    @Test
    void shouldCallCorrectPathForList() throws Exception {
        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.getStatusCode()).thenReturn(500);
        when(mockResponse.getBody()).thenReturn("Error");
        when(mockClient.get("/apis/metrics.k8s.io/v1beta1/namespaces/default/pods")).thenReturn(mockResponse);

        assertThatThrownBy(() -> manager.list("default"))
                .isInstanceOf(ResourceException.class);

        verify(mockClient).get("/apis/metrics.k8s.io/v1beta1/namespaces/default/pods");
    }

    @Test
    void shouldGetPodMetrics() throws Exception {
        final String resourceJson = """
                {
                  "apiVersion": "metrics.k8s.io/v1beta1",
                  "kind": "PodMetrics",
                  "metadata": {
                    "name": "nginx-pod",
                    "namespace": "default"
                  },
                  "window": "30s",
                  "containers": [
                    {
                      "name": "nginx",
                      "usage": {
                        "cpu": "100m",
                        "memory": "256Mi"
                      }
                    }
                  ]
                }
                """;

        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.getBody()).thenReturn(resourceJson);
        when(mockClient.get("/apis/metrics.k8s.io/v1beta1/namespaces/default/pods/nginx-pod"))
                .thenReturn(mockResponse);

        final PodMetrics podMetrics = manager.get("default", "nginx-pod");

        assertThat(podMetrics).isNotNull();
        assertThat(podMetrics.getName()).isEqualTo("nginx-pod");
        assertThat(podMetrics.getNamespace()).isEqualTo("default");
        assertThat(podMetrics.getContainers()).hasSize(1);
        assertThat(podMetrics.getContainers().get(0).getName()).isEqualTo("nginx");
        assertThat(podMetrics.getContainers().get(0).getUsage().getCpu()).isEqualTo("100m");
        verify(mockClient).get("/apis/metrics.k8s.io/v1beta1/namespaces/default/pods/nginx-pod");
    }

    @Test
    void shouldThrowExceptionWhenPodNotFound() throws Exception {
        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isNotFound()).thenReturn(true);
        when(mockClient.get("/apis/metrics.k8s.io/v1beta1/namespaces/default/pods/nonexistent"))
                .thenReturn(mockResponse);

        assertThatThrownBy(() -> manager.get("default", "nonexistent"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("Resource not found");

        verify(mockClient).get("/apis/metrics.k8s.io/v1beta1/namespaces/default/pods/nonexistent");
    }

    @Test
    void shouldRejectCreateOperation() {
        final PodMetrics podMetrics = PodMetrics.builder()
                .name("test-pod")
                .namespace("default")
                .build();

        assertThatThrownBy(() -> manager.create(podMetrics))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("read-only")
                .hasMessageContaining("create")
                .satisfies(e -> {
                    final ResourceException re = (ResourceException) e;
                    assertThat(re.getStatusCode()).isEqualTo(METHOD_NOT_ALLOWED);
                });
    }

    @Test
    void shouldRejectUpdateOperation() {
        final PodMetrics podMetrics = PodMetrics.builder()
                .name("test-pod")
                .namespace("default")
                .build();

        assertThatThrownBy(() -> manager.update(podMetrics))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("read-only")
                .hasMessageContaining("update")
                .satisfies(e -> {
                    final ResourceException re = (ResourceException) e;
                    assertThat(re.getStatusCode()).isEqualTo(METHOD_NOT_ALLOWED);
                });
    }

    @Test
    void shouldRejectDeleteOperation() {
        assertThatThrownBy(() -> manager.delete("default", "test-pod"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("read-only")
                .hasMessageContaining("delete")
                .satisfies(e -> {
                    final ResourceException re = (ResourceException) e;
                    assertThat(re.getStatusCode()).isEqualTo(METHOD_NOT_ALLOWED);
                });
    }

    @Test
    void shouldRejectPatchOperation() {
        final PatchOptions options = PatchOptions.builder()
                .patchType(PatchType.JSON_PATCH)
                .build();

        assertThatThrownBy(() -> manager.patch("default", "test-pod", options, "[]"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("read-only")
                .hasMessageContaining("patch")
                .satisfies(e -> {
                    final ResourceException re = (ResourceException) e;
                    assertThat(re.getStatusCode()).isEqualTo(METHOD_NOT_ALLOWED);
                });
    }

    @Test
    void shouldRejectApplyOperation() {
        final ApplyOptions options = ApplyOptions.builder()
                .fieldManager("test-manager")
                .build();

        assertThatThrownBy(() -> manager.apply("default", "test-pod", options, "{}"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("read-only")
                .hasMessageContaining("apply")
                .satisfies(e -> {
                    final ResourceException re = (ResourceException) e;
                    assertThat(re.getStatusCode()).isEqualTo(METHOD_NOT_ALLOWED);
                });
    }
}
