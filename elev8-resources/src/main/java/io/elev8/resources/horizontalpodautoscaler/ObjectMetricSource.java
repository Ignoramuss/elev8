package io.elev8.resources.horizontalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * ObjectMetricSource indicates how to scale on a metric describing a Kubernetes object
 * (for example, hits-per-second on an Ingress object).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectMetricSource {
    /**
     * DescribedObject specifies the descriptions of a object, such as kind, name apiVersion.
     */
    private CrossVersionObjectReference describedObject;

    /**
     * Metric identifies the target metric by name and selector.
     */
    private MetricIdentifier metric;

    /**
     * Target specifies the target value for the given metric.
     */
    private MetricTarget target;
}
