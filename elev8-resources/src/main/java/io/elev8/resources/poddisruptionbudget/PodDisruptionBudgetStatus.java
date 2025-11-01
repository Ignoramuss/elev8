package io.elev8.resources.poddisruptionbudget;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

/**
 * PodDisruptionBudgetStatus represents information about the status of a PodDisruptionBudget.
 * Status may trail the actual state of a system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PodDisruptionBudgetStatus {
    /**
     * CurrentHealthy is the current number of healthy pods.
     */
    private Integer currentHealthy;

    /**
     * DesiredHealthy is the minimum desired number of healthy pods.
     */
    private Integer desiredHealthy;

    /**
     * DisruptionsAllowed is the number of pod disruptions that are currently allowed.
     */
    private Integer disruptionsAllowed;

    /**
     * ExpectedPods is the total number of pods counted by this disruption budget.
     */
    private Integer expectedPods;

    /**
     * ObservedGeneration is the most recent generation observed when updating this PDB status.
     * DisruptionsAllowed and other status information is valid only if observedGeneration equals to PDB's object generation.
     */
    private Long observedGeneration;

    /**
     * Conditions contain conditions for PDB. The disruption controller sets the DisruptionAllowed condition.
     * The following are known values for the reason field (additional reasons could be added in the future):
     * - SyncFailed: The controller encountered an error and wasn't able to compute the number of allowed disruptions
     * - InsufficientPods: The number of pods are either at or below the number required by the PodDisruptionBudget
     * - SufficientPods: There are more pods than required by the PodDisruptionBudget
     */
    @Singular("condition")
    private List<PodDisruptionBudgetCondition> conditions;

    /**
     * DisruptedPods contains information about pods whose eviction was processed by the API server eviction subresource handler
     * but has not yet been observed by the PodDisruptionBudget controller.
     * A pod will be in this map from the time when the API server processed the eviction request to the time when the pod is seen by PDB controller as having been marked for deletion (or after a timeout).
     * The key in the map is the name of the pod and the value is the time when the API server processed the eviction request.
     */
    @Singular("disruptedPodEntry")
    private Map<String, String> disruptedPods;
}
