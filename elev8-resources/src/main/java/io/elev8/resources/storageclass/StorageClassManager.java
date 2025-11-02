package io.elev8.resources.storageclass;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractClusterResourceManager;

/**
 * Manager for StorageClass resources.
 * Provides CRUD operations for Kubernetes StorageClasses in the storage.k8s.io/v1 API group.
 *
 * StorageClass provides a way to describe different classes of storage that can be dynamically
 * provisioned. It defines parameters for the volume plugin (provisioner), reclaim policy,
 * volume binding mode, and topology constraints for storage provisioning.
 */
public final class StorageClassManager extends AbstractClusterResourceManager<StorageClass> {

    public StorageClassManager(final KubernetesClient client) {
        super(client, StorageClass.class, "/apis/storage.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "storageclasses";
    }
}
