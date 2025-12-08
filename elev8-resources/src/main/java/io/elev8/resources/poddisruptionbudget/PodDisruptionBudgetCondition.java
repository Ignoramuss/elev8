package io.elev8.resources.poddisruptionbudget;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.Condition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * PodDisruptionBudgetCondition contains details for the current condition of this PodDisruptionBudget.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PodDisruptionBudgetCondition implements Condition {
    /**
     * Type describes the current condition.
     * Common type: "DisruptionAllowed"
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

    /**
     * ObservedGeneration represents the generation that the condition was set based upon.
     */
    private Long observedGeneration;
}
