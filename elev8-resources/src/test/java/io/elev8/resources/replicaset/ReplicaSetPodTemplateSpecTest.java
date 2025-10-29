package io.elev8.resources.replicaset;

import io.elev8.resources.Metadata;
import io.elev8.resources.pod.Container;
import io.elev8.resources.pod.PodSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReplicaSetPodTemplateSpecTest {

    @Test
    void shouldBuildPodTemplateSpecWithRequiredFields() {
        final PodSpec podSpec = PodSpec.builder()
                .container(Container.builder()
                        .name("nginx")
                        .image("nginx:latest")
                        .build())
                .build();

        final ReplicaSetPodTemplateSpec template = ReplicaSetPodTemplateSpec.builder()
                .spec(podSpec)
                .build();

        assertThat(template.getSpec()).isEqualTo(podSpec);
        assertThat(template.getMetadata()).isNull();
    }

    @Test
    void shouldBuildPodTemplateSpecWithMetadata() {
        final Metadata metadata = Metadata.builder()
                .label("app", "nginx")
                .build();

        final PodSpec podSpec = PodSpec.builder()
                .container(Container.builder()
                        .name("nginx")
                        .image("nginx:latest")
                        .build())
                .build();

        final ReplicaSetPodTemplateSpec template = ReplicaSetPodTemplateSpec.builder()
                .metadata(metadata)
                .spec(podSpec)
                .build();

        assertThat(template.getMetadata()).isEqualTo(metadata);
        assertThat(template.getSpec()).isEqualTo(podSpec);
    }

    @Test
    void shouldBuildPodTemplateSpecWithLabels() {
        final PodSpec podSpec = PodSpec.builder()
                .container(Container.builder()
                        .name("nginx")
                        .image("nginx:latest")
                        .build())
                .build();

        final ReplicaSetPodTemplateSpec template = ReplicaSetPodTemplateSpec.builder()
                .label("app", "nginx")
                .label("version", "v1")
                .spec(podSpec)
                .build();

        assertThat(template.getMetadata()).isNotNull();
        assertThat(template.getMetadata().getLabels()).containsEntry("app", "nginx");
        assertThat(template.getMetadata().getLabels()).containsEntry("version", "v1");
    }

    @Test
    void shouldBuildPodTemplateSpecWithMultipleLabels() {
        final PodSpec podSpec = PodSpec.builder()
                .container(Container.builder()
                        .name("nginx")
                        .image("nginx:latest")
                        .build())
                .build();

        final ReplicaSetPodTemplateSpec template = ReplicaSetPodTemplateSpec.builder()
                .label("app", "nginx")
                .label("tier", "frontend")
                .label("environment", "production")
                .spec(podSpec)
                .build();

        assertThat(template.getMetadata().getLabels()).hasSize(3);
        assertThat(template.getMetadata().getLabels()).containsEntry("app", "nginx");
        assertThat(template.getMetadata().getLabels()).containsEntry("tier", "frontend");
        assertThat(template.getMetadata().getLabels()).containsEntry("environment", "production");
    }

    @Test
    void shouldSupportToBuilder() {
        final PodSpec podSpec = PodSpec.builder()
                .container(Container.builder()
                        .name("nginx")
                        .image("nginx:latest")
                        .build())
                .build();

        final ReplicaSetPodTemplateSpec original = ReplicaSetPodTemplateSpec.builder()
                .label("app", "nginx")
                .spec(podSpec)
                .build();

        final ReplicaSetPodTemplateSpec modified = original.toBuilder()
                .label("version", "v2")
                .build();

        assertThat(modified.getMetadata().getLabels()).containsEntry("app", "nginx");
        assertThat(modified.getMetadata().getLabels()).containsEntry("version", "v2");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> ReplicaSetPodTemplateSpec.builder()
                .label("app", "nginx")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("spec is marked non-null but is null");
    }
}
