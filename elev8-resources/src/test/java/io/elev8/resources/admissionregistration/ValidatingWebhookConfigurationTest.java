package io.elev8.resources.admissionregistration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.elev8.resources.crd.conversion.ServiceReference;
import io.elev8.resources.crd.conversion.WebhookClientConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidatingWebhookConfigurationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildWithRequiredFields() {
        final ValidatingWebhookConfiguration config = ValidatingWebhookConfiguration.builder()
                .name("policy-checker")
                .build();

        assertThat(config.getApiVersion()).isEqualTo("admissionregistration.k8s.io/v1");
        assertThat(config.getKind()).isEqualTo("ValidatingWebhookConfiguration");
        assertThat(config.getName()).isEqualTo("policy-checker");
    }

    @Test
    void shouldBuildWithWebhooks() {
        final ValidatingWebhookConfiguration config = ValidatingWebhookConfiguration.builder()
                .name("policy-checker")
                .webhook(ValidatingWebhook.builder()
                        .name("policy-checker.example.com")
                        .clientConfig(WebhookClientConfig.builder()
                                .service(ServiceReference.builder()
                                        .namespace("policy-system")
                                        .name("policy-service")
                                        .path("/validate")
                                        .port(443)
                                        .build())
                                .build())
                        .sideEffects("None")
                        .admissionReviewVersion("v1")
                        .rule(RuleWithOperations.builder()
                                .operation("CREATE")
                                .operation("UPDATE")
                                .apiGroup("apps")
                                .apiVersion("v1")
                                .resource("deployments")
                                .build())
                        .failurePolicy("Fail")
                        .build())
                .build();

        assertThat(config.getWebhooks()).hasSize(1);
        assertThat(config.getWebhooks().get(0).getName()).isEqualTo("policy-checker.example.com");
        assertThat(config.getWebhooks().get(0).getFailurePolicy()).isEqualTo("Fail");
    }

    @Test
    void shouldBuildWithLabelsAndAnnotations() {
        final ValidatingWebhookConfiguration config = ValidatingWebhookConfiguration.builder()
                .name("my-validator")
                .label("app", "policy-engine")
                .annotation("cert-manager.io/inject-ca-from", "policy-system/policy-cert")
                .build();

        assertThat(config.getMetadata().getLabels()).containsEntry("app", "policy-engine");
        assertThat(config.getMetadata().getAnnotations())
                .containsEntry("cert-manager.io/inject-ca-from", "policy-system/policy-cert");
    }

    @Test
    void shouldBuildWithMultipleWebhooks() {
        final ValidatingWebhookConfiguration config = ValidatingWebhookConfiguration.builder()
                .name("multi-validator")
                .webhook(ValidatingWebhook.builder()
                        .name("first.example.com")
                        .sideEffects("None")
                        .admissionReviewVersion("v1")
                        .build())
                .webhook(ValidatingWebhook.builder()
                        .name("second.example.com")
                        .sideEffects("None")
                        .admissionReviewVersion("v1")
                        .build())
                .build();

        assertThat(config.getWebhooks()).hasSize(2);
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> ValidatingWebhookConfiguration.builder().build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ValidatingWebhookConfiguration name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> ValidatingWebhookConfiguration.builder().name("").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ValidatingWebhookConfiguration name is required");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final ValidatingWebhookConfiguration config = ValidatingWebhookConfiguration.builder()
                .name("test-validating")
                .webhook(ValidatingWebhook.builder()
                        .name("test.example.com")
                        .sideEffects("None")
                        .admissionReviewVersion("v1")
                        .build())
                .build();

        final String json = objectMapper.writeValueAsString(config);

        assertThat(json).contains("\"apiVersion\":\"admissionregistration.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"ValidatingWebhookConfiguration\"");
        assertThat(json).contains("\"name\":\"test-validating\"");
        assertThat(json).contains("\"webhooks\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "apiVersion": "admissionregistration.k8s.io/v1",
                  "kind": "ValidatingWebhookConfiguration",
                  "metadata": {
                    "name": "pod-policy"
                  },
                  "webhooks": [
                    {
                      "name": "pod-policy.example.com",
                      "clientConfig": {
                        "url": "https://policy.example.com/validate"
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
                      "failurePolicy": "Fail",
                      "matchConditions": [
                        {
                          "name": "exclude-kube-system",
                          "expression": "object.metadata.namespace != 'kube-system'"
                        }
                      ]
                    }
                  ]
                }
                """;

        final ValidatingWebhookConfiguration config = objectMapper.readValue(json, ValidatingWebhookConfiguration.class);

        assertThat(config.getApiVersion()).isEqualTo("admissionregistration.k8s.io/v1");
        assertThat(config.getKind()).isEqualTo("ValidatingWebhookConfiguration");
        assertThat(config.getName()).isEqualTo("pod-policy");
        assertThat(config.getWebhooks()).hasSize(1);
        assertThat(config.getWebhooks().get(0).getMatchConditions()).hasSize(1);
        assertThat(config.getWebhooks().get(0).getClientConfig().getUrl())
                .isEqualTo("https://policy.example.com/validate");
    }

    @Test
    void shouldHaveDefaultConstructor() {
        final ValidatingWebhookConfiguration config = new ValidatingWebhookConfiguration();

        assertThat(config.getApiVersion()).isEqualTo("admissionregistration.k8s.io/v1");
        assertThat(config.getKind()).isEqualTo("ValidatingWebhookConfiguration");
    }
}
