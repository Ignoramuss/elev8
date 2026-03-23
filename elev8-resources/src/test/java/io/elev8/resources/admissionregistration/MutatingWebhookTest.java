package io.elev8.resources.admissionregistration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.elev8.resources.LabelSelector;
import io.elev8.resources.crd.conversion.ServiceReference;
import io.elev8.resources.crd.conversion.WebhookClientConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MutatingWebhookTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildWithAllFields() {
        final MutatingWebhook webhook = MutatingWebhook.builder()
                .name("my-mutating-webhook.example.com")
                .clientConfig(WebhookClientConfig.builder()
                        .service(ServiceReference.builder()
                                .namespace("webhook-system")
                                .name("webhook-service")
                                .path("/mutate")
                                .port(443)
                                .build())
                        .caBundle("LS0tLS1...")
                        .build())
                .rule(RuleWithOperations.builder()
                        .operation("CREATE")
                        .operation("UPDATE")
                        .apiGroup("")
                        .apiVersion("v1")
                        .resource("pods")
                        .build())
                .failurePolicy("Fail")
                .matchPolicy("Equivalent")
                .namespaceSelector(LabelSelector.builder()
                        .matchLabel("env", "prod")
                        .build())
                .objectSelector(LabelSelector.builder()
                        .matchLabel("inject", "true")
                        .build())
                .sideEffects("None")
                .timeoutSeconds(10)
                .admissionReviewVersion("v1")
                .admissionReviewVersion("v1beta1")
                .reinvocationPolicy("IfNeeded")
                .matchCondition(MatchCondition.builder()
                        .name("exclude-system")
                        .expression("!request.userInfo.username.startsWith('system:')")
                        .build())
                .build();

        assertThat(webhook.getName()).isEqualTo("my-mutating-webhook.example.com");
        assertThat(webhook.getClientConfig().getService().getNamespace()).isEqualTo("webhook-system");
        assertThat(webhook.getClientConfig().getService().getPath()).isEqualTo("/mutate");
        assertThat(webhook.getRules()).hasSize(1);
        assertThat(webhook.getRules().get(0).getOperations()).containsExactly("CREATE", "UPDATE");
        assertThat(webhook.getFailurePolicy()).isEqualTo("Fail");
        assertThat(webhook.getMatchPolicy()).isEqualTo("Equivalent");
        assertThat(webhook.getNamespaceSelector().getMatchLabels()).containsEntry("env", "prod");
        assertThat(webhook.getObjectSelector().getMatchLabels()).containsEntry("inject", "true");
        assertThat(webhook.getSideEffects()).isEqualTo("None");
        assertThat(webhook.getTimeoutSeconds()).isEqualTo(10);
        assertThat(webhook.getAdmissionReviewVersions()).containsExactly("v1", "v1beta1");
        assertThat(webhook.getReinvocationPolicy()).isEqualTo("IfNeeded");
        assertThat(webhook.getMatchConditions()).hasSize(1);
    }

    @Test
    void shouldBuildWithUrlClientConfig() {
        final MutatingWebhook webhook = MutatingWebhook.builder()
                .name("external-webhook.example.com")
                .clientConfig(WebhookClientConfig.builder()
                        .url("https://external-webhook.example.com/mutate")
                        .caBundle("LS0tLS1...")
                        .build())
                .sideEffects("None")
                .admissionReviewVersion("v1")
                .build();

        assertThat(webhook.getClientConfig().getUrl()).isEqualTo("https://external-webhook.example.com/mutate");
        assertThat(webhook.getClientConfig().getService()).isNull();
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final MutatingWebhook webhook = MutatingWebhook.builder()
                .name("sidecar-injector.example.com")
                .clientConfig(WebhookClientConfig.builder()
                        .service(ServiceReference.builder()
                                .namespace("istio-system")
                                .name("istio-sidecar-injector")
                                .path("/inject")
                                .build())
                        .build())
                .sideEffects("None")
                .admissionReviewVersion("v1")
                .reinvocationPolicy("Never")
                .build();

        final String json = objectMapper.writeValueAsString(webhook);

        assertThat(json).contains("\"name\":\"sidecar-injector.example.com\"");
        assertThat(json).contains("\"reinvocationPolicy\":\"Never\"");
        assertThat(json).contains("\"sideEffects\":\"None\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "name": "mutating-webhook.example.com",
                  "clientConfig": {
                    "service": {
                      "namespace": "default",
                      "name": "webhook-svc",
                      "path": "/mutate",
                      "port": 443
                    },
                    "caBundle": "dGVzdA=="
                  },
                  "rules": [
                    {
                      "operations": ["CREATE"],
                      "apiGroups": [""],
                      "apiVersions": ["v1"],
                      "resources": ["pods"]
                    }
                  ],
                  "failurePolicy": "Ignore",
                  "sideEffects": "None",
                  "timeoutSeconds": 5,
                  "admissionReviewVersions": ["v1"],
                  "reinvocationPolicy": "Never"
                }
                """;

        final MutatingWebhook webhook = objectMapper.readValue(json, MutatingWebhook.class);

        assertThat(webhook.getName()).isEqualTo("mutating-webhook.example.com");
        assertThat(webhook.getClientConfig().getService().getPort()).isEqualTo(443);
        assertThat(webhook.getClientConfig().getCaBundle()).isEqualTo("dGVzdA==");
        assertThat(webhook.getRules()).hasSize(1);
        assertThat(webhook.getFailurePolicy()).isEqualTo("Ignore");
        assertThat(webhook.getTimeoutSeconds()).isEqualTo(5);
        assertThat(webhook.getReinvocationPolicy()).isEqualTo("Never");
    }
}
