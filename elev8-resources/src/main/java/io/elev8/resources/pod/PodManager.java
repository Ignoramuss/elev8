package io.elev8.resources.pod;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

public final class PodManager extends AbstractResourceManager<Pod> {

    public PodManager(final KubernetesClient client) {
        super(client, Pod.class, "/api/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "pods";
    }
}
