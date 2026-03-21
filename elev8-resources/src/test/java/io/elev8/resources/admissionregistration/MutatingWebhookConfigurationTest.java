package io.elev8.resources.admissionregistration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.elev8.resources.crd.conversion.ServiceReference;
import io.elev8.resources.crd.conversion.WebhookClientConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MutatingWebhookConfigurationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildWithRequiredFields() {
        final MutatingWebhookConfiguration config = MutatingWebhookConfiguration.builder()
                .name("sidecar-injector")
                .build();

        assertThat(config.getApiVersion()).isEqualTo("admissionregistration.k8s.io/v1");
        assertThat(config.getKind()).isEqualTo("MutatingWebhookConfiguration");
        assertThat(config.getName()).isEqualTo("sidecar-injector");
    }

    @Test
    void shouldBuildWithWebhooks() {
        final MutatingWebhookConfiguration config = MutatingWebhookConfiguration.builder()
                .name("sidecar-injector")
                .webhook(MutatingWebhook.builder()
                        .name("sidecar-injector.example.com")
                        .clientConfig(WebhookClientConfig.builder()
                                .service(ServiceReference.builder()
                                        .namespace("istio-system")
                                        .name("istio-sidecar-injector")
                                        .path("/inject")
                                        .port(443)
                                        .build())
                                .build())
                        .sideEffects("None")
                        .admissionReviewVersion("v1")
                        .rule(RuleWithOperations.builder()
                                .operation("CREATE")
                                .apiGroup("")
                                .apiVersion("v1")
                                .resource("pods")
                                .build())
                        .build())
                .build();

        assertThat(config.getWebhooks()).hasSize(1);
        assertThat(config.getWebhooks().get(0).getName()).isEqualTo("sidecar-injector.example.com");
    }

    @Test
    void shouldBuildWithLabelsAndAnnotations() {
        final MutatingWebhookConfiguration config = MutatingWebhookConfiguration.builder()
                .name("my-webhook")
                .label("app", "webhook-controller")
                .annotation("cert-manager.io/inject-ca-from", "webhook-system/webhook-cert")
                .build();

        assertThat(config.getMetadata().getLabels()).containsEntry("app", "webhook-controller");
        assertThat(config.getMetadata().getAnnotations())
                .containsEntry("cert-manager.io/inject-ca-from", "webhook-system/webhook-cert");
    }

    @Test
    void shouldBuildWithMetadata() {
        final MutatingWebhookConfiguration config = MutatingWebhookConfiguration.builder()
                .name("test-webhook")
                .metadata(io.elev8.resources.Metadata.builder()
                        .name("test-webhook")
                        .label("managed-by", "elev8")
                        .build())
                .build();

        assertThat(config.getName()).isEqualTo("test-webhook");
        assertThat(config.getMetadata().getLabels()).containsEntry("managed-by", "elev8");
    }

    @Test
    void shouldBuildWithMultipleWebhooks() {
        final MutatingWebhookConfiguration config = MutatingWebhookConfiguration.builder()
                .name("multi-webhook")
                .webhook(MutatingWebhook.builder()
                        .name("first.example.com")
                        .sideEffects("None")
                        .admissionReviewVersion("v1")
                        .build())
                .webhook(MutatingWebhook.builder()
                        .name("second.example.com")
                        .sideEffects("None")
                        .admissionReviewVersion("v1")
                        .build())
                .build();

        assertThat(config.getWebhooks()).hasSize(2);
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> MutatingWebhookConfiguration.builder().build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MutatingWebhookConfiguration name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> MutatingWebhookConfiguration.builder().name("").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MutatingWebhookConfiguration name is required");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final MutatingWebhookConfiguration config = MutatingWebhookConfiguration.builder()
                .name("test-mutating")
                .webhook(MutatingWebhook.builder()
                        .name("test.example.com")
                        .sideEffects("None")
                        .admissionReviewVersion("v1")
                        .build())
                .build();

        final String json = objectMapper.writeValueAsString(config);

        assertThat(json).contains("\"apiVersion\":\"admissionregistration.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"MutatingWebhookConfiguration\"");
        assertThat(json).contains("\"name\":\"test-mutating\"");
        assertThat(json).contains("\"webhooks\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "apiVersion": "admissionregistration.k8s.io/v1",
                  "kind": "MutatingWebhookConfiguration",
                  "metadata": {
                    "name": "sidecar-injector"
                  },
                  "webhooks": [
                    {
                      "name": "sidecar.example.com",
                      "clientConfig": {
                        "service": {
                          "namespace": "webhook-ns",
                          "name": "webhook-svc",
                          "path": "/inject"
                        }
                      },
                      "rules": [
                        {
                          "operations": ["CREATE"],
                          "apiGroups": [""],
                          "apiVersions": ["v1"],
                          "resources": ["pods"]
                        }
                      ],
                      "sideEffects": "None",
                      "admissionReviewVersions": ["v1"],
                      "reinvocationPolicy": "IfNeeded"
                    }
                  ]
                }
                """;

        final MutatingWebhookConfiguration config = objectMapper.readValue(json, MutatingWebhookConfiguration.class);

        assertThat(config.getApiVersion()).isEqualTo("admissionregistration.k8s.io/v1");
        assertThat(config.getKind()).isEqualTo("MutatingWebhookConfiguration");
        assertThat(config.getName()).isEqualTo("sidecar-injector");
        assertThat(config.getWebhooks()).hasSize(1);
        assertThat(config.getWebhooks().get(0).getName()).isEqualTo("sidecar.example.com");
        assertThat(config.getWebhooks().get(0).getReinvocationPolicy()).isEqualTo("IfNeeded");
    }

    @Test
    void shouldHaveDefaultConstructor() {
        final MutatingWebhookConfiguration config = new MutatingWebhookConfiguration();

        assertThat(config.getApiVersion()).isEqualTo("admissionregistration.k8s.io/v1");
        assertThat(config.getKind()).isEqualTo("MutatingWebhookConfiguration");
    }
}
