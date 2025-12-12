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

class NodeMetricsManagerTest {

    private static final int METHOD_NOT_ALLOWED = 405;

    private KubernetesClient mockClient;
    private NodeMetricsManager manager;

    @BeforeEach
    void setUp() {
        mockClient = mock(KubernetesClient.class);
        manager = new NodeMetricsManager(mockClient);
    }

    @Test
    void shouldInitializeWithCorrectApiPath() {
        assertThat(manager.getApiPath()).isEqualTo("/apis/metrics.k8s.io/v1beta1");
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        assertThat(manager.getResourceTypePlural()).isEqualTo("nodes");
    }

    @Test
    void shouldCallCorrectPathForList() throws Exception {
        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.getStatusCode()).thenReturn(500);
        when(mockResponse.getBody()).thenReturn("Error");
        when(mockClient.get("/apis/metrics.k8s.io/v1beta1/nodes")).thenReturn(mockResponse);

        assertThatThrownBy(() -> manager.list())
                .isInstanceOf(ResourceException.class);

        verify(mockClient).get("/apis/metrics.k8s.io/v1beta1/nodes");
    }

    @Test
    void shouldGetNodeMetrics() throws Exception {
        final String resourceJson = """
                {
                  "apiVersion": "metrics.k8s.io/v1beta1",
                  "kind": "NodeMetrics",
                  "metadata": {
                    "name": "ip-192-168-1-100.ec2.internal"
                  },
                  "window": "30s",
                  "usage": {
                    "cpu": "2",
                    "memory": "8Gi"
                  }
                }
                """;

        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.getBody()).thenReturn(resourceJson);
        when(mockClient.get("/apis/metrics.k8s.io/v1beta1/nodes/ip-192-168-1-100.ec2.internal"))
                .thenReturn(mockResponse);

        final NodeMetrics nodeMetrics = manager.get("ip-192-168-1-100.ec2.internal");

        assertThat(nodeMetrics).isNotNull();
        assertThat(nodeMetrics.getName()).isEqualTo("ip-192-168-1-100.ec2.internal");
        assertThat(nodeMetrics.getUsage()).isNotNull();
        assertThat(nodeMetrics.getUsage().getCpu()).isEqualTo("2");
        assertThat(nodeMetrics.getUsage().getMemory()).isEqualTo("8Gi");
        verify(mockClient).get("/apis/metrics.k8s.io/v1beta1/nodes/ip-192-168-1-100.ec2.internal");
    }

    @Test
    void shouldThrowExceptionWhenNodeNotFound() throws Exception {
        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isNotFound()).thenReturn(true);
        when(mockClient.get("/apis/metrics.k8s.io/v1beta1/nodes/nonexistent"))
                .thenReturn(mockResponse);

        assertThatThrownBy(() -> manager.get("nonexistent"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("Resource not found");

        verify(mockClient).get("/apis/metrics.k8s.io/v1beta1/nodes/nonexistent");
    }

    @Test
    void shouldRejectCreateOperation() {
        final NodeMetrics nodeMetrics = NodeMetrics.builder()
                .name("test-node")
                .build();

        assertThatThrownBy(() -> manager.create(nodeMetrics))
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
        final NodeMetrics nodeMetrics = NodeMetrics.builder()
                .name("test-node")
                .build();

        assertThatThrownBy(() -> manager.update(nodeMetrics))
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
        assertThatThrownBy(() -> manager.delete("test-node"))
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

        assertThatThrownBy(() -> manager.patch("test-node", options, "[]"))
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

        assertThatThrownBy(() -> manager.apply("test-node", options, "{}"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("read-only")
                .hasMessageContaining("apply")
                .satisfies(e -> {
                    final ResourceException re = (ResourceException) e;
                    assertThat(re.getStatusCode()).isEqualTo(METHOD_NOT_ALLOWED);
                });
    }
}
