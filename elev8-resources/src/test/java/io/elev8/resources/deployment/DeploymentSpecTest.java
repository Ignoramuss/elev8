package io.elev8.resources.deployment;

import io.elev8.resources.pod.Container;
import io.elev8.resources.pod.PodSpec;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeploymentSpecTest {

    @Test
    void shouldBuildDeploymentSpecWithRequiredFields() {
        final DeploymentSpec spec = DeploymentSpec.builder()
                .replicas(3)
                .selector("app", "test")
                .template(DeploymentSpec.PodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("nginx")
                                        .image("nginx:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(spec.getReplicas()).isEqualTo(3);
        assertThat(spec.getSelector()).containsEntry("app", "test");
        assertThat(spec.getTemplate()).isNotNull();
        assertThat(spec.getStrategy()).isEqualTo("RollingUpdate");
        assertThat(spec.getRevisionHistoryLimit()).isEqualTo(10);
    }

    @Test
    void shouldBuildDeploymentSpecWithAllFields() {
        final DeploymentSpec spec = DeploymentSpec.builder()
                .replicas(5)
                .selector(Map.of("app", "test", "version", "1.0"))
                .template(DeploymentSpec.PodTemplateSpec.builder()
                        .label("app", "test")
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("app")
                                        .image("app:1.0")
                                        .build())
                                .build())
                        .build())
                .strategy("Recreate")
                .revisionHistoryLimit(5)
                .build();

        assertThat(spec.getReplicas()).isEqualTo(5);
        assertThat(spec.getSelector()).hasSize(2);
        assertThat(spec.getStrategy()).isEqualTo("Recreate");
        assertThat(spec.getRevisionHistoryLimit()).isEqualTo(5);
    }

    @Test
    void shouldAllowDeploymentWithoutSelector() {
        final DeploymentSpec spec = DeploymentSpec.builder()
                .replicas(1)
                .template(DeploymentSpec.PodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("test")
                                        .image("test")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(spec.getSelector()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenNoTemplate() {
        assertThatThrownBy(() -> DeploymentSpec.builder()
                .replicas(1)
                .selector("app", "test")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("template is marked non-null but is null");
    }

    @Test
    void shouldBuildPodTemplateSpec() {
        final PodSpec podSpec = PodSpec.builder()
                .container(Container.builder()
                        .name("nginx")
                        .image("nginx:latest")
                        .build())
                .build();

        final DeploymentSpec.PodTemplateSpec template = DeploymentSpec.PodTemplateSpec.builder()
                .label("app", "test")
                .label("version", "1.0")
                .spec(podSpec)
                .build();

        assertThat(template.getMetadata().getLabels()).containsEntry("app", "test");
        assertThat(template.getMetadata().getLabels()).containsEntry("version", "1.0");
        assertThat(template.getSpec()).isEqualTo(podSpec);
    }
}
