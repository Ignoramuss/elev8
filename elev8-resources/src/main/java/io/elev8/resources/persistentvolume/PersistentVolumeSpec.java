package io.elev8.resources.persistentvolume;

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
 * PersistentVolumeSpec defines the desired characteristics of a volume.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersistentVolumeSpec {
    /**
     * Capacity describes the resources that the volume should have.
     * The "storage" key specifies the size (e.g., "10Gi", "100Mi").
     */
    @Singular("capacity")
    private Map<String, String> capacity;

    /**
     * AccessModes contains the desired access modes the volume should have.
     * Possible values: ReadWriteOnce, ReadOnlyMany, ReadWriteMany, ReadWriteOncePod.
     */
    @Singular("accessMode")
    private List<String> accessModes;

    /**
     * PersistentVolumeReclaimPolicy defines what happens to a persistent volume
     * when released from its claim. Valid options are Retain (default), Delete, Recycle.
     */
    private String persistentVolumeReclaimPolicy;

    /**
     * StorageClassName is the name of StorageClass to which this persistent volume belongs.
     * Empty value means that this volume does not belong to any StorageClass.
     */
    private String storageClassName;

    /**
     * VolumeMode defines if a volume is intended to be used with a formatted filesystem
     * or to remain in raw block state. Value of Filesystem is implied when not included.
     * Possible values: Filesystem, Block.
     */
    private String volumeMode;

    /**
     * MountOptions is the list of mount options, e.g. ["ro", "soft"].
     * Not validated - mount will simply fail if one is invalid.
     */
    @Singular("mountOption")
    private List<String> mountOptions;

    /**
     * HostPath represents a directory on the host.
     * Provisioned by a developer or tester. This is useful for single-node development and testing only!
     */
    private HostPathVolumeSource hostPath;

    /**
     * NFS represents an NFS mount on the host that shares a pod's lifetime.
     */
    private NFSVolumeSource nfs;

    /**
     * AWSElasticBlockStore represents an AWS Disk resource that is attached to a
     * kubelet's host machine and then exposed to the pod.
     */
    private AWSElasticBlockStoreVolumeSource awsElasticBlockStore;

    /**
     * AzureDisk represents an Azure Data Disk mount on the host and bind mount to the pod.
     */
    private AzureDiskVolumeSource azureDisk;

    /**
     * GCEPersistentDisk represents a GCE Disk resource that is attached to a
     * kubelet's host machine and then exposed to the pod.
     */
    private GCEPersistentDiskVolumeSource gcePersistentDisk;

    /**
     * CSI (Container Storage Interface) represents storage that is handled by an external CSI driver.
     */
    private CSIPersistentVolumeSource csi;
}
