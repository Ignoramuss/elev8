package io.elev8.resources.persistentvolume;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractClusterResourceManager;

/**
 * Manager for PersistentVolume resources.
 * Provides CRUD operations for Kubernetes PersistentVolumes.
 *
 * PersistentVolumes are cluster-scoped resources that represent a piece of storage in the cluster.
 * They have a lifecycle independent of any individual Pod that uses the PV. Unlike most other
 * resources, PersistentVolumes do not belong to a namespace.
 */
public final class PersistentVolumeManager extends AbstractClusterResourceManager<PersistentVolume> {

    public PersistentVolumeManager(final KubernetesClient client) {
        super(client, PersistentVolume.class, "/api/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "persistentvolumes";
    }
}
