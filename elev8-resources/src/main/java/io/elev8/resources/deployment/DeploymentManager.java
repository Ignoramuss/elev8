package io.elev8.resources.deployment;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

public final class DeploymentManager extends AbstractResourceManager<Deployment> {

    public DeploymentManager(final KubernetesClient client) {
        super(client, Deployment.class, "/apis/apps/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "deployments";
    }
}
