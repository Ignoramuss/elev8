package io.elev8.resources.horizontalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * HPAScalingRules configures the scaling behavior for one direction (scale up or scale down).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HPAScalingRules {
    /**
     * StabilizationWindowSeconds is the number of seconds for which past recommendations should be
     * considered while scaling up or down.
     * Must be greater than or equal to zero and less than or equal to 3600 (one hour).
     * If not set, use the default values:
     * - For scale up: 0 (no stabilization)
     * - For scale down: 300 (5 minutes)
     */
    private Integer stabilizationWindowSeconds;

    /**
     * SelectPolicy is used to specify which policy should be used.
     * If not set, the default value Max is used for scale up and Min for scale down.
     * Valid values: "Max" (use the policy with highest change), "Min" (lowest change), "Disabled"
     */
    private String selectPolicy;

    /**
     * Policies is a list of potential scaling policies which can be used during scaling.
     * At least one policy must be specified, otherwise the HPAScalingRules will be discarded as invalid.
     */
    @Singular("policy")
    private List<HPAScalingPolicy> policies;
}
