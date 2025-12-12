package io.elev8.resources.metrics;

import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PodMetricsTest {

    @Test
    void shouldHaveCorrectApiVersionAndKind() {
        final PodMetrics podMetrics = new PodMetrics();

        assertThat(podMetrics.getApiVersion()).isEqualTo("metrics.k8s.io/v1beta1");
        assertThat(podMetrics.getKind()).isEqualTo("PodMetrics");
    }

    @Test
    void shouldBuildWithAllFields() {
        final Instant now = Instant.now();
        final ContainerMetrics container = ContainerMetrics.builder()
                .name("nginx")
                .usage(ResourceUsage.builder().cpu("100m").memory("256Mi").build())
                .build();

        final PodMetrics podMetrics = PodMetrics.builder()
                .name("nginx-deployment-abc123")
                .namespace("default")
                .timestamp(now)
                .window("30s")
                .containers(List.of(container))
                .build();

        assertThat(podMetrics.getName()).isEqualTo("nginx-deployment-abc123");
        assertThat(podMetrics.getNamespace()).isEqualTo("default");
        assertThat(podMetrics.getTimestamp()).isEqualTo(now);
        assertThat(podMetrics.getWindow()).isEqualTo("30s");
        assertThat(podMetrics.getContainers()).hasSize(1);
        assertThat(podMetrics.getContainers().get(0).getName()).isEqualTo("nginx");
    }

    @Test
    void shouldBuildWithMetadataObject() {
        final Metadata metadata = Metadata.builder()
                .name("test-pod")
                .namespace("test-namespace")
                .build();

        final PodMetrics podMetrics = PodMetrics.builder()
                .metadata(metadata)
                .window("1m")
                .build();

        assertThat(podMetrics.getName()).isEqualTo("test-pod");
        assertThat(podMetrics.getNamespace()).isEqualTo("test-namespace");
        assertThat(podMetrics.getWindow()).isEqualTo("1m");
    }

    @Test
    void shouldBuildWithMultipleContainers() {
        final ContainerMetrics container1 = ContainerMetrics.builder()
                .name("app")
                .usage(ResourceUsage.builder().cpu("200m").memory("512Mi").build())
                .build();

        final ContainerMetrics container2 = ContainerMetrics.builder()
                .name("sidecar")
                .usage(ResourceUsage.builder().cpu("50m").memory("64Mi").build())
                .build();

        final PodMetrics podMetrics = PodMetrics.builder()
                .name("multi-container-pod")
                .namespace("default")
                .containers(List.of(container1, container2))
                .build();

        assertThat(podMetrics.getContainers()).hasSize(2);
        assertThat(podMetrics.getContainers().get(0).getName()).isEqualTo("app");
        assertThat(podMetrics.getContainers().get(1).getName()).isEqualTo("sidecar");
    }

    @Test
    void shouldSerializeToJson() {
        final PodMetrics podMetrics = PodMetrics.builder()
                .name("test-pod")
                .namespace("default")
                .window("30s")
                .containers(List.of(
                        ContainerMetrics.builder()
                                .name("nginx")
                                .usage(ResourceUsage.builder().cpu("100m").memory("256Mi").build())
                                .build()
                ))
                .build();

        final String json = podMetrics.toJson();

        assertThat(json).contains("\"apiVersion\":\"metrics.k8s.io/v1beta1\"");
        assertThat(json).contains("\"kind\":\"PodMetrics\"");
        assertThat(json).contains("\"name\":\"test-pod\"");
        assertThat(json).contains("\"namespace\":\"default\"");
        assertThat(json).contains("\"window\":\"30s\"");
        assertThat(json).contains("\"containers\"");
    }

    @Test
    void shouldDeserializeFromJson() {
        final String json = """
                {
                  "apiVersion": "metrics.k8s.io/v1beta1",
                  "kind": "PodMetrics",
                  "metadata": {
                    "name": "nginx-deployment-abc123",
                    "namespace": "default"
                  },
                  "timestamp": "2024-01-15T10:30:00Z",
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

        final PodMetrics podMetrics = AbstractResource.fromJson(json, PodMetrics.class);

        assertThat(podMetrics.getApiVersion()).isEqualTo("metrics.k8s.io/v1beta1");
        assertThat(podMetrics.getKind()).isEqualTo("PodMetrics");
        assertThat(podMetrics.getName()).isEqualTo("nginx-deployment-abc123");
        assertThat(podMetrics.getNamespace()).isEqualTo("default");
        assertThat(podMetrics.getWindow()).isEqualTo("30s");
        assertThat(podMetrics.getContainers()).hasSize(1);
        assertThat(podMetrics.getContainers().get(0).getName()).isEqualTo("nginx");
        assertThat(podMetrics.getContainers().get(0).getUsage().getCpu()).isEqualTo("100m");
        assertThat(podMetrics.getContainers().get(0).getUsage().getMemory()).isEqualTo("256Mi");
    }

    @Test
    void shouldHandleEmptyContainersList() {
        final PodMetrics podMetrics = PodMetrics.builder()
                .name("empty-pod")
                .namespace("default")
                .containers(List.of())
                .build();

        assertThat(podMetrics.getContainers()).isEmpty();
    }

    @Test
    void shouldBuildWithoutOptionalFields() {
        final PodMetrics podMetrics = PodMetrics.builder()
                .name("minimal-pod")
                .namespace("default")
                .build();

        assertThat(podMetrics.getName()).isEqualTo("minimal-pod");
        assertThat(podMetrics.getNamespace()).isEqualTo("default");
        assertThat(podMetrics.getTimestamp()).isNull();
        assertThat(podMetrics.getWindow()).isNull();
        assertThat(podMetrics.getContainers()).isNull();
    }
}
