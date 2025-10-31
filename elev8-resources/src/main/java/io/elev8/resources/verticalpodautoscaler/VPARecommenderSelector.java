package io.elev8.resources.verticalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * VPARecommenderSelector points to a specific Vertical Pod Autoscaler recommender.
 * When multiple recommenders are available, this selector allows choosing which one to use.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VPARecommenderSelector {
    /**
     * Name of the recommender responsible for generating recommendation for this object.
     */
    private String name;
}
