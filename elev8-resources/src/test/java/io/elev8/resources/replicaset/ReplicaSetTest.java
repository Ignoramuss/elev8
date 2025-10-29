package io.elev8.resources.replicaset;

import io.elev8.resources.pod.Container;
import io.elev8.resources.pod.PodSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReplicaSetTest {

    @Test
    void shouldBuildReplicaSetWithRequiredFields() {
        final Container container = Container.builder()
                .name("nginx")
                .image("nginx:latest")
                .build();

        final ReplicaSetSpec spec = ReplicaSetSpec.builder()
                .selector("app", "nginx")
                .template(ReplicaSetPodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(container)
                                .build())
                        .build())
                .build();

        final ReplicaSet replicaSet = ReplicaSet.builder()
                .name("nginx-replicaset")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(replicaSet.getApiVersion()).isEqualTo("apps/v1");
        assertThat(replicaSet.getKind()).isEqualTo("ReplicaSet");
        assertThat(replicaSet.getName()).isEqualTo("nginx-replicaset");
        assertThat(replicaSet.getNamespace()).isEqualTo("default");
        assertThat(replicaSet.getSpec()).isEqualTo(spec);
    }

    @Test
    void shouldBuildReplicaSetWithLabels() {
        final ReplicaSet replicaSet = ReplicaSet.builder()
                .name("nginx-replicaset")
                .namespace("default")
                .label("app", "nginx")
                .label("env", "prod")
                .spec(ReplicaSetSpec.builder()
                        .selector("app", "nginx")
                        .template(ReplicaSetPodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("nginx")
                                                .image("nginx:1.21")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(replicaSet.getMetadata().getLabels()).containsEntry("app", "nginx");
        assertThat(replicaSet.getMetadata().getLabels()).containsEntry("env", "prod");
    }

    @Test
    void shouldBuildReplicaSetWithCustomReplicas() {
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

        final ReplicaSet replicaSet = ReplicaSet.builder()
                .name("nginx-replicaset")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(replicaSet.getSpec().getReplicas()).isEqualTo(5);
    }

    @Test
    void shouldBuildReplicaSetWithDefaultReplicas() {
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

        final ReplicaSet replicaSet = ReplicaSet.builder()
                .name("nginx-replicaset")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(replicaSet.getSpec().getReplicas()).isEqualTo(1);
    }

    @Test
    void shouldBuildReplicaSetWithMinReadySeconds() {
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

        final ReplicaSet replicaSet = ReplicaSet.builder()
                .name("nginx-replicaset")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(replicaSet.getSpec().getMinReadySeconds()).isEqualTo(30);
    }

    @Test
    void shouldBuildReplicaSetWithMultipleSelectors() {
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

        final ReplicaSet replicaSet = ReplicaSet.builder()
                .name("nginx-replicaset")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(replicaSet.getSpec().getSelector()).containsEntry("app", "nginx");
        assertThat(replicaSet.getSpec().getSelector()).containsEntry("tier", "frontend");
        assertThat(replicaSet.getSpec().getSelector()).containsEntry("environment", "production");
    }

    @Test
    void shouldBuildReplicaSetWithPodTemplateLabels() {
        final ReplicaSet replicaSet = ReplicaSet.builder()
                .name("nginx-replicaset")
                .namespace("default")
                .spec(ReplicaSetSpec.builder()
                        .selector("app", "nginx")
                        .template(ReplicaSetPodTemplateSpec.builder()
                                .label("app", "nginx")
                                .label("version", "v1")
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("nginx")
                                                .image("nginx:latest")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(replicaSet.getSpec().getTemplate().getMetadata().getLabels())
                .containsEntry("app", "nginx")
                .containsEntry("version", "v1");
    }

    @Test
    void shouldSerializeToJson() {
        final ReplicaSet replicaSet = ReplicaSet.builder()
                .name("nginx-replicaset")
                .namespace("default")
                .spec(ReplicaSetSpec.builder()
                        .replicas(3)
                        .selector("app", "nginx")
                        .template(ReplicaSetPodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("nginx")
                                                .image("nginx:1.21")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final String json = replicaSet.toJson();

        assertThat(json).contains("\"apiVersion\":\"apps/v1\"");
        assertThat(json).contains("\"kind\":\"ReplicaSet\"");
        assertThat(json).contains("\"name\":\"nginx-replicaset\"");
        assertThat(json).contains("\"replicas\":3");
        assertThat(json).contains("\"selector\"");
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() {
        final ReplicaSet replicaSet = ReplicaSet.builder()
                .name("nginx-replicaset")
                .namespace("default")
                .spec(ReplicaSetSpec.builder()
                        .selector("app", "nginx")
                        .template(ReplicaSetPodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("nginx")
                                                .image("nginx:latest")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final String json = replicaSet.toJson();

        assertThat(json).doesNotContain("\"status\"");
        assertThat(json).doesNotContain("\"minReadySeconds\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> ReplicaSet.builder()
                .namespace("default")
                .spec(ReplicaSetSpec.builder()
                        .selector("app", "nginx")
                        .template(ReplicaSetPodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("nginx")
                                                .image("nginx:latest")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ReplicaSet name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> ReplicaSet.builder()
                .name("")
                .namespace("default")
                .spec(ReplicaSetSpec.builder()
                        .selector("app", "nginx")
                        .template(ReplicaSetPodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("nginx")
                                                .image("nginx:latest")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ReplicaSet name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> ReplicaSet.builder()
                .name("nginx-replicaset")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ReplicaSet spec is required");
    }

    @Test
    void shouldAllowReplicaSetWithoutNamespace() {
        final ReplicaSet replicaSet = ReplicaSet.builder()
                .name("nginx-replicaset")
                .spec(ReplicaSetSpec.builder()
                        .selector("app", "nginx")
                        .template(ReplicaSetPodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("nginx")
                                                .image("nginx:latest")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(replicaSet.getName()).isEqualTo("nginx-replicaset");
        assertThat(replicaSet.getNamespace()).isNull();
    }
}
