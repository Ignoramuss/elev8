package io.elev8.resources.verticalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * VerticalPodAutoscalerStatus describes the runtime state of the autoscaler.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerticalPodAutoscalerStatus {
    /**
     * Conditions is the set of conditions required for this autoscaler to scale its target,
     * and indicates whether or not those conditions are met.
     */
    @Singular("condition")
    private List<VPACondition> conditions;

    /**
     * Recommendation holds the most recently computed amount of resources recommended by
     * the autoscaler for the controlled pods.
     */
    private VPARecommendation recommendation;
}
