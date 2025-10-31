package io.elev8.resources.horizontalpodautoscaler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HorizontalPodAutoscalerTest {

    @Test
    void shouldBuildHPAWithRequiredFields() {
        final HorizontalPodAutoscaler hpa = HorizontalPodAutoscaler.builder()
                .name("php-apache")
                .namespace("default")
                .spec(HorizontalPodAutoscalerSpec.builder()
                        .scaleTargetRef(CrossVersionObjectReference.builder()
                                .apiVersion("apps/v1")
                                .kind("Deployment")
                                .name("php-apache")
                                .build())
                        .maxReplicas(10)
                        .build())
                .build();

        assertThat(hpa.getApiVersion()).isEqualTo("autoscaling/v2");
        assertThat(hpa.getKind()).isEqualTo("HorizontalPodAutoscaler");
        assertThat(hpa.getName()).isEqualTo("php-apache");
        assertThat(hpa.getNamespace()).isEqualTo("default");
    }

    @Test
    void shouldBuildHPAWithResourceMetrics() {
        final HorizontalPodAutoscaler hpa = HorizontalPodAutoscaler.builder()
                .name("cpu-hpa")
                .namespace("default")
                .spec(HorizontalPodAutoscalerSpec.builder()
                        .scaleTargetRef(CrossVersionObjectReference.builder()
                                .apiVersion("apps/v1")
                                .kind("Deployment")
                                .name("my-app")
                                .build())
                        .minReplicas(2)
                        .maxReplicas(10)
                        .metric(MetricSpec.builder()
                                .type("Resource")
                                .resource(ResourceMetricSource.builder()
                                        .name("cpu")
                                        .target(MetricTarget.builder()
                                                .type("Utilization")
                                                .averageUtilization(70)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(hpa.getSpec().getMetrics()).hasSize(1);
        assertThat(hpa.getSpec().getMetrics().get(0).getType()).isEqualTo("Resource");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> HorizontalPodAutoscaler.builder()
                .namespace("default")
                .spec(HorizontalPodAutoscalerSpec.builder().maxReplicas(10).build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HorizontalPodAutoscaler name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> HorizontalPodAutoscaler.builder()
                .name("test")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HorizontalPodAutoscaler spec is required");
    }
}
