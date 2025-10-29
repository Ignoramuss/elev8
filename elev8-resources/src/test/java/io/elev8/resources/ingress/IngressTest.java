package io.elev8.resources.ingress;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IngressTest {

    @Test
    void shouldBuildIngressWithRequiredFields() {
        final IngressSpec spec = IngressSpec.builder()
                .ingressClassName("nginx")
                .rule(IngressRule.builder()
                        .host("example.com")
                        .http(HTTPIngressRuleValue.builder()
                                .path(HTTPIngressPath.builder()
                                        .path("/")
                                        .pathType("Prefix")
                                        .backend(IngressBackend.builder()
                                                .service(IngressServiceBackend.builder()
                                                        .name("example-service")
                                                        .port(ServiceBackendPort.builder()
                                                                .number(80)
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final Ingress ingress = Ingress.builder()
                .name("example-ingress")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(ingress.getApiVersion()).isEqualTo("networking.k8s.io/v1");
        assertThat(ingress.getKind()).isEqualTo("Ingress");
        assertThat(ingress.getName()).isEqualTo("example-ingress");
        assertThat(ingress.getNamespace()).isEqualTo("default");
        assertThat(ingress.getSpec()).isEqualTo(spec);
    }

    @Test
    void shouldBuildIngressWithLabels() {
        final Ingress ingress = Ingress.builder()
                .name("example-ingress")
                .namespace("default")
                .label("app", "web")
                .label("env", "prod")
                .spec(IngressSpec.builder()
                        .ingressClassName("nginx")
                        .build())
                .build();

        assertThat(ingress.getMetadata().getLabels()).containsEntry("app", "web");
        assertThat(ingress.getMetadata().getLabels()).containsEntry("env", "prod");
    }

    @Test
    void shouldBuildIngressWithTLS() {
        final IngressSpec spec = IngressSpec.builder()
                .ingressClassName("nginx")
                .tl(IngressTLS.builder()
                        .host("example.com")
                        .secretName("example-tls")
                        .build())
                .build();

        final Ingress ingress = Ingress.builder()
                .name("example-ingress")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(ingress.getSpec().getTls()).hasSize(1);
        assertThat(ingress.getSpec().getTls().get(0).getHosts()).containsExactly("example.com");
        assertThat(ingress.getSpec().getTls().get(0).getSecretName()).isEqualTo("example-tls");
    }

    @Test
    void shouldBuildIngressWithMultipleRules() {
        final IngressSpec spec = IngressSpec.builder()
                .ingressClassName("nginx")
                .rule(IngressRule.builder()
                        .host("example.com")
                        .http(HTTPIngressRuleValue.builder()
                                .path(HTTPIngressPath.builder()
                                        .path("/api")
                                        .pathType("Prefix")
                                        .backend(IngressBackend.builder()
                                                .service(IngressServiceBackend.builder()
                                                        .name("api-service")
                                                        .port(ServiceBackendPort.builder()
                                                                .number(8080)
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .rule(IngressRule.builder()
                        .host("app.example.com")
                        .http(HTTPIngressRuleValue.builder()
                                .path(HTTPIngressPath.builder()
                                        .path("/")
                                        .pathType("Prefix")
                                        .backend(IngressBackend.builder()
                                                .service(IngressServiceBackend.builder()
                                                        .name("app-service")
                                                        .port(ServiceBackendPort.builder()
                                                                .number(80)
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final Ingress ingress = Ingress.builder()
                .name("example-ingress")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(ingress.getSpec().getRules()).hasSize(2);
        assertThat(ingress.getSpec().getRules().get(0).getHost()).isEqualTo("example.com");
        assertThat(ingress.getSpec().getRules().get(1).getHost()).isEqualTo("app.example.com");
    }

    @Test
    void shouldBuildIngressWithDefaultBackend() {
        final IngressSpec spec = IngressSpec.builder()
                .ingressClassName("nginx")
                .defaultBackend(IngressBackend.builder()
                        .service(IngressServiceBackend.builder()
                                .name("default-service")
                                .port(ServiceBackendPort.builder()
                                        .number(80)
                                        .build())
                                .build())
                        .build())
                .build();

        final Ingress ingress = Ingress.builder()
                .name("example-ingress")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(ingress.getSpec().getDefaultBackend()).isNotNull();
        assertThat(ingress.getSpec().getDefaultBackend().getService().getName()).isEqualTo("default-service");
    }

    @Test
    void shouldBuildIngressWithStatus() {
        final IngressStatus status = IngressStatus.builder()
                .loadBalancer(IngressLoadBalancerStatus.builder()
                        .ingress(IngressLoadBalancerIngress.builder()
                                .ip("192.168.1.1")
                                .build())
                        .build())
                .build();

        final Ingress ingress = Ingress.builder()
                .name("example-ingress")
                .namespace("default")
                .spec(IngressSpec.builder()
                        .ingressClassName("nginx")
                        .build())
                .status(status)
                .build();

        assertThat(ingress.getStatus()).isNotNull();
        assertThat(ingress.getStatus().getLoadBalancer()).isNotNull();
    }

    @Test
    void shouldSerializeToJson() {
        final Ingress ingress = Ingress.builder()
                .name("example-ingress")
                .namespace("default")
                .spec(IngressSpec.builder()
                        .ingressClassName("nginx")
                        .rule(IngressRule.builder()
                                .host("example.com")
                                .http(HTTPIngressRuleValue.builder()
                                        .path(HTTPIngressPath.builder()
                                                .path("/")
                                                .pathType("Prefix")
                                                .backend(IngressBackend.builder()
                                                        .service(IngressServiceBackend.builder()
                                                                .name("example-service")
                                                                .port(ServiceBackendPort.builder()
                                                                        .number(80)
                                                                        .build())
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final String json = ingress.toJson();

        assertThat(json).contains("\"apiVersion\":\"networking.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"Ingress\"");
        assertThat(json).contains("\"name\":\"example-ingress\"");
        assertThat(json).contains("\"ingressClassName\":\"nginx\"");
        assertThat(json).contains("\"rules\"");
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() {
        final Ingress ingress = Ingress.builder()
                .name("example-ingress")
                .namespace("default")
                .spec(IngressSpec.builder()
                        .ingressClassName("nginx")
                        .build())
                .build();

        final String json = ingress.toJson();

        assertThat(json).doesNotContain("\"status\"");
        assertThat(json).doesNotContain("\"defaultBackend\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> Ingress.builder()
                .namespace("default")
                .spec(IngressSpec.builder()
                        .ingressClassName("nginx")
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ingress name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> Ingress.builder()
                .name("")
                .namespace("default")
                .spec(IngressSpec.builder()
                        .ingressClassName("nginx")
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ingress name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> Ingress.builder()
                .name("example-ingress")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ingress spec is required");
    }

    @Test
    void shouldAllowIngressWithoutNamespace() {
        final Ingress ingress = Ingress.builder()
                .name("example-ingress")
                .spec(IngressSpec.builder()
                        .ingressClassName("nginx")
                        .build())
                .build();

        assertThat(ingress.getName()).isEqualTo("example-ingress");
        assertThat(ingress.getNamespace()).isNull();
    }
}
