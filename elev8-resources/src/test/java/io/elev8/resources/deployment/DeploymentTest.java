package io.elev8.resources.deployment;

import io.elev8.resources.pod.Container;
import io.elev8.resources.pod.PodSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeploymentTest {

    @Test
    void shouldBuildDeploymentWithRequiredFields() {
        final Container container = Container.builder()
                .name("nginx")
                .image("nginx:latest")
                .build();

        final DeploymentSpec spec = DeploymentSpec.builder()
                .replicas(3)
                .addSelector("app", "test")
                .template(DeploymentSpec.PodTemplateSpec.builder()
                        .label("app", "test")
                        .spec(PodSpec.builder()
                                .addContainer(container)
                                .build())
                        .build())
                .build();

        final Deployment deployment = Deployment.builder()
                .name("test-deployment")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(deployment.getApiVersion()).isEqualTo("apps/v1");
        assertThat(deployment.getKind()).isEqualTo("Deployment");
        assertThat(deployment.getName()).isEqualTo("test-deployment");
        assertThat(deployment.getNamespace()).isEqualTo("default");
        assertThat(deployment.getSpec()).isEqualTo(spec);
    }

    @Test
    void shouldBuildDeploymentWithLabels() {
        final Deployment deployment = Deployment.builder()
                .name("test-deployment")
                .namespace("default")
                .label("app", "test")
                .label("env", "prod")
                .spec(DeploymentSpec.builder()
                        .replicas(2)
                        .addSelector("app", "test")
                        .template(DeploymentSpec.PodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .addContainer(Container.builder()
                                                .name("app")
                                                .image("app:1.0")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(deployment.getMetadata().getLabels()).containsEntry("app", "test");
        assertThat(deployment.getMetadata().getLabels()).containsEntry("env", "prod");
    }

    @Test
    void shouldSerializeToJson() {
        final Deployment deployment = Deployment.builder()
                .name("test-deployment")
                .namespace("default")
                .spec(DeploymentSpec.builder()
                        .replicas(3)
                        .addSelector("app", "test")
                        .template(DeploymentSpec.PodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .addContainer(Container.builder()
                                                .name("nginx")
                                                .image("nginx:latest")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final String json = deployment.toJson();

        assertThat(json).contains("\"apiVersion\":\"apps/v1\"");
        assertThat(json).contains("\"kind\":\"Deployment\"");
        assertThat(json).contains("\"name\":\"test-deployment\"");
        assertThat(json).contains("\"replicas\":3");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> Deployment.builder()
                .namespace("default")
                .spec(DeploymentSpec.builder()
                        .replicas(1)
                        .addSelector("app", "test")
                        .template(DeploymentSpec.PodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .addContainer(Container.builder()
                                                .name("test")
                                                .image("test")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deployment name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> Deployment.builder()
                .name("test-deployment")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deployment spec is required");
    }
}
