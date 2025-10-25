package io.elev8.resources.pod;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContainerTest {

    @Test
    void shouldBuildContainerWithRequiredFields() {
        final Container container = Container.builder()
                .name("nginx")
                .image("nginx:latest")
                .build();

        assertThat(container.getName()).isEqualTo("nginx");
        assertThat(container.getImage()).isEqualTo("nginx:latest");
    }

    @Test
    void shouldBuildContainerWithAllFields() {
        final Container container = Container.builder()
                .name("app")
                .image("app:1.0")
                .command("sh", "-c", "echo hello")
                .args("arg1", "arg2")
                .workingDir("/app")
                .addPort(8080)
                .addEnv("KEY", "value")
                .imagePullPolicy("Always")
                .build();

        assertThat(container.getName()).isEqualTo("app");
        assertThat(container.getImage()).isEqualTo("app:1.0");
        assertThat(container.getCommand()).containsExactly("sh", "-c", "echo hello");
        assertThat(container.getArgs()).containsExactly("arg1", "arg2");
        assertThat(container.getWorkingDir()).isEqualTo("/app");
        assertThat(container.getPorts()).hasSize(1);
        assertThat(container.getEnv()).hasSize(1);
        assertThat(container.getImagePullPolicy()).isEqualTo("Always");
    }

    @Test
    void shouldAddMultiplePorts() {
        final Container container = Container.builder()
                .name("app")
                .image("app:1.0")
                .addPort(8080)
                .addPort(Container.ContainerPort.builder()
                        .name("metrics")
                        .containerPort(9090)
                        .protocol("TCP")
                        .build())
                .build();

        assertThat(container.getPorts()).hasSize(2);
        assertThat(container.getPorts().get(0).getContainerPort()).isEqualTo(8080);
        assertThat(container.getPorts().get(1).getContainerPort()).isEqualTo(9090);
    }

    @Test
    void shouldAddMultipleEnvVars() {
        final Container container = Container.builder()
                .name("app")
                .image("app:1.0")
                .addEnv("KEY1", "value1")
                .addEnv("KEY2", "value2")
                .build();

        assertThat(container.getEnv()).hasSize(2);
        assertThat(container.getEnv().get(0).getName()).isEqualTo("KEY1");
        assertThat(container.getEnv().get(1).getName()).isEqualTo("KEY2");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> Container.builder()
                .image("nginx:latest")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Container name is required");
    }

    @Test
    void shouldThrowExceptionWhenImageIsNull() {
        assertThatThrownBy(() -> Container.builder()
                .name("nginx")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Container image is required");
    }
}
