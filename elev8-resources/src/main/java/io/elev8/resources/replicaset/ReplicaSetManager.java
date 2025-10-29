package io.elev8.resources.replicaset;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for ReplicaSet resources.
 * Provides CRUD operations for Kubernetes ReplicaSets.
 *
 * ReplicaSets ensure that a specified number of pod replicas are running at any given time.
 * While ReplicaSets can be used independently, they are often managed automatically by
 * higher-level controllers like Deployments.
 */
public final class ReplicaSetManager extends AbstractResourceManager<ReplicaSet> {

    public ReplicaSetManager(final KubernetesClient client) {
        super(client, ReplicaSet.class, "/apis/apps/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "replicasets";
    }
}
