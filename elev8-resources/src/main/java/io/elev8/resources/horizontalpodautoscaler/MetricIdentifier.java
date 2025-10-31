package io.elev8.resources.horizontalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.LabelSelector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * MetricIdentifier defines the name and optionally selector for a metric.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricIdentifier {
    /**
     * Name is the name of the given metric.
     */
    private String name;

    /**
     * Selector is the string-encoded form of a standard Kubernetes label selector for the given metric.
     * When set, it is passed as an additional parameter to the metrics server for more specific metrics scoping.
     * When unset, just the metricName will be used to gather metrics.
     */
    private LabelSelector selector;
}
