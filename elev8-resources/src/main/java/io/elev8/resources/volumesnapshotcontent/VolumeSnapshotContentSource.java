package io.elev8.resources.volumesnapshotcontent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * VolumeSnapshotContentSource specifies whether the snapshot is (or should be)
 * dynamically provisioned or already exists.
 * Exactly one of its members must be set.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VolumeSnapshotContentSource {
    /**
     * VolumeHandle specifies the CSI volume ID of the volume from which a snapshot
     * should be dynamically taken from.
     * This field is immutable after creation.
     * Mutually exclusive with snapshotHandle.
     */
    private String volumeHandle;

    /**
     * SnapshotHandle specifies the CSI snapshot ID of a pre-existing snapshot on the
     * underlying storage system for which a Kubernetes object representation was created.
     * This field is immutable after creation.
     * Mutually exclusive with volumeHandle.
     */
    private String snapshotHandle;
}
