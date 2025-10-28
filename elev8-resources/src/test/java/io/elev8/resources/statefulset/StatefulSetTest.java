package io.elev8.resources.statefulset;

import io.elev8.resources.pod.Container;
import io.elev8.resources.pod.PodSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatefulSetTest {

    @Test
    void shouldBuildStatefulSetWithRequiredFields() {
        final Container container = Container.builder()
                .name("web")
                .image("nginx:latest")
                .build();

        final StatefulSetSpec spec = StatefulSetSpec.builder()
                .serviceName("nginx")
                .selector("app", "nginx")
                .template(StatefulSetPodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(container)
                                .build())
                        .build())
                .build();

        final StatefulSet statefulSet = StatefulSet.builder()
                .name("web")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(statefulSet.getApiVersion()).isEqualTo("apps/v1");
        assertThat(statefulSet.getKind()).isEqualTo("StatefulSet");
        assertThat(statefulSet.getName()).isEqualTo("web");
        assertThat(statefulSet.getNamespace()).isEqualTo("default");
        assertThat(statefulSet.getSpec()).isEqualTo(spec);
        assertThat(statefulSet.getSpec().getServiceName()).isEqualTo("nginx");
    }

    @Test
    void shouldBuildStatefulSetWithLabels() {
        final StatefulSet statefulSet = StatefulSet.builder()
                .name("web")
                .namespace("default")
                .label("app", "nginx")
                .label("env", "prod")
                .spec(StatefulSetSpec.builder()
                        .serviceName("nginx")
                        .selector("app", "nginx")
                        .template(StatefulSetPodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("nginx")
                                                .image("nginx:1.21")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(statefulSet.getMetadata().getLabels()).containsEntry("app", "nginx");
        assertThat(statefulSet.getMetadata().getLabels()).containsEntry("env", "prod");
    }

    @Test
    void shouldBuildStatefulSetWithReplicas() {
        final StatefulSetSpec spec = StatefulSetSpec.builder()
                .serviceName("nginx")
                .replicas(3)
                .selector("app", "nginx")
                .template(StatefulSetPodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("nginx")
                                        .image("nginx:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        final StatefulSet statefulSet = StatefulSet.builder()
                .name("web")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(statefulSet.getSpec().getReplicas()).isEqualTo(3);
        assertThat(statefulSet.getSpec().getServiceName()).isEqualTo("nginx");
    }

    @Test
    void shouldBuildStatefulSetWithCustomUpdateStrategy() {
        final StatefulSetSpec spec = StatefulSetSpec.builder()
                .serviceName("nginx")
                .selector("app", "nginx")
                .updateStrategy("OnDelete")
                .podManagementPolicy("Parallel")
                .revisionHistoryLimit(5)
                .minReadySeconds(10)
                .template(StatefulSetPodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("nginx")
                                        .image("nginx:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        final StatefulSet statefulSet = StatefulSet.builder()
                .name("web")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(statefulSet.getSpec().getUpdateStrategy()).isEqualTo("OnDelete");
        assertThat(statefulSet.getSpec().getPodManagementPolicy()).isEqualTo("Parallel");
        assertThat(statefulSet.getSpec().getRevisionHistoryLimit()).isEqualTo(5);
        assertThat(statefulSet.getSpec().getMinReadySeconds()).isEqualTo(10);
    }

    @Test
    void shouldBuildStatefulSetWithDefaultValues() {
        final StatefulSetSpec spec = StatefulSetSpec.builder()
                .serviceName("nginx")
                .selector("app", "nginx")
                .template(StatefulSetPodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("nginx")
                                        .image("nginx:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        final StatefulSet statefulSet = StatefulSet.builder()
                .name("web")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(statefulSet.getSpec().getReplicas()).isEqualTo(1);
        assertThat(statefulSet.getSpec().getUpdateStrategy()).isEqualTo("RollingUpdate");
        assertThat(statefulSet.getSpec().getPodManagementPolicy()).isEqualTo("OrderedReady");
        assertThat(statefulSet.getSpec().getRevisionHistoryLimit()).isEqualTo(10);
    }

    @Test
    void shouldBuildStatefulSetWithPodTemplateLabels() {
        final StatefulSet statefulSet = StatefulSet.builder()
                .name("web")
                .namespace("default")
                .spec(StatefulSetSpec.builder()
                        .serviceName("nginx")
                        .selector("app", "nginx")
                        .template(StatefulSetPodTemplateSpec.builder()
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

        assertThat(statefulSet.getSpec().getTemplate().getMetadata().getLabels())
                .containsEntry("app", "nginx")
                .containsEntry("version", "v1");
    }

    @Test
    void shouldSerializeToJson() {
        final StatefulSet statefulSet = StatefulSet.builder()
                .name("web")
                .namespace("default")
                .spec(StatefulSetSpec.builder()
                        .serviceName("nginx")
                        .replicas(3)
                        .selector("app", "nginx")
                        .template(StatefulSetPodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("nginx")
                                                .image("nginx:1.21")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final String json = statefulSet.toJson();

        assertThat(json).contains("\"apiVersion\":\"apps/v1\"");
        assertThat(json).contains("\"kind\":\"StatefulSet\"");
        assertThat(json).contains("\"name\":\"web\"");
        assertThat(json).contains("\"serviceName\":\"nginx\"");
        assertThat(json).contains("\"replicas\":3");
        assertThat(json).contains("\"updateStrategy\":\"RollingUpdate\"");
        assertThat(json).contains("\"podManagementPolicy\":\"OrderedReady\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> StatefulSet.builder()
                .namespace("default")
                .spec(StatefulSetSpec.builder()
                        .serviceName("nginx")
                        .selector("app", "nginx")
                        .template(StatefulSetPodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("test")
                                                .image("test")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("StatefulSet name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> StatefulSet.builder()
                .name("web")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("StatefulSet spec is required");
    }

    @Test
    void shouldThrowExceptionWhenServiceNameIsNull() {
        assertThatThrownBy(() -> StatefulSet.builder()
                .name("web")
                .namespace("default")
                .spec(StatefulSetSpec.builder()
                        .selector("app", "nginx")
                        .template(StatefulSetPodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("test")
                                                .image("test")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build())
                .isInstanceOf(NullPointerException.class);
    }
}
