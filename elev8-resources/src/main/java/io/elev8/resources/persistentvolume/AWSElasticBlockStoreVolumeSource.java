package io.elev8.resources.persistentvolume;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

/**
 * AWSElasticBlockStoreVolumeSource represents an AWS Disk resource that is attached to a
 * kubelet's host machine and then exposed to the pod.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AWSElasticBlockStoreVolumeSource {
    /**
     * VolumeID is unique ID of the persistent disk resource in AWS (e.g., aws://us-east-1a/vol-123456789).
     */
    @NonNull
    private String volumeID;

    /**
     * Filesystem type of the volume that you want to mount.
     * Tip: Ensure that the filesystem type is supported by the host operating system.
     * Examples: "ext4", "xfs", "ntfs". Defaults to "ext4".
     */
    private String fsType;

    /**
     * Partition is the partition in the volume that you want to mount.
     * If omitted, the default is to mount by volume name.
     */
    private Integer partition;

    /**
     * ReadOnly value to force the volume to be mounted with read-only permissions.
     * Defaults to false.
     */
    private Boolean readOnly;
}
