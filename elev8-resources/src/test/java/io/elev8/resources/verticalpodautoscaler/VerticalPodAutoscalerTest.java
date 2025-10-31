package io.elev8.resources.verticalpodautoscaler;

import io.elev8.resources.horizontalpodautoscaler.CrossVersionObjectReference;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerticalPodAutoscalerTest {

    @Test
    void shouldBuildVPAWithRequiredFields() {
        final VerticalPodAutoscaler vpa = VerticalPodAutoscaler.builder()
                .name("my-app-vpa")
                .namespace("default")
                .spec(VerticalPodAutoscalerSpec.builder()
                        .targetRef(CrossVersionObjectReference.builder()
                                .apiVersion("apps/v1")
                                .kind("Deployment")
                                .name("my-app")
                                .build())
                        .build())
                .build();

        assertThat(vpa.getApiVersion()).isEqualTo("autoscaling.k8s.io/v1");
        assertThat(vpa.getKind()).isEqualTo("VerticalPodAutoscaler");
        assertThat(vpa.getName()).isEqualTo("my-app-vpa");
        assertThat(vpa.getNamespace()).isEqualTo("default");
    }

    @Test
    void shouldBuildVPAWithUpdatePolicy() {
        final VerticalPodAutoscaler vpa = VerticalPodAutoscaler.builder()
                .name("vpa-off")
                .namespace("default")
                .spec(VerticalPodAutoscalerSpec.builder()
                        .targetRef(CrossVersionObjectReference.builder()
                                .apiVersion("apps/v1")
                                .kind("Deployment")
                                .name("my-app")
                                .build())
                        .updatePolicy(VPAUpdatePolicy.builder()
                                .updateMode("Off")
                                .build())
                        .build())
                .build();

        assertThat(vpa.getSpec().getUpdatePolicy()).isNotNull();
        assertThat(vpa.getSpec().getUpdatePolicy().getUpdateMode()).isEqualTo("Off");
    }

    @Test
    void shouldBuildVPAWithResourcePolicy() {
        final VerticalPodAutoscaler vpa = VerticalPodAutoscaler.builder()
                .name("vpa-with-limits")
                .namespace("default")
                .spec(VerticalPodAutoscalerSpec.builder()
                        .targetRef(CrossVersionObjectReference.builder()
                                .apiVersion("apps/v1")
                                .kind("Deployment")
                                .name("my-app")
                                .build())
                        .resourcePolicy(VPAResourcePolicy.builder()
                                .containerPolicy(VPAContainerResourcePolicy.builder()
                                        .containerName("*")
                                        .minAllowed(Map.of("cpu", "100m", "memory", "128Mi"))
                                        .maxAllowed(Map.of("cpu", "2", "memory", "2Gi"))
                                        .controlledResource("cpu")
                                        .controlledResource("memory")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(vpa.getSpec().getResourcePolicy()).isNotNull();
        assertThat(vpa.getSpec().getResourcePolicy().getContainerPolicies()).hasSize(1);
        assertThat(vpa.getSpec().getResourcePolicy().getContainerPolicies().get(0).getContainerName())
                .isEqualTo("*");
    }

    @Test
    void shouldBuildVPAWithRecommenders() {
        final VerticalPodAutoscaler vpa = VerticalPodAutoscaler.builder()
                .name("vpa-custom-recommender")
                .namespace("default")
                .spec(VerticalPodAutoscalerSpec.builder()
                        .targetRef(CrossVersionObjectReference.builder()
                                .apiVersion("apps/v1")
                                .kind("Deployment")
                                .name("my-app")
                                .build())
                        .recommender(VPARecommenderSelector.builder()
                                .name("custom-recommender")
                                .build())
                        .build())
                .build();

        assertThat(vpa.getSpec().getRecommenders()).hasSize(1);
        assertThat(vpa.getSpec().getRecommenders().get(0).getName()).isEqualTo("custom-recommender");
    }

    @Test
    void shouldBuildVPAWithStatus() {
        final VerticalPodAutoscaler vpa = VerticalPodAutoscaler.builder()
                .name("vpa-with-status")
                .namespace("default")
                .spec(VerticalPodAutoscalerSpec.builder()
                        .targetRef(CrossVersionObjectReference.builder()
                                .apiVersion("apps/v1")
                                .kind("Deployment")
                                .name("my-app")
                                .build())
                        .build())
                .status(VerticalPodAutoscalerStatus.builder()
                        .condition(VPACondition.builder()
                                .type("RecommendationProvided")
                                .status("True")
                                .build())
                        .recommendation(VPARecommendation.builder()
                                .containerRecommendation(VPAContainerRecommendation.builder()
                                        .containerName("app")
                                        .target(Map.of("cpu", "250m", "memory", "256Mi"))
                                        .lowerBound(Map.of("cpu", "100m", "memory", "128Mi"))
                                        .upperBound(Map.of("cpu", "500m", "memory", "512Mi"))
                                        .uncappedTarget(Map.of("cpu", "300m", "memory", "300Mi"))
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(vpa.getStatus()).isNotNull();
        assertThat(vpa.getStatus().getConditions()).hasSize(1);
        assertThat(vpa.getStatus().getRecommendation()).isNotNull();
        assertThat(vpa.getStatus().getRecommendation().getContainerRecommendations()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> VerticalPodAutoscaler.builder()
                .namespace("default")
                .spec(VerticalPodAutoscalerSpec.builder()
                        .targetRef(CrossVersionObjectReference.builder().build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VerticalPodAutoscaler name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> VerticalPodAutoscaler.builder()
                .name("test")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VerticalPodAutoscaler spec is required");
    }
}
