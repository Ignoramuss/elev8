package io.elev8.resources.verticalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.horizontalpodautoscaler.CrossVersionObjectReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * VerticalPodAutoscalerSpec defines the desired behavior of the VerticalPodAutoscaler.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerticalPodAutoscalerSpec {
    /**
     * TargetRef points to the controller managing the set of pods for which
     * autoscaling should be enabled (Deployment, StatefulSet, DaemonSet, ReplicaSet, etc.).
     * This field is required.
     */
    private CrossVersionObjectReference targetRef;

    /**
     * UpdatePolicy describes the rules on how changes are applied to the pods.
     * If not specified, all fields in the UpdatePolicy are set to their default values.
     */
    private VPAUpdatePolicy updatePolicy;

    /**
     * ResourcePolicy controls how the autoscaler computes recommended resources.
     * The resource policy may be used to set constraints on the recommendations for
     * individual containers. If not specified, the autoscaler computes recommended
     * resources for all containers in the pod, without additional constraints.
     */
    private VPAResourcePolicy resourcePolicy;

    /**
     * Recommenders is the list of recommenders to use.
     * If not specified, the default recommender will be used.
     */
    @Singular("recommender")
    private List<VPARecommenderSelector> recommenders;
}
