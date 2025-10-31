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
 * VPARecommendation holds the most recently computed amount of resources recommended by
 * the autoscaler for the controlled pods.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VPARecommendation {
    /**
     * ContainerRecommendations are per-container recommendations.
     */
    @Singular("containerRecommendation")
    private List<VPAContainerRecommendation> containerRecommendations;
}
