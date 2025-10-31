package io.elev8.resources.verticalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * VPAContainerRecommendation contains recommendations for a single container.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VPAContainerRecommendation {
    /**
     * ContainerName is the name of the container.
     */
    private String containerName;

    /**
     * LowerBound represents the minimum recommended amount of resources.
     * Running the application with less resources is likely to have significant impact on performance/availability.
     * Example: {"cpu": "50m", "memory": "64Mi"}
     */
    private Map<String, String> lowerBound;

    /**
     * Target represents the recommended amount of resources.
     * Pods will be updated to use this amount of resources.
     * Example: {"cpu": "100m", "memory": "128Mi"}
     */
    private Map<String, String> target;

    /**
     * UncappedTarget represents the recommendation calculated without considering
     * the minAllowed and maxAllowed restrictions from the container policy.
     * This can be used to see what VPA would recommend if there were no min/max constraints.
     * Example: {"cpu": "200m", "memory": "256Mi"}
     */
    private Map<String, String> uncappedTarget;

    /**
     * UpperBound represents the maximum recommended amount of resources.
     * Any resources allocated beyond this value are likely wasted.
     * Example: {"cpu": "500m", "memory": "512Mi"}
     */
    private Map<String, String> upperBound;
}
