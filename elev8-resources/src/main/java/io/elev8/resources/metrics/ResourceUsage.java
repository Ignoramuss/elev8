package io.elev8.resources.metrics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents resource usage metrics (CPU and memory) from the Kubernetes Metrics API.
 *
 * <p>CPU values are expressed in Kubernetes CPU units (e.g., "100m" for 100 millicores,
 * "2" for 2 cores). Memory values are expressed in bytes with optional suffixes
 * (e.g., "256Mi", "1Gi", "1000000000").</p>
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceUsage {

    private final String cpu;
    private final String memory;
}
