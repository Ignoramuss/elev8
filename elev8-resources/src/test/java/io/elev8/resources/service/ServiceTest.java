package io.elev8.resources.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceTest {

    @Test
    void shouldBuildServiceWithRequiredFields() {
        final ServiceSpec spec = ServiceSpec.builder()
                .addSelector("app", "test")
                .addPort(80, 8080)
                .build();

        final Service service = Service.builder()
                .name("test-service")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(service.getApiVersion()).isEqualTo("v1");
        assertThat(service.getKind()).isEqualTo("Service");
        assertThat(service.getName()).isEqualTo("test-service");
        assertThat(service.getNamespace()).isEqualTo("default");
        assertThat(service.getSpec()).isEqualTo(spec);
    }

    @Test
    void shouldBuildServiceWithLabels() {
        final Service service = Service.builder()
                .name("test-service")
                .namespace("default")
                .label("app", "test")
                .label("env", "dev")
                .spec(ServiceSpec.builder()
                        .addSelector("app", "test")
                        .addPort(80, 8080)
                        .build())
                .build();

        assertThat(service.getMetadata().getLabels()).containsEntry("app", "test");
        assertThat(service.getMetadata().getLabels()).containsEntry("env", "dev");
    }

    @Test
    void shouldSerializeToJson() {
        final Service service = Service.builder()
                .name("test-service")
                .namespace("default")
                .spec(ServiceSpec.builder()
                        .addSelector("app", "test")
                        .addPort(80, 8080)
                        .type("ClusterIP")
                        .build())
                .build();

        final String json = service.toJson();

        assertThat(json).contains("\"apiVersion\":\"v1\"");
        assertThat(json).contains("\"kind\":\"Service\"");
        assertThat(json).contains("\"name\":\"test-service\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> Service.builder()
                .namespace("default")
                .spec(ServiceSpec.builder()
                        .addSelector("app", "test")
                        .addPort(80, 8080)
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Service name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> Service.builder()
                .name("test-service")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Service spec is required");
    }
}
