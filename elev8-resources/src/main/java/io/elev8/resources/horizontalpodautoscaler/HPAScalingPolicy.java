package io.elev8.resources.horizontalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * HPAScalingPolicy is a single policy which must hold true for a specified past interval.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HPAScalingPolicy {
    /**
     * Type is used to specify the scaling policy.
     * Valid values: "Pods" (scale by absolute number), "Percent" (scale by percentage)
     */
    private String type;

    /**
     * Value contains the amount of change which is permitted by the policy.
     * It must be greater than zero.
     * - For type="Pods": absolute number of pods
     * - For type="Percent": percentage value
     */
    private Integer value;

    /**
     * PeriodSeconds specifies the window of time for which the policy should hold true.
     * Must be greater than zero and less than or equal to 1800 (30 min).
     */
    private Integer periodSeconds;
}
