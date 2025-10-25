package io.elev8.resources.service;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

public final class ServiceManager extends AbstractResourceManager<Service> {

    public ServiceManager(final KubernetesClient client) {
        super(client, Service.class, "/api/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "services";
    }
}
