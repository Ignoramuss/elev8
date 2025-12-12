package io.elev8.resources.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceUsageTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildWithCpuAndMemory() {
        final ResourceUsage usage = ResourceUsage.builder()
                .cpu("100m")
                .memory("256Mi")
                .build();

        assertThat(usage.getCpu()).isEqualTo("100m");
        assertThat(usage.getMemory()).isEqualTo("256Mi");
    }

    @Test
    void shouldBuildWithCpuOnly() {
        final ResourceUsage usage = ResourceUsage.builder()
                .cpu("500m")
                .build();

        assertThat(usage.getCpu()).isEqualTo("500m");
        assertThat(usage.getMemory()).isNull();
    }

    @Test
    void shouldBuildWithMemoryOnly() {
        final ResourceUsage usage = ResourceUsage.builder()
                .memory("1Gi")
                .build();

        assertThat(usage.getCpu()).isNull();
        assertThat(usage.getMemory()).isEqualTo("1Gi");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final ResourceUsage usage = ResourceUsage.builder()
                .cpu("100m")
                .memory("256Mi")
                .build();

        final String json = objectMapper.writeValueAsString(usage);

        assertThat(json).contains("\"cpu\":\"100m\"");
        assertThat(json).contains("\"memory\":\"256Mi\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "cpu": "200m",
                  "memory": "512Mi"
                }
                """;

        final ResourceUsage usage = objectMapper.readValue(json, ResourceUsage.class);

        assertThat(usage.getCpu()).isEqualTo("200m");
        assertThat(usage.getMemory()).isEqualTo("512Mi");
    }

    @Test
    void shouldOmitNullValuesInJson() throws Exception {
        final ResourceUsage usage = ResourceUsage.builder()
                .cpu("100m")
                .build();

        final String json = objectMapper.writeValueAsString(usage);

        assertThat(json).contains("\"cpu\":\"100m\"");
        assertThat(json).doesNotContain("memory");
    }

    @Test
    void shouldHandleWholeCoreCpuValues() {
        final ResourceUsage usage = ResourceUsage.builder()
                .cpu("2")
                .memory("4Gi")
                .build();

        assertThat(usage.getCpu()).isEqualTo("2");
        assertThat(usage.getMemory()).isEqualTo("4Gi");
    }

    @Test
    void shouldHandleNanocoreCpuValues() {
        final ResourceUsage usage = ResourceUsage.builder()
                .cpu("123456789n")
                .memory("1234567890")
                .build();

        assertThat(usage.getCpu()).isEqualTo("123456789n");
        assertThat(usage.getMemory()).isEqualTo("1234567890");
    }
}
