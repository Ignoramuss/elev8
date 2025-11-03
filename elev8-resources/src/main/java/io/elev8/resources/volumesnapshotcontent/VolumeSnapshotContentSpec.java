package io.elev8.resources.volumesnapshotcontent;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.ObjectReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * VolumeSnapshotContentSpec describes the common attributes of a VolumeSnapshotContent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VolumeSnapshotContentSpec {
    /**
     * VolumeSnapshotRef specifies the VolumeSnapshot object to which this VolumeSnapshotContent
     * object is bound. VolumeSnapshot.spec.volumeSnapshotContentName must reference this
     * VolumeSnapshotContent's name for the bidirectional binding to be valid.
     * This field is immutable after creation.
     * This is a required field.
     */
    private ObjectReference volumeSnapshotRef;

    /**
     * DeletionPolicy determines whether this VolumeSnapshotContent and the physical snapshot on
     * the underlying storage system should be deleted when the bound VolumeSnapshot is deleted.
     * Valid values are:
     * - "Delete" means that the content will be deleted along with the VolumeSnapshot.
     * - "Retain" means that the content will not be deleted when the VolumeSnapshot is deleted.
     * This field is immutable after creation.
     * This is a required field.
     */
    private String deletionPolicy;

    /**
     * Driver is the name of the CSI driver used to create the physical snapshot on the
     * underlying storage system.
     * This field is immutable after creation.
     * This is a required field.
     */
    private String driver;

    /**
     * Source specifies whether the snapshot is (or should be) dynamically provisioned or already exists,
     * and just requires a Kubernetes object representation.
     * This field is immutable after creation.
     * Exactly one of its members (volumeHandle or snapshotHandle) must be set.
     * This is a required field.
     */
    private VolumeSnapshotContentSource source;

    /**
     * VolumeSnapshotClassName is the name of the VolumeSnapshotClass from which this snapshot
     * was (or will be) created.
     * This field is optional and immutable after creation.
     */
    private String volumeSnapshotClassName;

    /**
     * SourceVolumeMode is the mode of the volume whose snapshot is taken.
     * Valid values are "Filesystem" or "Block".
     * This field is optional and immutable after creation.
     * This is an alpha field and requires enabling VolumeSnapshotDataSource feature gate.
     */
    private String sourceVolumeMode;
}
