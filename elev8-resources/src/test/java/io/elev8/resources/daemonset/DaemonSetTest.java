package io.elev8.resources.daemonset;

import io.elev8.resources.pod.Container;
import io.elev8.resources.pod.PodSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaemonSetTest {

    @Test
    void shouldBuildDaemonSetWithRequiredFields() {
        final Container container = Container.builder()
                .name("fluentd")
                .image("fluentd:latest")
                .build();

        final DaemonSetSpec spec = DaemonSetSpec.builder()
                .selector("app", "fluentd")
                .template(DaemonSetPodTemplateSpec.builder()
                        .label("app", "fluentd")
                        .spec(PodSpec.builder()
                                .container(container)
                                .build())
                        .build())
                .build();

        final DaemonSet daemonSet = DaemonSet.builder()
                .name("test-daemonset")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(daemonSet.getApiVersion()).isEqualTo("apps/v1");
        assertThat(daemonSet.getKind()).isEqualTo("DaemonSet");
        assertThat(daemonSet.getName()).isEqualTo("test-daemonset");
        assertThat(daemonSet.getNamespace()).isEqualTo("default");
        assertThat(daemonSet.getSpec()).isEqualTo(spec);
    }

    @Test
    void shouldBuildDaemonSetWithLabels() {
        final DaemonSet daemonSet = DaemonSet.builder()
                .name("test-daemonset")
                .namespace("kube-system")
                .label("app", "monitoring")
                .label("env", "prod")
                .spec(DaemonSetSpec.builder()
                        .selector("app", "monitoring")
                        .template(DaemonSetPodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("node-exporter")
                                                .image("node-exporter:1.0")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(daemonSet.getMetadata().getLabels()).containsEntry("app", "monitoring");
        assertThat(daemonSet.getMetadata().getLabels()).containsEntry("env", "prod");
    }

    @Test
    void shouldBuildDaemonSetWithUpdateStrategy() {
        final DaemonSetSpec spec = DaemonSetSpec.builder()
                .selector("app", "test")
                .updateStrategy("OnDelete")
                .minReadySeconds(30)
                .template(DaemonSetPodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("test")
                                        .image("test:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        final DaemonSet daemonSet = DaemonSet.builder()
                .name("test-daemonset")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(daemonSet.getSpec().getUpdateStrategy()).isEqualTo("OnDelete");
        assertThat(daemonSet.getSpec().getMinReadySeconds()).isEqualTo(30);
    }

    @Test
    void shouldSerializeToJson() {
        final DaemonSet daemonSet = DaemonSet.builder()
                .name("test-daemonset")
                .namespace("default")
                .spec(DaemonSetSpec.builder()
                        .selector("app", "test")
                        .template(DaemonSetPodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("nginx")
                                                .image("nginx:latest")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final String json = daemonSet.toJson();

        assertThat(json).contains("\"apiVersion\":\"apps/v1\"");
        assertThat(json).contains("\"kind\":\"DaemonSet\"");
        assertThat(json).contains("\"name\":\"test-daemonset\"");
        assertThat(json).contains("\"updateStrategy\":\"RollingUpdate\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> DaemonSet.builder()
                .namespace("default")
                .spec(DaemonSetSpec.builder()
                        .selector("app", "test")
                        .template(DaemonSetPodTemplateSpec.builder()
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
                .hasMessageContaining("DaemonSet name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> DaemonSet.builder()
                .name("test-daemonset")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DaemonSet spec is required");
    }
}
