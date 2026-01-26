package io.elev8.resources.crd.conversion;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * CustomResourceConversion describes how to convert different versions of a CR.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomResourceConversion {
    private String strategy;
    private WebhookConversion webhook;
}
