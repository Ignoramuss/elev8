package io.elev8.resources.configmap;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for ConfigMap resources.
 * Provides CRUD operations for Kubernetes ConfigMaps.
 */
public final class ConfigMapManager extends AbstractResourceManager<ConfigMap> {

    public ConfigMapManager(final KubernetesClient client) {
        super(client, ConfigMap.class, "/api/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "configmaps";
    }
}
