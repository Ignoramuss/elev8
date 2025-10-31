package io.elev8.resources.horizontalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * PodsMetricSource indicates how to scale on a metric describing each pod in the current scale target
 * (for example, transactions-processed-per-second). The values will be averaged together before being
 * compared to the target value.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PodsMetricSource {
    /**
     * Metric identifies the target metric by name and selector.
     */
    private MetricIdentifier metric;

    /**
     * Target specifies the target value for the given metric.
     */
    private MetricTarget target;
}
