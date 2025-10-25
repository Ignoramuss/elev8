package io.elev8.resources.pod;

import io.elev8.resources.Metadata;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PodTest {

    @Test
    void shouldBuildPodWithRequiredFields() {
        final Container container = Container.builder()
                .name("nginx")
                .image("nginx:latest")
                .build();

        final PodSpec spec = PodSpec.builder()
                .container(container)
                .build();

        final Pod pod = Pod.builder()
                .name("test-pod")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(pod.getApiVersion()).isEqualTo("v1");
        assertThat(pod.getKind()).isEqualTo("Pod");
        assertThat(pod.getName()).isEqualTo("test-pod");
        assertThat(pod.getNamespace()).isEqualTo("default");
        assertThat(pod.getSpec()).isEqualTo(spec);
    }

    @Test
    void shouldBuildPodWithLabels() {
        final Container container = Container.builder()
                .name("nginx")
                .image("nginx:latest")
                .build();

        final Pod pod = Pod.builder()
                .name("test-pod")
                .namespace("default")
                .label("app", "test")
                .label("version", "1.0")
                .spec(PodSpec.builder()
                        .container(container)
                        .build())
                .build();

        assertThat(pod.getMetadata().getLabels()).containsEntry("app", "test");
        assertThat(pod.getMetadata().getLabels()).containsEntry("version", "1.0");
    }

    @Test
    void shouldSerializeToJson() {
        final Container container = Container.builder()
                .name("nginx")
                .image("nginx:latest")
                .addPort(80)
                .build();

        final Pod pod = Pod.builder()
                .name("test-pod")
                .namespace("default")
                .spec(PodSpec.builder()
                        .container(container)
                        .build())
                .build();

        final String json = pod.toJson();

        assertThat(json).contains("\"apiVersion\":\"v1\"");
        assertThat(json).contains("\"kind\":\"Pod\"");
        assertThat(json).contains("\"name\":\"test-pod\"");
        assertThat(json).contains("\"namespace\":\"default\"");
        assertThat(json).contains("\"nginx\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> Pod.builder()
                .namespace("default")
                .spec(PodSpec.builder()
                        .container(Container.builder()
                                .name("test")
                                .image("test")
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pod name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> Pod.builder()
                .name("test-pod")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pod spec is required");
    }
}
