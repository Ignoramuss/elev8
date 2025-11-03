package io.elev8.resources.csidriver;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * CSIDriverSpec describes how the CSI driver should be configured and behave.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CSIDriverSpec {
    /**
     * Indicates whether this CSI volume driver requires an attach operation.
     * Set to true if the driver implements the CSI ControllerPublishVolume() method.
     * Default: true if not specified.
     */
    private Boolean attachRequired;

    /**
     * Indicates whether this CSI volume driver requires additional pod information
     * (like podName, podUID, etc.) during mount operations.
     * If true, pod information is passed as volume context in NodePublishVolume calls.
     * Default: false.
     */
    private Boolean podInfoOnMount;

    /**
     * Defines if the underlying volume supports changing ownership and permission
     * of the volume before being mounted.
     * Valid values:
     * - "None": No modifications to volume ownership/permissions
     * - "File": Driver supports volume ownership/permission changes
     * - "ReadWriteOnceWithFSType": Default - supports changes only for RWO volumes with fsType
     * Default: "ReadWriteOnceWithFSType"
     */
    private String fsGroupPolicy;

    /**
     * Defines the supported volume lifecycle modes for this CSI driver.
     * Valid values:
     * - "Persistent": Standard PVC/PV mechanism
     * - "Ephemeral": Inline ephemeral volumes (CSI inline volumes)
     * This field is immutable after creation.
     * Default: ["Persistent"]
     */
    @Singular("volumeLifecycleMode")
    private List<String> volumeLifecycleModes;

    /**
     * Enables capacity-aware pod scheduling when set to true.
     * The scheduler will use CSIStorageCapacity objects created by the driver
     * to make better pod placement decisions.
     * Default: false
     */
    private Boolean storageCapacity;

    /**
     * Service account token specifications that the CSI driver needs for authentication.
     * Kubelet will pass the requested tokens in VolumeContext during NodePublishVolume calls.
     */
    @Singular("tokenRequest")
    private List<TokenRequest> tokenRequests;

    /**
     * Indicates the CSI driver wants periodic NodePublishVolume calls to reflect
     * any changes in volume attributes.
     * Useful for volumes with short-lived tokens or dynamically changing properties.
     * Default: false
     */
    private Boolean requiresRepublish;

    /**
     * Indicates the CSI driver supports "-o context" mount option for SELinux.
     * When true, each volume gets an independent SELinux label.
     * Alpha feature (Kubernetes 1.25+).
     * Default: false
     */
    private Boolean seLinuxMount;

    /**
     * Interval in seconds for updating the allocatable capacity reported in CSINode.
     * Must be at least 10 seconds if specified.
     * Optional field.
     */
    private Long nodeAllocatableUpdatePeriodSeconds;
}
