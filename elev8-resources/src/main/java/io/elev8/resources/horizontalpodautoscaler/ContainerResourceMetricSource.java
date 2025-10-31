package io.elev8.resources.horizontalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * ContainerResourceMetricSource indicates how to scale on a resource metric known to Kubernetes,
 * as specified in requests and limits, describing each pod in the current scale target
 * (e.g., CPU or memory). The values will be averaged together before being compared to the target.
 * Such metrics are built in to Kubernetes, and have special scaling options on top of those available
 * to normal per-pod metrics using the "pods" source. Only one "target" type should be set.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContainerResourceMetricSource {
    /**
     * Name of the resource in question.
     * Valid values: "cpu", "memory"
     */
    private String name;

    /**
     * Container is the name of the container in the pods of the scaling target.
     */
    private String container;

    /**
     * Target specifies the target value for the given metric.
     */
    private MetricTarget target;
}
