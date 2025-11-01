package io.elev8.resources.poddisruptionbudget;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.elev8.resources.LabelSelector;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PodDisruptionBudgetTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildPDBWithRequiredFields() {
        final PodDisruptionBudget pdb = PodDisruptionBudget.builder()
                .name("my-app-pdb")
                .namespace("default")
                .spec(PodDisruptionBudgetSpec.builder()
                        .minAvailable(2)
                        .build())
                .build();

        assertThat(pdb.getApiVersion()).isEqualTo("policy/v1");
        assertThat(pdb.getKind()).isEqualTo("PodDisruptionBudget");
        assertThat(pdb.getName()).isEqualTo("my-app-pdb");
        assertThat(pdb.getNamespace()).isEqualTo("default");
    }

    @Test
    void shouldBuildPDBWithMinAvailableInteger() {
        final PodDisruptionBudget pdb = PodDisruptionBudget.builder()
                .name("min-available-pdb")
                .namespace("production")
                .spec(PodDisruptionBudgetSpec.builder()
                        .minAvailable(3)
                        .selector(LabelSelector.builder()
                                .matchLabel("app", "my-app")
                                .build())
                        .build())
                .build();

        assertThat(pdb.getSpec().getMinAvailable()).isEqualTo(3);
        assertThat(pdb.getSpec().getSelector().getMatchLabels()).containsEntry("app", "my-app");
    }

    @Test
    void shouldBuildPDBWithMinAvailablePercentage() {
        final PodDisruptionBudget pdb = PodDisruptionBudget.builder()
                .name("min-available-percent-pdb")
                .namespace("production")
                .spec(PodDisruptionBudgetSpec.builder()
                        .minAvailable("50%")
                        .selector(LabelSelector.builder()
                                .matchLabel("app", "my-app")
                                .build())
                        .build())
                .build();

        assertThat(pdb.getSpec().getMinAvailable()).isEqualTo("50%");
    }

    @Test
    void shouldBuildPDBWithMaxUnavailableInteger() {
        final PodDisruptionBudget pdb = PodDisruptionBudget.builder()
                .name("max-unavailable-pdb")
                .namespace("production")
                .spec(PodDisruptionBudgetSpec.builder()
                        .maxUnavailable(1)
                        .selector(LabelSelector.builder()
                                .matchLabel("app", "my-app")
                                .build())
                        .build())
                .build();

        assertThat(pdb.getSpec().getMaxUnavailable()).isEqualTo(1);
    }

    @Test
    void shouldBuildPDBWithMaxUnavailablePercentage() {
        final PodDisruptionBudget pdb = PodDisruptionBudget.builder()
                .name("max-unavailable-percent-pdb")
                .namespace("production")
                .spec(PodDisruptionBudgetSpec.builder()
                        .maxUnavailable("25%")
                        .selector(LabelSelector.builder()
                                .matchLabel("app", "my-app")
                                .build())
                        .build())
                .build();

        assertThat(pdb.getSpec().getMaxUnavailable()).isEqualTo("25%");
    }

    @Test
    void shouldBuildPDBWithUnhealthyPodEvictionPolicy() {
        final PodDisruptionBudget pdb = PodDisruptionBudget.builder()
                .name("unhealthy-policy-pdb")
                .namespace("production")
                .spec(PodDisruptionBudgetSpec.builder()
                        .minAvailable(2)
                        .unhealthyPodEvictionPolicy("AlwaysAllow")
                        .selector(LabelSelector.builder()
                                .matchLabel("app", "my-app")
                                .build())
                        .build())
                .build();

        assertThat(pdb.getSpec().getUnhealthyPodEvictionPolicy()).isEqualTo("AlwaysAllow");
    }

    @Test
    void shouldBuildPDBWithStatus() {
        final PodDisruptionBudget pdb = PodDisruptionBudget.builder()
                .name("status-pdb")
                .namespace("production")
                .spec(PodDisruptionBudgetSpec.builder()
                        .minAvailable(2)
                        .build())
                .status(PodDisruptionBudgetStatus.builder()
                        .currentHealthy(3)
                        .desiredHealthy(2)
                        .disruptionsAllowed(1)
                        .expectedPods(3)
                        .observedGeneration(1L)
                        .build())
                .build();

        assertThat(pdb.getStatus().getCurrentHealthy()).isEqualTo(3);
        assertThat(pdb.getStatus().getDesiredHealthy()).isEqualTo(2);
        assertThat(pdb.getStatus().getDisruptionsAllowed()).isEqualTo(1);
        assertThat(pdb.getStatus().getExpectedPods()).isEqualTo(3);
    }

    @Test
    void shouldBuildPDBWithConditions() {
        final PodDisruptionBudget pdb = PodDisruptionBudget.builder()
                .name("conditions-pdb")
                .namespace("production")
                .spec(PodDisruptionBudgetSpec.builder()
                        .minAvailable(2)
                        .build())
                .status(PodDisruptionBudgetStatus.builder()
                        .condition(PodDisruptionBudgetCondition.builder()
                                .type("DisruptionAllowed")
                                .status("True")
                                .lastTransitionTime("2025-01-01T00:00:00Z")
                                .reason("SufficientPods")
                                .message("There are more pods than required")
                                .observedGeneration(1L)
                                .build())
                        .build())
                .build();

        assertThat(pdb.getStatus().getConditions()).hasSize(1);
        assertThat(pdb.getStatus().getConditions().get(0).getType()).isEqualTo("DisruptionAllowed");
        assertThat(pdb.getStatus().getConditions().get(0).getStatus()).isEqualTo("True");
    }

    @Test
    void shouldBuildPDBWithDisruptedPods() {
        final PodDisruptionBudget pdb = PodDisruptionBudget.builder()
                .name("disrupted-pods-pdb")
                .namespace("production")
                .spec(PodDisruptionBudgetSpec.builder()
                        .minAvailable(2)
                        .build())
                .status(PodDisruptionBudgetStatus.builder()
                        .disruptedPodEntry("pod-1", "2025-01-01T00:00:00Z")
                        .disruptedPodEntry("pod-2", "2025-01-01T00:00:05Z")
                        .build())
                .build();

        assertThat(pdb.getStatus().getDisruptedPods()).hasSize(2);
        assertThat(pdb.getStatus().getDisruptedPods()).containsEntry("pod-1", "2025-01-01T00:00:00Z");
    }

    @Test
    void shouldBuildPDBWithLabels() {
        final PodDisruptionBudget pdb = PodDisruptionBudget.builder()
                .name("labeled-pdb")
                .namespace("production")
                .label("environment", "production")
                .label("team", "platform")
                .spec(PodDisruptionBudgetSpec.builder()
                        .minAvailable(2)
                        .build())
                .build();

        assertThat(pdb.getMetadata().getLabels())
                .containsEntry("environment", "production")
                .containsEntry("team", "platform");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> PodDisruptionBudget.builder()
                .namespace("default")
                .spec(PodDisruptionBudgetSpec.builder()
                        .minAvailable(2)
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PodDisruptionBudget name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> PodDisruptionBudget.builder()
                .name("test")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PodDisruptionBudget spec is required");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final PodDisruptionBudget pdb = PodDisruptionBudget.builder()
                .name("serialize-pdb")
                .namespace("default")
                .spec(PodDisruptionBudgetSpec.builder()
                        .minAvailable(2)
                        .selector(LabelSelector.builder()
                                .matchLabel("app", "my-app")
                                .build())
                        .build())
                .build();

        final String json = objectMapper.writeValueAsString(pdb);

        assertThat(json).contains("\"apiVersion\":\"policy/v1\"");
        assertThat(json).contains("\"kind\":\"PodDisruptionBudget\"");
        assertThat(json).contains("\"name\":\"serialize-pdb\"");
        assertThat(json).contains("\"minAvailable\":2");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "apiVersion": "policy/v1",
                  "kind": "PodDisruptionBudget",
                  "metadata": {
                    "name": "deserialize-pdb",
                    "namespace": "default"
                  },
                  "spec": {
                    "minAvailable": 2,
                    "selector": {
                      "matchLabels": {
                        "app": "my-app"
                      }
                    }
                  }
                }
                """;

        final PodDisruptionBudget pdb = objectMapper.readValue(json, PodDisruptionBudget.class);

        assertThat(pdb.getApiVersion()).isEqualTo("policy/v1");
        assertThat(pdb.getKind()).isEqualTo("PodDisruptionBudget");
        assertThat(pdb.getName()).isEqualTo("deserialize-pdb");
        assertThat(pdb.getSpec().getMinAvailable()).isEqualTo(2);
    }
}
