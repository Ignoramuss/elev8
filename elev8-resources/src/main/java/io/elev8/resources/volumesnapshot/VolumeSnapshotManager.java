package io.elev8.resources.volumesnapshot;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for VolumeSnapshot resources.
 * Provides CRUD operations for Kubernetes VolumeSnapshots in the snapshot.storage.k8s.io/v1 API group.
 *
 * VolumeSnapshot represents a user's request for either creating a point-in-time snapshot of a
 * persistent volume, or binding to a pre-existing snapshot. VolumeSnapshots are namespace-scoped.
 */
public final class VolumeSnapshotManager extends AbstractResourceManager<VolumeSnapshot> {

    public VolumeSnapshotManager(final KubernetesClient client) {
        super(client, VolumeSnapshot.class, "/apis/snapshot.storage.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "volumesnapshots";
    }
}
