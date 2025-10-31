package io.elev8.resources.horizontalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * HorizontalPodAutoscalerBehavior configures the scaling behavior of the target in both Up and Down directions
 * (scaleUp and scaleDown fields respectively).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HorizontalPodAutoscalerBehavior {
    /**
     * ScaleUp is the scaling policy for scaling Up.
     * If not set, the default value is the higher of:
     * - increase no more than 4 pods per 60 seconds
     * - double the number of pods per 60 seconds
     * No stabilization is used for scale up.
     */
    private HPAScalingRules scaleUp;

    /**
     * ScaleDown is the scaling policy for scaling Down.
     * If not set, the default value is to allow to scale down to minReplicas pods,
     * with a 300 second stabilization window (i.e., the highest recommendation for the last 300sec is used).
     */
    private HPAScalingRules scaleDown;
}
