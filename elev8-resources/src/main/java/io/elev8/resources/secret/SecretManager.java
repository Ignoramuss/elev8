package io.elev8.resources.secret;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for Secret resources.
 * Provides CRUD operations for Kubernetes Secrets.
 */
public final class SecretManager extends AbstractResourceManager<Secret> {

    public SecretManager(final KubernetesClient client) {
        super(client, Secret.class, "/api/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "secrets";
    }
}
