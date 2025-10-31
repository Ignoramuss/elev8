package io.elev8.resources.verticalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * VPAResourcePolicy controls how the autoscaler computes recommended resources.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VPAResourcePolicy {
    /**
     * ContainerPolicies contains the per-container resource policies.
     * If not specified, the autoscaler will compute recommended resources for all containers
     * without additional constraints.
     */
    @Singular("containerPolicy")
    private List<VPAContainerResourcePolicy> containerPolicies;
}
