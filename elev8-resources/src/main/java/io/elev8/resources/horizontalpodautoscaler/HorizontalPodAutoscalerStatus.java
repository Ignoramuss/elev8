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
 * HorizontalPodAutoscalerStatus describes the current status of a horizontal pod autoscaler.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HorizontalPodAutoscalerStatus {
    /**
     * ObservedGeneration is the most recent generation observed by this autoscaler.
     */
    private Long observedGeneration;

    /**
     * LastScaleTime is the last time the HorizontalPodAutoscaler scaled the number of pods.
     * Used by the autoscaler to control how often the number of pods is changed.
     */
    private String lastScaleTime;

    /**
     * CurrentReplicas is the current number of replicas of pods managed by this autoscaler,
     * as last seen by the autoscaler.
     */
    private Integer currentReplicas;

    /**
     * DesiredReplicas is the desired number of replicas of pods managed by this autoscaler,
     * as last calculated by the autoscaler.
     */
    private Integer desiredReplicas;

    /**
     * Conditions is the set of conditions required for this autoscaler to scale its target,
     * and indicates whether or not those conditions are met.
     */
    @Singular("condition")
    private List<HorizontalPodAutoscalerCondition> conditions;
}
