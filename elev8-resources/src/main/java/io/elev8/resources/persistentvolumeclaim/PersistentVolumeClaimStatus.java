package io.elev8.resources.persistentvolumeclaim;

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
 * PersistentVolumeClaimStatus represents the current status of a persistent volume claim.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersistentVolumeClaimStatus {
    /**
     * Phase represents the current phase of PersistentVolumeClaim.
     * Possible values: Pending, Bound, Lost.
     */
    private String phase;

    /**
     * AccessModes contains the actual access modes the volume has.
     * More info: https://kubernetes.io/docs/concepts/storage/persistent-volumes#access-modes-1
     */
    @Singular("accessMode")
    private List<String> accessModes;

    /**
     * Capacity represents the actual resources of the underlying volume.
     */
    @Singular("capacity")
    private Map<String, String> capacity;

    /**
     * Conditions is the current Condition of persistent volume claim.
     * If underlying persistent volume is being resized then the Condition will be set to 'ResizeStarted'.
     */
    @Singular("condition")
    private List<PersistentVolumeClaimCondition> conditions;

    /**
     * AllocatedResources tracks the resources allocated to a PVC including its capacity.
     * Key names follow standard Kubernetes label syntax. Valid values are either:
     * * Un-prefixed keys:
     *   - storage - the capacity of the volume.
     * * Custom resources must use implementation-defined prefixed names such as "example.com/my-custom-resource"
     */
    @Singular("allocatedResource")
    private Map<String, String> allocatedResources;

    /**
     * PersistentVolumeClaimCondition contains details about state of pvc
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PersistentVolumeClaimCondition {
        /**
         * Type of PersistentVolumeClaim condition.
         * Possible values: Resizing, FileSystemResizePending.
         */
        private String type;

        /**
         * Status of the condition, one of True, False, Unknown.
         */
        private String status;

        /**
         * Last time we probed the condition.
         */
        private String lastProbeTime;

        /**
         * Last time the condition transitioned from one status to another.
         */
        private String lastTransitionTime;

        /**
         * Unique, one-word, CamelCase reason for the condition's last transition.
         */
        private String reason;

        /**
         * Human-readable message indicating details about last transition.
         */
        private String message;
    }
}
