package io.elev8.resources.persistentvolume;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

/**
 * AzureDiskVolumeSource represents an Azure Data Disk mount on the host and bind mount to the pod.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AzureDiskVolumeSource {
    /**
     * DiskName is the name of the data disk in the blob storage.
     */
    @NonNull
    private String diskName;

    /**
     * DiskURI is the URI of data disk in the blob storage.
     */
    @NonNull
    private String diskURI;

    /**
     * CachingMode is the Host Caching mode: None, ReadOnly, ReadWrite.
     */
    private String cachingMode;

    /**
     * FSType is filesystem type to mount. Must be a filesystem type supported by the host
     * operating system. Ex. "ext4", "xfs", "ntfs". Defaults to "ext4".
     */
    private String fsType;

    /**
     * ReadOnly forces the ReadOnly setting in VolumeMounts. Defaults to false.
     */
    private Boolean readOnly;

    /**
     * Kind expected values are Shared, Dedicated, Managed. Defaults to Shared.
     */
    private String kind;
}
