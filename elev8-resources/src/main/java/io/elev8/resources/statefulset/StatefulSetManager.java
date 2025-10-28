package io.elev8.resources.statefulset;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

public final class StatefulSetManager extends AbstractResourceManager<StatefulSet> {

    public StatefulSetManager(final KubernetesClient client) {
        super(client, StatefulSet.class, "/apis/apps/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "statefulsets";
    }
}
