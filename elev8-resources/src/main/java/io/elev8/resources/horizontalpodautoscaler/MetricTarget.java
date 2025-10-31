package io.elev8.resources.horizontalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * MetricTarget defines the target value, average value, or average utilization of a metric.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricTarget {
    /**
     * Type represents whether the metric type is Utilization, Value, or AverageValue.
     * Valid values: "Utilization", "Value", "AverageValue"
     */
    private String type;

    /**
     * Value is the target value of the metric (as a quantity string).
     * Mutually exclusive with AverageValue and AverageUtilization.
     * Example: "100m" for CPU, "128Mi" for memory
     */
    private String value;

    /**
     * AverageValue is the target value of the average of the metric across all relevant pods
     * (as a quantity string).
     * Mutually exclusive with Value and AverageUtilization.
     * Example: "50m" for CPU, "64Mi" for memory
     */
    private String averageValue;

    /**
     * AverageUtilization is the target value of the average of the resource metric
     * across all relevant pods, represented as a percentage of the requested value of the
     * resource for the pods.
     * Mutually exclusive with Value and AverageValue.
     * Valid for "Resource" and "ContainerResource" metric source types.
     * Example: 50 (represents 50%)
     */
    private Integer averageUtilization;
}
