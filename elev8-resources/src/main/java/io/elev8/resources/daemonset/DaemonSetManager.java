package io.elev8.resources.daemonset;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

public final class DaemonSetManager extends AbstractResourceManager<DaemonSet> {

    public DaemonSetManager(final KubernetesClient client) {
        super(client, DaemonSet.class, "/apis/apps/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "daemonsets";
    }
}
