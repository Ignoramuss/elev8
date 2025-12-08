package io.elev8.resources.verticalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.Condition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * VPACondition describes the state of a VerticalPodAutoscaler at a certain point.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VPACondition implements Condition {
    /**
     * Type describes the current condition.
     * Valid values:
     * - "RecommendationProvided": VPA has valid recommendations
     * - "LowConfidence": VPA has low confidence in its recommendations
     * - "NoPodsMatched": VPA couldn't find any pods matching the target selector
     * - "FetchingHistory": VPA is fetching historical metrics
     */
    private String type;

    /**
     * Status is the status of the condition.
     * Valid values: "True", "False", "Unknown"
     */
    private String status;

    /**
     * LastTransitionTime is the last time the condition transitioned from one status to another.
     */
    private String lastTransitionTime;

    /**
     * Reason is a one-word CamelCase reason for the condition's last transition.
     */
    private String reason;

    /**
     * Message is a human-readable message indicating details about last transition.
     */
    private String message;
}
