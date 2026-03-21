package io.elev8.resources.admissionregistration;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.LabelSelector;
import io.elev8.resources.crd.conversion.WebhookClientConfig;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidatingWebhook {
    private String name;
    private WebhookClientConfig clientConfig;

    @Singular("rule")
    private List<RuleWithOperations> rules;

    private String failurePolicy;
    private String matchPolicy;
    private LabelSelector namespaceSelector;
    private LabelSelector objectSelector;
    private String sideEffects;
    private Integer timeoutSeconds;

    @Singular("admissionReviewVersion")
    private List<String> admissionReviewVersions;

    @Singular("matchCondition")
    private List<MatchCondition> matchConditions;
}
