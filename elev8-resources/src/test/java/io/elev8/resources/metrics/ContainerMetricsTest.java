package io.elev8.resources.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContainerMetricsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildWithNameAndUsage() {
        final ResourceUsage usage = ResourceUsage.builder()
                .cpu("100m")
                .memory("256Mi")
                .build();

        final ContainerMetrics metrics = ContainerMetrics.builder()
                .name("nginx")
                .usage(usage)
                .build();

        assertThat(metrics.getName()).isEqualTo("nginx");
        assertThat(metrics.getUsage()).isNotNull();
        assertThat(metrics.getUsage().getCpu()).isEqualTo("100m");
        assertThat(metrics.getUsage().getMemory()).isEqualTo("256Mi");
    }

    @Test
    void shouldBuildWithNameOnly() {
        final ContainerMetrics metrics = ContainerMetrics.builder()
                .name("app")
                .build();

        assertThat(metrics.getName()).isEqualTo("app");
        assertThat(metrics.getUsage()).isNull();
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final ResourceUsage usage = ResourceUsage.builder()
                .cpu("100m")
                .memory("256Mi")
                .build();

        final ContainerMetrics metrics = ContainerMetrics.builder()
                .name("nginx")
                .usage(usage)
                .build();

        final String json = objectMapper.writeValueAsString(metrics);

        assertThat(json).contains("\"name\":\"nginx\"");
        assertThat(json).contains("\"usage\"");
        assertThat(json).contains("\"cpu\":\"100m\"");
        assertThat(json).contains("\"memory\":\"256Mi\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "name": "app-container",
                  "usage": {
                    "cpu": "50m",
                    "memory": "128Mi"
                  }
                }
                """;

        final ContainerMetrics metrics = objectMapper.readValue(json, ContainerMetrics.class);

        assertThat(metrics.getName()).isEqualTo("app-container");
        assertThat(metrics.getUsage()).isNotNull();
        assertThat(metrics.getUsage().getCpu()).isEqualTo("50m");
        assertThat(metrics.getUsage().getMemory()).isEqualTo("128Mi");
    }

    @Test
    void shouldOmitNullValuesInJson() throws Exception {
        final ContainerMetrics metrics = ContainerMetrics.builder()
                .name("sidecar")
                .build();

        final String json = objectMapper.writeValueAsString(metrics);

        assertThat(json).contains("\"name\":\"sidecar\"");
        assertThat(json).doesNotContain("usage");
    }
}
