package io.elev8.resources.admissionregistration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.elev8.resources.LabelSelector;
import io.elev8.resources.crd.conversion.ServiceReference;
import io.elev8.resources.crd.conversion.WebhookClientConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatingWebhookTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildWithAllFields() {
        final ValidatingWebhook webhook = ValidatingWebhook.builder()
                .name("my-validating-webhook.example.com")
                .clientConfig(WebhookClientConfig.builder()
                        .service(ServiceReference.builder()
                                .namespace("webhook-system")
                                .name("webhook-service")
                                .path("/validate")
                                .port(443)
                                .build())
                        .caBundle("LS0tLS1...")
                        .build())
                .rule(RuleWithOperations.builder()
                        .operation("CREATE")
                        .operation("UPDATE")
                        .apiGroup("apps")
                        .apiVersion("v1")
                        .resource("deployments")
                        .build())
                .failurePolicy("Fail")
                .matchPolicy("Exact")
                .namespaceSelector(LabelSelector.builder()
                        .matchLabel("env", "prod")
                        .build())
                .objectSelector(LabelSelector.builder()
                        .matchLabel("validate", "true")
                        .build())
                .sideEffects("None")
                .timeoutSeconds(15)
                .admissionReviewVersion("v1")
                .admissionReviewVersion("v1beta1")
                .matchCondition(MatchCondition.builder()
                        .name("only-pods-with-label")
                        .expression("has(object.metadata.labels) && 'validate' in object.metadata.labels")
                        .build())
                .build();

        assertThat(webhook.getName()).isEqualTo("my-validating-webhook.example.com");
        assertThat(webhook.getClientConfig().getService().getPath()).isEqualTo("/validate");
        assertThat(webhook.getRules()).hasSize(1);
        assertThat(webhook.getRules().get(0).getResources()).containsExactly("deployments");
        assertThat(webhook.getFailurePolicy()).isEqualTo("Fail");
        assertThat(webhook.getMatchPolicy()).isEqualTo("Exact");
        assertThat(webhook.getSideEffects()).isEqualTo("None");
        assertThat(webhook.getTimeoutSeconds()).isEqualTo(15);
        assertThat(webhook.getAdmissionReviewVersions()).containsExactly("v1", "v1beta1");
        assertThat(webhook.getMatchConditions()).hasSize(1);
    }

    @Test
    void shouldNotHaveReinvocationPolicy() throws Exception {
        final ValidatingWebhook webhook = ValidatingWebhook.builder()
                .name("test-webhook.example.com")
                .sideEffects("None")
                .admissionReviewVersion("v1")
                .build();

        final String json = objectMapper.writeValueAsString(webhook);

        assertThat(json).doesNotContain("reinvocationPolicy");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final ValidatingWebhook webhook = ValidatingWebhook.builder()
                .name("policy-check.example.com")
                .clientConfig(WebhookClientConfig.builder()
                        .url("https://policy.example.com/validate")
                        .build())
                .sideEffects("None")
                .admissionReviewVersion("v1")
                .failurePolicy("Fail")
                .build();

        final String json = objectMapper.writeValueAsString(webhook);

        assertThat(json).contains("\"name\":\"policy-check.example.com\"");
        assertThat(json).contains("\"failurePolicy\":\"Fail\"");
        assertThat(json).contains("\"url\":\"https://policy.example.com/validate\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "name": "validating-webhook.example.com",
                  "clientConfig": {
                    "service": {
                      "namespace": "default",
                      "name": "webhook-svc",
                      "path": "/validate",
                      "port": 443
                    }
                  },
                  "rules": [
                    {
                      "operations": ["CREATE", "UPDATE", "DELETE"],
                      "apiGroups": ["apps"],
                      "apiVersions": ["v1"],
                      "resources": ["deployments"]
                    }
                  ],
                  "failurePolicy": "Fail",
                  "sideEffects": "None",
                  "timeoutSeconds": 30,
                  "admissionReviewVersions": ["v1"],
                  "matchConditions": [
                    {
                      "name": "production-only",
                      "expression": "object.metadata.namespace == 'production'"
                    }
                  ]
                }
                """;

        final ValidatingWebhook webhook = objectMapper.readValue(json, ValidatingWebhook.class);

        assertThat(webhook.getName()).isEqualTo("validating-webhook.example.com");
        assertThat(webhook.getRules()).hasSize(1);
        assertThat(webhook.getRules().get(0).getOperations()).containsExactly("CREATE", "UPDATE", "DELETE");
        assertThat(webhook.getTimeoutSeconds()).isEqualTo(30);
        assertThat(webhook.getMatchConditions()).hasSize(1);
        assertThat(webhook.getMatchConditions().get(0).getName()).isEqualTo("production-only");
    }
}
