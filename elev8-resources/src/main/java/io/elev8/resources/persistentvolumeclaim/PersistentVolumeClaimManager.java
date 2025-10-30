package io.elev8.resources.persistentvolumeclaim;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for PersistentVolumeClaim resources.
 * Provides CRUD operations for Kubernetes PersistentVolumeClaims.
 *
 * PersistentVolumeClaims are namespace-scoped resources that represent a user's request
 * for persistent storage. They can request specific size and access modes.
 */
public final class PersistentVolumeClaimManager extends AbstractResourceManager<PersistentVolumeClaim> {

    public PersistentVolumeClaimManager(final KubernetesClient client) {
        super(client, PersistentVolumeClaim.class, "/api/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "persistentvolumeclaims";
    }
}
