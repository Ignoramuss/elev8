package io.elev8.resources.verticalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * VPAUpdatePolicy describes the rules on how changes are applied to the pods.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VPAUpdatePolicy {
    /**
     * UpdateMode controls when autoscaler applies changes to the pod resources.
     * Valid values:
     * - "Off": VPA only provides recommendations, doesn't update pods
     * - "Initial": VPA only assigns resources on pod creation, never changes them later
     * - "Recreate": VPA assigns resources on pod creation and updates them by evicting and recreating pods
     * - "Auto": Currently the same as "Recreate", may change in the future
     * Default: "Auto"
     */
    private String updateMode;

    /**
     * MinReplicas is the minimum number of replicas which need to be alive for Updater to
     * attempt pod eviction (only when updateMode is "Recreate" or "Auto").
     * Only positive values are allowed. Overrides global '--min-replicas' flag.
     */
    private Integer minReplicas;
}
