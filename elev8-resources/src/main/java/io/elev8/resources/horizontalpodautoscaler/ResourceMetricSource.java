package io.elev8.resources.horizontalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * ResourceMetricSource indicates how to scale on a resource metric known to Kubernetes,
 * such as CPU or memory.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceMetricSource {
    /**
     * Name of the resource in question.
     * Valid values: "cpu", "memory"
     */
    private String name;

    /**
     * Target specifies the target value for the given metric.
     */
    private MetricTarget target;
}
