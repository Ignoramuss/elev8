package io.elev8.resources.volumesnapshotcontent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * VolumeSnapshotContentStatus represents the status of a VolumeSnapshotContent object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VolumeSnapshotContentStatus {
    /**
     * SnapshotHandle is the CSI snapshot ID on the underlying storage system.
     * This is the unique identifier used by the CSI driver to identify the snapshot
     * on the storage backend.
     * This field must be set by the snapshot controller upon successful snapshot creation.
     */
    private String snapshotHandle;

    /**
     * CreationTime is the timestamp when the point-in-time snapshot was taken by the
     * underlying storage system.
     * Format: Unix time encoded as an int64 (nanoseconds since January 1, 1970 UTC).
     * If not specified, it indicates that the snapshot has not been created yet.
     * This field must be set by the snapshot controller upon successful snapshot creation.
     */
    private Long creationTime;

    /**
     * RestoreSize represents the complete size of the snapshot in bytes.
     * This is the minimum size of volume required to rehydrate from this snapshot.
     * This field must be set by the snapshot controller upon successful snapshot creation.
     */
    private Long restoreSize;

    /**
     * ReadyToUse indicates if a snapshot is ready to be used to restore a volume.
     * This field must be set by the snapshot controller.
     * If not specified, it means the readiness of a snapshot is unknown.
     */
    private Boolean readyToUse;

    /**
     * Error is the last observed error during snapshot creation, if any.
     * Upon success, this field will be empty.
     * Upon failure, this field will contain error details.
     */
    private VolumeSnapshotError error;
}
