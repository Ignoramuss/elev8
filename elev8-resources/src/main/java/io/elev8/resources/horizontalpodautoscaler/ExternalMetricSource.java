package io.elev8.resources.horizontalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * ExternalMetricSource indicates how to scale on a metric not associated with any Kubernetes object
 * (for example, length of queue in cloud messaging service, or QPS from loadbalancer running outside of cluster).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalMetricSource {
    /**
     * Metric identifies the target metric by name and selector.
     */
    private MetricIdentifier metric;

    /**
     * Target specifies the target value for the given metric.
     */
    private MetricTarget target;
}
