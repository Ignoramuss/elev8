package io.elev8.resources.metrics;

import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class NodeMetricsTest {

    @Test
    void shouldHaveCorrectApiVersionAndKind() {
        final NodeMetrics nodeMetrics = new NodeMetrics();

        assertThat(nodeMetrics.getApiVersion()).isEqualTo("metrics.k8s.io/v1beta1");
        assertThat(nodeMetrics.getKind()).isEqualTo("NodeMetrics");
    }

    @Test
    void shouldBuildWithAllFields() {
        final Instant now = Instant.now();
        final ResourceUsage usage = ResourceUsage.builder()
                .cpu("500m")
                .memory("4Gi")
                .build();

        final NodeMetrics nodeMetrics = NodeMetrics.builder()
                .name("ip-192-168-1-100.ec2.internal")
                .timestamp(now)
                .window("30s")
                .usage(usage)
                .build();

        assertThat(nodeMetrics.getName()).isEqualTo("ip-192-168-1-100.ec2.internal");
        assertThat(nodeMetrics.getTimestamp()).isEqualTo(now);
        assertThat(nodeMetrics.getWindow()).isEqualTo("30s");
        assertThat(nodeMetrics.getUsage()).isNotNull();
        assertThat(nodeMetrics.getUsage().getCpu()).isEqualTo("500m");
        assertThat(nodeMetrics.getUsage().getMemory()).isEqualTo("4Gi");
    }

    @Test
    void shouldBuildWithMetadataObject() {
        final Metadata metadata = Metadata.builder()
                .name("worker-node-1")
                .build();

        final NodeMetrics nodeMetrics = NodeMetrics.builder()
                .metadata(metadata)
                .window("1m")
                .build();

        assertThat(nodeMetrics.getName()).isEqualTo("worker-node-1");
        assertThat(nodeMetrics.getWindow()).isEqualTo("1m");
    }

    @Test
    void shouldNotHaveNamespace() {
        final NodeMetrics nodeMetrics = NodeMetrics.builder()
                .name("node-1")
                .build();

        assertThat(nodeMetrics.getNamespace()).isNull();
    }

    @Test
    void shouldSerializeToJson() {
        final NodeMetrics nodeMetrics = NodeMetrics.builder()
                .name("test-node")
                .window("30s")
                .usage(ResourceUsage.builder().cpu("2").memory("8Gi").build())
                .build();

        final String json = nodeMetrics.toJson();

        assertThat(json).contains("\"apiVersion\":\"metrics.k8s.io/v1beta1\"");
        assertThat(json).contains("\"kind\":\"NodeMetrics\"");
        assertThat(json).contains("\"name\":\"test-node\"");
        assertThat(json).contains("\"window\":\"30s\"");
        assertThat(json).contains("\"usage\"");
        assertThat(json).contains("\"cpu\":\"2\"");
        assertThat(json).contains("\"memory\":\"8Gi\"");
    }

    @Test
    void shouldDeserializeFromJson() {
        final String json = """
                {
                  "apiVersion": "metrics.k8s.io/v1beta1",
                  "kind": "NodeMetrics",
                  "metadata": {
                    "name": "ip-192-168-1-100.ec2.internal"
                  },
                  "timestamp": "2024-01-15T10:30:00Z",
                  "window": "30s",
                  "usage": {
                    "cpu": "500m",
                    "memory": "4Gi"
                  }
                }
                """;

        final NodeMetrics nodeMetrics = AbstractResource.fromJson(json, NodeMetrics.class);

        assertThat(nodeMetrics.getApiVersion()).isEqualTo("metrics.k8s.io/v1beta1");
        assertThat(nodeMetrics.getKind()).isEqualTo("NodeMetrics");
        assertThat(nodeMetrics.getName()).isEqualTo("ip-192-168-1-100.ec2.internal");
        assertThat(nodeMetrics.getWindow()).isEqualTo("30s");
        assertThat(nodeMetrics.getUsage()).isNotNull();
        assertThat(nodeMetrics.getUsage().getCpu()).isEqualTo("500m");
        assertThat(nodeMetrics.getUsage().getMemory()).isEqualTo("4Gi");
    }

    @Test
    void shouldBuildWithoutOptionalFields() {
        final NodeMetrics nodeMetrics = NodeMetrics.builder()
                .name("minimal-node")
                .build();

        assertThat(nodeMetrics.getName()).isEqualTo("minimal-node");
        assertThat(nodeMetrics.getTimestamp()).isNull();
        assertThat(nodeMetrics.getWindow()).isNull();
        assertThat(nodeMetrics.getUsage()).isNull();
    }

    @Test
    void shouldHandleHighResourceUsage() {
        final NodeMetrics nodeMetrics = NodeMetrics.builder()
                .name("high-capacity-node")
                .usage(ResourceUsage.builder()
                        .cpu("32")
                        .memory("128Gi")
                        .build())
                .build();

        assertThat(nodeMetrics.getUsage().getCpu()).isEqualTo("32");
        assertThat(nodeMetrics.getUsage().getMemory()).isEqualTo("128Gi");
    }
}
