package io.elev8.resources.crd;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.Condition;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * CustomResourceDefinitionCondition contains details for the current condition of this pod.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CRDCondition implements Condition {
    private String type;
    private String status;
    private String lastTransitionTime;
    private String reason;
    private String message;
}
