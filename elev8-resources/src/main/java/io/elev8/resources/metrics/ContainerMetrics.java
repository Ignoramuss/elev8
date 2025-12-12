package io.elev8.resources.metrics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents per-container resource usage metrics from the Kubernetes Metrics API.
 *
 * <p>Each container in a pod has its own metrics entry with the container name
 * and its current resource usage.</p>
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContainerMetrics {

    private final String name;
    private final ResourceUsage usage;
}
