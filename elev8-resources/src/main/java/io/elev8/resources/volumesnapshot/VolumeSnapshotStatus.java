package io.elev8.resources.volumesnapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * VolumeSnapshotStatus represents the current information about a snapshot.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VolumeSnapshotStatus {
    /**
     * BoundVolumeSnapshotContentName is the name of the VolumeSnapshotContent object
     * to which this VolumeSnapshot object intends to bind to.
     * If not specified, it indicates that the VolumeSnapshot object has not been
     * successfully bound to a VolumeSnapshotContent object yet.
     */
    private String boundVolumeSnapshotContentName;

    /**
     * CreationTime is the timestamp when the point-in-time snapshot was taken by
     * the underlying storage system.
     * If not specified, it indicates that the snapshot has not been created yet.
     * Format: RFC3339 date-time
     */
    private String creationTime;

    /**
     * ReadyToUse indicates if the snapshot is ready to be used to restore a volume.
     * In dynamic snapshot creation case, this field will be filled in by the snapshot
     * controller with the "ready_to_use" value returned from CSI "CreateSnapshot" gRPC call.
     * For a pre-existing snapshot, this field will be filled with the "ready_to_use" value
     * returned from CSI "ListSnapshots" gRPC call if the driver supports it.
     * If not specified, it means the readiness of a snapshot is unknown.
     */
    private Boolean readyToUse;

    /**
     * RestoreSize represents the minimum size of volume required to create a volume
     * from this snapshot.
     * In dynamic snapshot creation case, this field will be filled in by the snapshot
     * controller with the "size_bytes" value returned from CSI "CreateSnapshot" gRPC call.
     * For a pre-existing snapshot, this field will be filled with the "size_bytes" value
     * returned from CSI "ListSnapshots" gRPC call if the driver supports it.
     * Format: Kubernetes quantity (e.g., "10Gi", "5Ti")
     */
    private String restoreSize;

    /**
     * Error is the last observed error during snapshot creation, if any.
     * Upon success, this field will be empty.
     * Upon failure, this field will contain the error details.
     */
    private VolumeSnapshotError error;

    /**
     * VolumeGroupSnapshotName is the name of the VolumeGroupSnapshot of which this
     * VolumeSnapshot is a part of.
     * If not specified, this VolumeSnapshot is not part of any group snapshot.
     */
    private String volumeGroupSnapshotName;
}
