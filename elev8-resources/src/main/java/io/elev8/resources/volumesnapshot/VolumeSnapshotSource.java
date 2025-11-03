package io.elev8.resources.volumesnapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * VolumeSnapshotSource specifies whether the underlying snapshot should be dynamically taken upon creation
 * or if a pre-existing VolumeSnapshotContent object should be used.
 * Exactly one of its members must be set.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VolumeSnapshotSource {
    /**
     * PersistentVolumeClaimName specifies the name of the PersistentVolumeClaim object
     * representing the volume from which a snapshot should be created.
     * This PVC must be in the same namespace as the VolumeSnapshot object.
     * This field should be set if the snapshot does not exist, and needs to be created.
     * This field is immutable.
     * Mutually exclusive with volumeSnapshotContentName.
     */
    private String persistentVolumeClaimName;

    /**
     * VolumeSnapshotContentName specifies the name of a pre-existing VolumeSnapshotContent
     * object representing an existing volume snapshot.
     * This field should be set if the snapshot already exists and only needs to be bound.
     * This field is immutable.
     * Mutually exclusive with persistentVolumeClaimName.
     */
    private String volumeSnapshotContentName;
}
