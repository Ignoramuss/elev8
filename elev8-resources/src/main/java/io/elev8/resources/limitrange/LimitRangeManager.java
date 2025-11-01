package io.elev8.resources.limitrange;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for LimitRange resources.
 * Provides CRUD operations for Kubernetes LimitRanges in the v1 Core API.
 *
 * LimitRange sets resource usage limits for each kind of resource in a Namespace.
 * It can set default request/limit for compute resources, enforce min/max usage,
 * and enforce the ratio between request and limit for a resource in a namespace.
 */
public final class LimitRangeManager extends AbstractResourceManager<LimitRange> {

    public LimitRangeManager(final KubernetesClient client) {
        super(client, LimitRange.class, "/api/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "limitranges";
    }
}
