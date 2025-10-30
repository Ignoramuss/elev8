package io.elev8.resources.persistentvolume;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.LocalObjectReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * CSIPersistentVolumeSource represents storage that is handled by an external CSI driver.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CSIPersistentVolumeSource {
    /**
     * Driver is the name of the driver to use for this volume.
     */
    @NonNull
    private String driver;

    /**
     * VolumeHandle is the unique volume name returned by the CSI volume plugin's CreateVolume.
     */
    @NonNull
    private String volumeHandle;

    /**
     * ReadOnly value to force the volume to be mounted with read-only permissions.
     * Defaults to false.
     */
    private Boolean readOnly;

    /**
     * FSType to mount. Must be a filesystem type supported by the host operating system.
     * Ex. "ext4", "xfs", "ntfs".
     */
    private String fsType;

    /**
     * VolumeAttributes of the volume to publish.
     */
    @Singular("volumeAttribute")
    private Map<String, String> volumeAttributes;

    /**
     * ControllerPublishSecretRef is a reference to the secret object containing
     * sensitive information to pass to the CSI driver to complete the CSI ControllerPublishVolume call.
     */
    private LocalObjectReference controllerPublishSecretRef;

    /**
     * NodeStageSecretRef is a reference to the secret object containing sensitive
     * information to pass to the CSI driver to complete the CSI NodeStageVolume call.
     */
    private LocalObjectReference nodeStageSecretRef;

    /**
     * NodePublishSecretRef is a reference to the secret object containing sensitive
     * information to pass to the CSI driver to complete the CSI NodePublishVolume call.
     */
    private LocalObjectReference nodePublishSecretRef;

    /**
     * ControllerExpandSecretRef is a reference to the secret object containing
     * sensitive information to pass to the CSI driver to complete the CSI ControllerExpandVolume call.
     */
    private LocalObjectReference controllerExpandSecretRef;
}
