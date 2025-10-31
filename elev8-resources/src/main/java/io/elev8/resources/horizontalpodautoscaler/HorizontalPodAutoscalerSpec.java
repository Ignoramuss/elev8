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
 * HorizontalPodAutoscalerSpec describes the desired functionality of the HorizontalPodAutoscaler.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HorizontalPodAutoscalerSpec {
    /**
     * ScaleTargetRef points to the target resource to scale (Deployment, ReplicaSet, StatefulSet, etc.).
     * This field is required.
     */
    private CrossVersionObjectReference scaleTargetRef;

    /**
     * MinReplicas is the lower limit for the number of replicas to which the autoscaler can scale down.
     * It defaults to 1 pod. minReplicas is allowed to be 0 if the alpha feature gate HPAScaleToZero
     * is enabled and at least one Object or External metric is configured.
     */
    private Integer minReplicas;

    /**
     * MaxReplicas is the upper limit for the number of replicas to which the autoscaler can scale up.
     * It cannot be less than minReplicas.
     * This field is required.
     */
    private Integer maxReplicas;

    /**
     * Metrics contains the specifications for which to use to calculate the desired replica count
     * (the maximum replica count across all metrics will be used).
     * If not set, the default metric will be set to 80% average CPU utilization.
     */
    @Singular("metric")
    private List<MetricSpec> metrics;

    /**
     * Behavior configures the scaling behavior of the target in both Up and Down directions
     * (scaleUp and scaleDown fields respectively).
     * If not set, the default HPAScalingRules for scale up and scale down are used.
     */
    private HorizontalPodAutoscalerBehavior behavior;
}
