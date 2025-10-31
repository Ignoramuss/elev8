package io.elev8.resources.verticalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

/**
 * VPAContainerResourcePolicy controls how the autoscaler computes recommended resources for a specific container.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VPAContainerResourcePolicy {
    /**
     * ContainerName is the name of the container or "*" to apply to all containers.
     * This field is required.
     */
    private String containerName;

    /**
     * Mode controls whether autoscaling is enabled for the container.
     * Valid values: "Auto", "Off"
     * Default: "Auto"
     */
    private String mode;

    /**
     * MinAllowed specifies the minimal amount of resources that will be recommended for the container.
     * The default is no minimum.
     * Example: {"cpu": "100m", "memory": "128Mi"}
     */
    private Map<String, String> minAllowed;

    /**
     * MaxAllowed specifies the maximum amount of resources that will be recommended for the container.
     * The default is no maximum.
     * Example: {"cpu": "2", "memory": "2Gi"}
     */
    private Map<String, String> maxAllowed;

    /**
     * ControlledResources specifies which resource values should be controlled.
     * The default is ["cpu", "memory"].
     * Valid values: "cpu", "memory"
     */
    @Singular("controlledResource")
    private List<String> controlledResources;

    /**
     * ControlledValues controls which resource value should be autoscaled.
     * Valid values:
     * - "RequestsAndLimits": Both requests and limits are autoscaled (mirrored, limit = request)
     * - "RequestsOnly": Only requests are autoscaled (limits are not changed)
     * Default: "RequestsAndLimits"
     */
    private String controlledValues;
}
