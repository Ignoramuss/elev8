package io.elev8.resources.volumesnapshotclass;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractClusterResourceManager;

/**
 * Manager for VolumeSnapshotClass resources.
 * Provides CRUD operations for Kubernetes VolumeSnapshotClasses in the snapshot.storage.k8s.io/v1 API group.
 *
 * VolumeSnapshotClass specifies parameters that a snapshot provisioner uses when creating volume snapshots.
 * It is a cluster-scoped resource used to define different classes of snapshot storage.
 */
public final class VolumeSnapshotClassManager extends AbstractClusterResourceManager<VolumeSnapshotClass> {

    public VolumeSnapshotClassManager(final KubernetesClient client) {
        super(client, VolumeSnapshotClass.class, "/apis/snapshot.storage.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "volumesnapshotclasses";
    }
}
