package io.elev8.resources.pod;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PodSpecTest {

    @Test
    void shouldBuildPodSpecWithRequiredFields() {
        final Container container = Container.builder()
                .name("nginx")
                .image("nginx:latest")
                .build();

        final PodSpec spec = PodSpec.builder()
                .container(container)
                .build();

        assertThat(spec.getContainers()).hasSize(1);
        assertThat(spec.getContainers().get(0)).isEqualTo(container);
        assertThat(spec.getRestartPolicy()).isEqualTo("Always");
    }

    @Test
    void shouldBuildPodSpecWithAllFields() {
        final Container container1 = Container.builder()
                .name("app")
                .image("app:1.0")
                .build();

        final Container initContainer = Container.builder()
                .name("init")
                .image("init:1.0")
                .build();

        final PodSpec spec = PodSpec.builder()
                .container(container1)
                .initContainer(initContainer)
                .restartPolicy("OnFailure")
                .serviceAccountName("my-service-account")
                .nodeName("node-1")
                .terminationGracePeriodSeconds(30L)
                .build();

        assertThat(spec.getContainers()).hasSize(1);
        assertThat(spec.getInitContainers()).hasSize(1);
        assertThat(spec.getRestartPolicy()).isEqualTo("OnFailure");
        assertThat(spec.getServiceAccountName()).isEqualTo("my-service-account");
        assertThat(spec.getNodeName()).isEqualTo("node-1");
        assertThat(spec.getTerminationGracePeriodSeconds()).isEqualTo(30L);
    }

    @Test
    void shouldAllowEmptyContainers() {
        final PodSpec spec = PodSpec.builder().build();
        assertThat(spec.getContainers()).isNullOrEmpty();
    }
}
