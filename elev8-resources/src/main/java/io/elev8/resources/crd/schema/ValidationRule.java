package io.elev8.resources.crd.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * ValidationRule describes a validation rule written in the CEL expression language.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationRule {
    private String rule;
    private String message;
    private String messageExpression;
    private String fieldPath;
    private String reason;
}
