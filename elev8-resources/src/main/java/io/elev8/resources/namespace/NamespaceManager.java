package io.elev8.resources.namespace;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for Namespace resources.
 * Provides CRUD operations for Kubernetes Namespaces.
 *
 * Note: Unlike other resources, Namespaces are cluster-scoped and do not belong to a namespace.
 * Therefore, operations on this manager do not require a namespace parameter.
 */
public final class NamespaceManager extends AbstractResourceManager<Namespace> {

    public NamespaceManager(final KubernetesClient client) {
        super(client, Namespace.class, "/api/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "namespaces";
    }
}
