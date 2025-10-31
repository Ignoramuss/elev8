package io.elev8.resources.horizontalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * MetricSpec specifies how to scale based on a single metric.
 * Only one of the metric source fields should be set, matching the type field.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricSpec {
    /**
     * Type is the type of metric source.
     * Valid values: "Resource", "Pods", "Object", "External", "ContainerResource"
     */
    private String type;

    /**
     * Resource refers to a resource metric (like CPU or memory) known to Kubernetes.
     * Set when type="Resource".
     */
    private ResourceMetricSource resource;

    /**
     * Pods refers to a metric describing each pod in the current scale target
     * (for example, transactions-processed-per-second).
     * Set when type="Pods".
     */
    private PodsMetricSource pods;

    /**
     * Object refers to a metric describing a single Kubernetes object
     * (for example, hits-per-second on an Ingress object).
     * Set when type="Object".
     */
    private ObjectMetricSource object;

    /**
     * External refers to a global metric that is not associated with any Kubernetes object.
     * Set when type="External".
     */
    private ExternalMetricSource external;

    /**
     * ContainerResource refers to a resource metric (like CPU or memory) known to Kubernetes
     * for a specific container in each pod in the current scale target.
     * Set when type="ContainerResource".
     */
    private ContainerResourceMetricSource containerResource;
}
