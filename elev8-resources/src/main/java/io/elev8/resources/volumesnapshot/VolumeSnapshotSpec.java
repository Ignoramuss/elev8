package io.elev8.resources.volumesnapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * VolumeSnapshotSpec describes the desired state of a VolumeSnapshot.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VolumeSnapshotSpec {
    /**
     * Source specifies where a snapshot will be created from.
     * This field is immutable after creation.
     * Exactly one of its members must be set.
     * This is a required field.
     */
    private VolumeSnapshotSource source;

    /**
     * VolumeSnapshotClassName is the name of the VolumeSnapshotClass requested by the VolumeSnapshot.
     * VolumeSnapshotClassName may be left empty to indicate that the default class should be used.
     * This field is immutable.
     */
    private String volumeSnapshotClassName;
}
