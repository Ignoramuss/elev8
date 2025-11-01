package io.elev8.resources.poddisruptionbudget;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.LabelSelector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * PodDisruptionBudgetSpec is a description of a PodDisruptionBudget.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PodDisruptionBudgetSpec {
    /**
     * MinAvailable ensures at least this many pods remain available after eviction.
     * Can be an absolute number (e.g., 5) or a percentage (e.g., "50%").
     * Mutually exclusive with maxUnavailable.
     */
    private Object minAvailable;

    /**
     * MaxUnavailable permits eviction if no more than this many pods become unavailable after eviction.
     * Can be an absolute number (e.g., 1) or a percentage (e.g., "25%").
     * Mutually exclusive with minAvailable.
     */
    private Object maxUnavailable;

    /**
     * Selector identifies target pods via label queries.
     * An empty selector matches all pods in the namespace.
     * A null selector matches no pods.
     */
    private LabelSelector selector;

    /**
     * UnhealthyPodEvictionPolicy governs unhealthy pod eviction behavior.
     * Valid values:
     * - "IfHealthyBudget" (default): Running but unhealthy pods only evict if app isn't disrupted
     * - "AlwaysAllow": Running but unhealthy pods face eviction regardless of budget
     */
    private String unhealthyPodEvictionPolicy;
}
