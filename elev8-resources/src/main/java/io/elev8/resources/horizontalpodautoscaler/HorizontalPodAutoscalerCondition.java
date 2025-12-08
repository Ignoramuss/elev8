package io.elev8.resources.horizontalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.Condition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * HorizontalPodAutoscalerCondition describes the state of a HorizontalPodAutoscaler at a certain point.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HorizontalPodAutoscalerCondition implements Condition {
    /**
     * Type describes the current condition.
     * Valid types: "ScalingActive", "AbleToScale", "ScalingLimited"
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
     * Reason is the reason for the condition's last transition.
     */
    private String reason;

    /**
     * Message is a human-readable explanation containing details about the transition.
     */
    private String message;
}
