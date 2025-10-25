package io.elev8.resources.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceSpecTest {

    @Test
    void shouldBuildServiceSpecWithRequiredFields() {
        final ServiceSpec spec = ServiceSpec.builder()
                .addSelector("app", "test")
                .addPort(80, 8080)
                .build();

        assertThat(spec.getSelector()).containsEntry("app", "test");
        assertThat(spec.getPorts()).hasSize(1);
        assertThat(spec.getType()).isEqualTo("ClusterIP");
        assertThat(spec.getSessionAffinity()).isEqualTo("None");
    }

    @Test
    void shouldBuildServiceSpecWithAllFields() {
        final ServiceSpec spec = ServiceSpec.builder()
                .selector(Map.of("app", "test", "version", "1.0"))
                .addPort(ServiceSpec.ServicePort.builder()
                        .name("http")
                        .port(80)
                        .targetPort(8080)
                        .protocol("TCP")
                        .build())
                .type("LoadBalancer")
                .clusterIP("10.0.0.1")
                .sessionAffinity("ClientIP")
                .loadBalancerIP("52.1.2.3")
                .build();

        assertThat(spec.getSelector()).hasSize(2);
        assertThat(spec.getPorts()).hasSize(1);
        assertThat(spec.getType()).isEqualTo("LoadBalancer");
        assertThat(spec.getClusterIP()).isEqualTo("10.0.0.1");
        assertThat(spec.getSessionAffinity()).isEqualTo("ClientIP");
        assertThat(spec.getLoadBalancerIP()).isEqualTo("52.1.2.3");
    }

    @Test
    void shouldAllowServiceWithoutSelector() {
        final ServiceSpec spec = ServiceSpec.builder()
                .addPort(80, 8080)
                .build();

        assertThat(spec.getSelector()).isNull();
        assertThat(spec.getPorts()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenNoPorts() {
        assertThatThrownBy(() -> ServiceSpec.builder()
                .addSelector("app", "test")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one port is required");
    }

    @Test
    void shouldBuildServicePort() {
        final ServiceSpec.ServicePort port = ServiceSpec.ServicePort.builder()
                .name("http")
                .port(80)
                .targetPort(8080)
                .protocol("TCP")
                .build();

        assertThat(port.getName()).isEqualTo("http");
        assertThat(port.getPort()).isEqualTo(80);
        assertThat(port.getTargetPort()).isEqualTo(8080);
        assertThat(port.getProtocol()).isEqualTo("TCP");
    }

    @Test
    void shouldThrowExceptionWhenPortNumberIsNull() {
        assertThatThrownBy(() -> ServiceSpec.ServicePort.builder()
                .name("http")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Service port is required");
    }
}
