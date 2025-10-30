package io.elev8.resources.persistentvolumeclaim;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.LabelSelector;
import io.elev8.resources.ResourceRequirements;
import io.elev8.resources.TypedLocalObjectReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * PersistentVolumeClaimSpec describes the common attributes of storage devices
 * and allows a Source for provider-specific attributes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersistentVolumeClaimSpec {
    /**
     * AccessModes contains the desired access modes the volume should have.
     * Possible values: ReadWriteOnce, ReadOnlyMany, ReadWriteMany, ReadWriteOncePod.
     */
    @Singular("accessMode")
    private List<String> accessModes;

    /**
     * Selector is a label query over volumes to consider for binding.
     */
    private LabelSelector selector;

    /**
     * Resources represents the minimum resources the volume should have.
     * If RecoverVolumeExpansionFailure feature is enabled users are allowed to specify resource requirements
     * that are lower than previous value but must still be higher than capacity recorded in the status field.
     */
    private ResourceRequirements resources;

    /**
     * VolumeName is the binding reference to the PersistentVolume backing this claim.
     */
    private String volumeName;

    /**
     * StorageClassName is the name of the StorageClass required by the claim.
     * More info: https://kubernetes.io/docs/concepts/storage/persistent-volumes#class-1
     */
    private String storageClassName;

    /**
     * VolumeMode defines what type of volume is required by the claim.
     * Value of Filesystem is implied when not included in claim spec.
     * Possible values: Filesystem, Block.
     */
    private String volumeMode;

    /**
     * DataSource field can be used to specify either:
     * * An existing VolumeSnapshot object (snapshot.storage.k8s.io/VolumeSnapshot)
     * * An existing PVC (PersistentVolumeClaim)
     * If the provisioner or an external controller can support the specified data source,
     * it will create a new volume based on the contents of the specified data source.
     */
    private TypedLocalObjectReference dataSource;

    /**
     * DataSourceRef specifies the object from which to populate the volume with data, if a non-empty
     * volume is desired. This may be any object from a non-empty API group (non
     * core object) or a PersistentVolumeClaim object.
     */
    private TypedLocalObjectReference dataSourceRef;
}
