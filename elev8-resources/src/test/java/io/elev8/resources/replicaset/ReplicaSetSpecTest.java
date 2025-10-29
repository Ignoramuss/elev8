package io.elev8.resources.replicaset;

import io.elev8.resources.pod.Container;
import io.elev8.resources.pod.PodSpec;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReplicaSetSpecTest {

    @Test
    void shouldBuildReplicaSetSpecWithRequiredFields() {
        final ReplicaSetSpec spec = ReplicaSetSpec.builder()
                .selector("app", "nginx")
                .template(ReplicaSetPodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("nginx")
                                        .image("nginx:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(spec.getSelector()).containsEntry("app", "nginx");
        assertThat(spec.getTemplate()).isNotNull();
        assertThat(spec.getReplicas()).isEqualTo(1);
        assertThat(spec.getMinReadySeconds()).isNull();
    }

    @Test
    void shouldBuildReplicaSetSpecWithCustomReplicas() {
        final ReplicaSetSpec spec = ReplicaSetSpec.builder()
                .replicas(5)
                .selector("app", "nginx")
                .template(ReplicaSetPodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("nginx")
                                        .image("nginx:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(spec.getReplicas()).isEqualTo(5);
    }

    @Test
    void shouldBuildReplicaSetSpecWithMinReadySeconds() {
        final ReplicaSetSpec spec = ReplicaSetSpec.builder()
                .selector("app", "nginx")
                .minReadySeconds(30)
                .template(ReplicaSetPodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("nginx")
                                        .image("nginx:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(spec.getMinReadySeconds()).isEqualTo(30);
    }

    @Test
    void shouldBuildReplicaSetSpecWithMultipleSelectors() {
        final ReplicaSetSpec spec = ReplicaSetSpec.builder()
                .selector("app", "nginx")
                .selector("tier", "frontend")
                .selector("environment", "production")
                .template(ReplicaSetPodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("nginx")
                                        .image("nginx:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(spec.getSelector()).hasSize(3);
        assertThat(spec.getSelector()).containsEntry("app", "nginx");
        assertThat(spec.getSelector()).containsEntry("tier", "frontend");
        assertThat(spec.getSelector()).containsEntry("environment", "production");
    }

    @Test
    void shouldBuildReplicaSetSpecWithSelectorMap() {
        final ReplicaSetSpec spec = ReplicaSetSpec.builder()
                .selector(Map.of("app", "nginx", "version", "1.0"))
                .template(ReplicaSetPodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("nginx")
                                        .image("nginx:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(spec.getSelector()).hasSize(2);
        assertThat(spec.getSelector()).containsEntry("app", "nginx");
        assertThat(spec.getSelector()).containsEntry("version", "1.0");
    }

    @Test
    void shouldSupportToBuilder() {
        final ReplicaSetSpec original = ReplicaSetSpec.builder()
                .replicas(3)
                .selector("app", "nginx")
                .template(ReplicaSetPodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("nginx")
                                        .image("nginx:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        final ReplicaSetSpec modified = original.toBuilder()
                .replicas(5)
                .minReadySeconds(60)
                .build();

        assertThat(modified.getReplicas()).isEqualTo(5);
        assertThat(modified.getMinReadySeconds()).isEqualTo(60);
        assertThat(modified.getSelector()).containsEntry("app", "nginx");
    }

    @Test
    void shouldAllowEmptySelector() {
        final ReplicaSetSpec spec = ReplicaSetSpec.builder()
                .replicas(3)
                .template(ReplicaSetPodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("nginx")
                                        .image("nginx:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(spec.getSelector()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenTemplateIsNull() {
        assertThatThrownBy(() -> ReplicaSetSpec.builder()
                .replicas(3)
                .selector("app", "nginx")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("template is marked non-null but is null");
    }
}
