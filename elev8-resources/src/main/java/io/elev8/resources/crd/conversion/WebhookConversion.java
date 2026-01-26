package io.elev8.resources.crd.conversion;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * WebhookConversion describes how to call a conversion webhook.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookConversion {
    private WebhookClientConfig clientConfig;

    @Singular
    private List<String> conversionReviewVersions;
}
