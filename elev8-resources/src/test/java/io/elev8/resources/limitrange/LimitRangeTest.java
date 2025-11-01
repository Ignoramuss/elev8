package io.elev8.resources.limitrange;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LimitRangeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildLimitRangeWithRequiredFields() {
        final LimitRange limitRange = LimitRange.builder()
                .name("mem-limit-range")
                .namespace("default")
                .spec(LimitRangeSpec.builder()
                        .limit(LimitRangeItem.builder()
                                .type("Container")
                                .build())
                        .build())
                .build();

        assertThat(limitRange.getApiVersion()).isEqualTo("v1");
        assertThat(limitRange.getKind()).isEqualTo("LimitRange");
        assertThat(limitRange.getName()).isEqualTo("mem-limit-range");
        assertThat(limitRange.getNamespace()).isEqualTo("default");
    }

    @Test
    void shouldBuildLimitRangeWithContainerResourceLimits() {
        final LimitRange limitRange = LimitRange.builder()
                .name("container-limits")
                .namespace("default")
                .spec(LimitRangeSpec.builder()
                        .limit(LimitRangeItem.builder()
                                .type("Container")
                                .maxEntry("cpu", "2")
                                .maxEntry("memory", "2Gi")
                                .minEntry("cpu", "100m")
                                .minEntry("memory", "128Mi")
                                .defaultEntry("cpu", "500m")
                                .defaultEntry("memory", "512Mi")
                                .defaultRequestEntry("cpu", "200m")
                                .defaultRequestEntry("memory", "256Mi")
                                .maxLimitRequestRatioEntry("cpu", "10")
                                .build())
                        .build())
                .build();

        assertThat(limitRange.getSpec().getLimits()).hasSize(1);
        final LimitRangeItem item = limitRange.getSpec().getLimits().get(0);
        assertThat(item.getType()).isEqualTo("Container");
        assertThat(item.getMax()).containsEntry("cpu", "2");
        assertThat(item.getMax()).containsEntry("memory", "2Gi");
        assertThat(item.getMin()).containsEntry("cpu", "100m");
        assertThat(item.getMin()).containsEntry("memory", "128Mi");
        assertThat(item.getDefaultLimit()).containsEntry("cpu", "500m");
        assertThat(item.getDefaultLimit()).containsEntry("memory", "512Mi");
        assertThat(item.getDefaultRequest()).containsEntry("cpu", "200m");
        assertThat(item.getDefaultRequest()).containsEntry("memory", "256Mi");
        assertThat(item.getMaxLimitRequestRatio()).containsEntry("cpu", "10");
    }

    @Test
    void shouldBuildLimitRangeWithPodResourceLimits() {
        final LimitRange limitRange = LimitRange.builder()
                .name("pod-limits")
                .namespace("default")
                .spec(LimitRangeSpec.builder()
                        .limit(LimitRangeItem.builder()
                                .type("Pod")
                                .maxEntry("cpu", "4")
                                .maxEntry("memory", "4Gi")
                                .minEntry("cpu", "200m")
                                .minEntry("memory", "256Mi")
                                .build())
                        .build())
                .build();

        assertThat(limitRange.getSpec().getLimits()).hasSize(1);
        final LimitRangeItem item = limitRange.getSpec().getLimits().get(0);
        assertThat(item.getType()).isEqualTo("Pod");
        assertThat(item.getMax()).containsEntry("cpu", "4");
        assertThat(item.getMax()).containsEntry("memory", "4Gi");
    }

    @Test
    void shouldBuildLimitRangeWithPVCStorageLimits() {
        final LimitRange limitRange = LimitRange.builder()
                .name("storage-limits")
                .namespace("default")
                .spec(LimitRangeSpec.builder()
                        .limit(LimitRangeItem.builder()
                                .type("PersistentVolumeClaim")
                                .maxEntry("storage", "100Gi")
                                .minEntry("storage", "1Gi")
                                .build())
                        .build())
                .build();

        assertThat(limitRange.getSpec().getLimits()).hasSize(1);
        final LimitRangeItem item = limitRange.getSpec().getLimits().get(0);
        assertThat(item.getType()).isEqualTo("PersistentVolumeClaim");
        assertThat(item.getMax()).containsEntry("storage", "100Gi");
        assertThat(item.getMin()).containsEntry("storage", "1Gi");
    }

    @Test
    void shouldBuildLimitRangeWithMultipleLimitItems() {
        final LimitRange limitRange = LimitRange.builder()
                .name("multi-limits")
                .namespace("default")
                .spec(LimitRangeSpec.builder()
                        .limit(LimitRangeItem.builder()
                                .type("Container")
                                .maxEntry("cpu", "2")
                                .maxEntry("memory", "2Gi")
                                .build())
                        .limit(LimitRangeItem.builder()
                                .type("Pod")
                                .maxEntry("cpu", "4")
                                .maxEntry("memory", "4Gi")
                                .build())
                        .limit(LimitRangeItem.builder()
                                .type("PersistentVolumeClaim")
                                .maxEntry("storage", "100Gi")
                                .build())
                        .build())
                .build();

        assertThat(limitRange.getSpec().getLimits()).hasSize(3);
        assertThat(limitRange.getSpec().getLimits().get(0).getType()).isEqualTo("Container");
        assertThat(limitRange.getSpec().getLimits().get(1).getType()).isEqualTo("Pod");
        assertThat(limitRange.getSpec().getLimits().get(2).getType()).isEqualTo("PersistentVolumeClaim");
    }

    @Test
    void shouldBuildLimitRangeWithLabels() {
        final LimitRange limitRange = LimitRange.builder()
                .name("labeled-limit")
                .namespace("default")
                .label("environment", "production")
                .label("team", "platform")
                .spec(LimitRangeSpec.builder()
                        .limit(LimitRangeItem.builder()
                                .type("Container")
                                .build())
                        .build())
                .build();

        assertThat(limitRange.getMetadata().getLabels())
                .containsEntry("environment", "production")
                .containsEntry("team", "platform");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> LimitRange.builder()
                .namespace("default")
                .spec(LimitRangeSpec.builder()
                        .limit(LimitRangeItem.builder()
                                .type("Container")
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("LimitRange name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> LimitRange.builder()
                .name("test")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("LimitRange spec is required");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final LimitRange limitRange = LimitRange.builder()
                .name("cpu-limit")
                .namespace("default")
                .spec(LimitRangeSpec.builder()
                        .limit(LimitRangeItem.builder()
                                .type("Container")
                                .maxEntry("cpu", "2")
                                .defaultEntry("cpu", "500m")
                                .build())
                        .build())
                .build();

        final String json = objectMapper.writeValueAsString(limitRange);

        assertThat(json).contains("\"apiVersion\":\"v1\"");
        assertThat(json).contains("\"kind\":\"LimitRange\"");
        assertThat(json).contains("\"name\":\"cpu-limit\"");
        assertThat(json).contains("\"type\":\"Container\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "apiVersion": "v1",
                  "kind": "LimitRange",
                  "metadata": {
                    "name": "cpu-limit",
                    "namespace": "default"
                  },
                  "spec": {
                    "limits": [
                      {
                        "type": "Container",
                        "max": {
                          "cpu": "2"
                        },
                        "default": {
                          "cpu": "500m"
                        }
                      }
                    ]
                  }
                }
                """;

        final LimitRange limitRange = objectMapper.readValue(json, LimitRange.class);

        assertThat(limitRange.getApiVersion()).isEqualTo("v1");
        assertThat(limitRange.getKind()).isEqualTo("LimitRange");
        assertThat(limitRange.getName()).isEqualTo("cpu-limit");
        assertThat(limitRange.getNamespace()).isEqualTo("default");
        assertThat(limitRange.getSpec().getLimits()).hasSize(1);
        assertThat(limitRange.getSpec().getLimits().get(0).getType()).isEqualTo("Container");
    }
}
