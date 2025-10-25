package io.elev8.resources.service;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

public class ServiceManager extends AbstractResourceManager<Service> {

    public ServiceManager(KubernetesClient client) {
        super(client, Service.class, "/api/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "services";
    }
}
