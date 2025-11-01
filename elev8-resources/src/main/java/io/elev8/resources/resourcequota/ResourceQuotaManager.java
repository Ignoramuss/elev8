package io.elev8.resources.resourcequota;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for ResourceQuota resources.
 * Provides CRUD operations for Kubernetes ResourceQuotas in the v1 API.
 *
 * ResourceQuota provides constraints that limit aggregate resource consumption per namespace.
 * It can limit the quantity of objects that can be created in a namespace by type,
 * as well as the total amount of compute resources that may be consumed by resources in that namespace.
 */
public final class ResourceQuotaManager extends AbstractResourceManager<ResourceQuota> {

    public ResourceQuotaManager(final KubernetesClient client) {
        super(client, ResourceQuota.class, "/api/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "resourcequotas";
    }
}
