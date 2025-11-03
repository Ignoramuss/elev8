package io.elev8.resources.csidriver;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractClusterResourceManager;

/**
 * Manager for CSIDriver resources.
 * Provides CRUD operations for Kubernetes CSIDrivers in the storage.k8s.io/v1 API group.
 *
 * CSIDriver captures information about a Container Storage Interface (CSI) volume driver
 * deployed on the cluster. It allows customization of how Kubernetes interacts with the driver,
 * including attach requirements, pod info on mount, fsGroup policies, and token requests.
 */
public final class CSIDriverManager extends AbstractClusterResourceManager<CSIDriver> {

    public CSIDriverManager(final KubernetesClient client) {
        super(client, CSIDriver.class, "/apis/storage.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "csidrivers";
    }
}
