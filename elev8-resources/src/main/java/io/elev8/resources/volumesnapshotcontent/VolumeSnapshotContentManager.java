package io.elev8.resources.volumesnapshotcontent;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractClusterResourceManager;

/**
 * Manager for VolumeSnapshotContent resources.
 * Provides CRUD operations for Kubernetes VolumeSnapshotContents in the snapshot.storage.k8s.io/v1 API group.
 *
 * VolumeSnapshotContent represents the actual snapshot content in the underlying storage system.
 * It is a cluster-scoped resource (not namespaced) that has a bidirectional binding with
 * VolumeSnapshot objects. VolumeSnapshotContents are usually created by the snapshot controller
 * during dynamic provisioning or can be pre-provisioned by cluster administrators.
 */
public final class VolumeSnapshotContentManager extends AbstractClusterResourceManager<VolumeSnapshotContent> {

    public VolumeSnapshotContentManager(final KubernetesClient client) {
        super(client, VolumeSnapshotContent.class, "/apis/snapshot.storage.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "volumesnapshotcontents";
    }
}
