package io.elev8.resources.ingress;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IngressSpecTest {

    @Test
    void shouldBuildIngressSpecWithIngressClassName() {
        final IngressSpec spec = IngressSpec.builder()
                .ingressClassName("nginx")
                .build();

        assertThat(spec.getIngressClassName()).isEqualTo("nginx");
    }

    @Test
    void shouldBuildIngressSpecWithDefaultBackend() {
        final IngressBackend backend = IngressBackend.builder()
                .service(IngressServiceBackend.builder()
                        .name("default-service")
                        .port(ServiceBackendPort.builder()
                                .number(80)
                                .build())
                        .build())
                .build();

        final IngressSpec spec = IngressSpec.builder()
                .defaultBackend(backend)
                .build();

        assertThat(spec.getDefaultBackend()).isEqualTo(backend);
    }

    @Test
    void shouldBuildIngressSpecWithSingleRule() {
        final IngressRule rule = IngressRule.builder()
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
                .build();

        final IngressSpec spec = IngressSpec.builder()
                .rule(rule)
                .build();

        assertThat(spec.getRules()).hasSize(1);
        assertThat(spec.getRules()).containsExactly(rule);
    }

    @Test
    void shouldBuildIngressSpecWithMultipleRules() {
        final IngressSpec spec = IngressSpec.builder()
                .rule(IngressRule.builder()
                        .host("example.com")
                        .build())
                .rule(IngressRule.builder()
                        .host("app.example.com")
                        .build())
                .build();

        assertThat(spec.getRules()).hasSize(2);
        assertThat(spec.getRules().get(0).getHost()).isEqualTo("example.com");
        assertThat(spec.getRules().get(1).getHost()).isEqualTo("app.example.com");
    }

    @Test
    void shouldBuildIngressSpecWithTLS() {
        final IngressTLS tls = IngressTLS.builder()
                .host("example.com")
                .secretName("example-tls")
                .build();

        final IngressSpec spec = IngressSpec.builder()
                .tl(tls)
                .build();

        assertThat(spec.getTls()).hasSize(1);
        assertThat(spec.getTls()).containsExactly(tls);
    }

    @Test
    void shouldBuildIngressSpecWithMultipleTLS() {
        final IngressSpec spec = IngressSpec.builder()
                .tl(IngressTLS.builder()
                        .host("example.com")
                        .secretName("example-tls")
                        .build())
                .tl(IngressTLS.builder()
                        .host("app.example.com")
                        .secretName("app-tls")
                        .build())
                .build();

        assertThat(spec.getTls()).hasSize(2);
    }

    @Test
    void shouldBuildCompleteIngressSpec() {
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
                .rule(IngressRule.builder()
                        .host("example.com")
                        .http(HTTPIngressRuleValue.builder()
                                .path(HTTPIngressPath.builder()
                                        .path("/")
                                        .pathType("Prefix")
                                        .backend(IngressBackend.builder()
                                                .service(IngressServiceBackend.builder()
                                                        .name("web-service")
                                                        .port(ServiceBackendPort.builder()
                                                                .number(80)
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .tl(IngressTLS.builder()
                        .host("example.com")
                        .secretName("example-tls")
                        .build())
                .build();

        assertThat(spec.getIngressClassName()).isEqualTo("nginx");
        assertThat(spec.getDefaultBackend()).isNotNull();
        assertThat(spec.getRules()).hasSize(1);
        assertThat(spec.getTls()).hasSize(1);
    }
}
