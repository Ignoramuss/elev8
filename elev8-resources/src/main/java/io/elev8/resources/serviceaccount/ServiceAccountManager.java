package io.elev8.resources.serviceaccount;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for ServiceAccount resources.
 * Provides CRUD operations for Kubernetes ServiceAccounts.
 *
 * ServiceAccounts provide an identity for processes that run in a Pod. When you create
 * a pod, if you do not specify a service account, it is automatically assigned the default
 * service account in the same namespace.
 */
public final class ServiceAccountManager extends AbstractResourceManager<ServiceAccount> {

    public ServiceAccountManager(final KubernetesClient client) {
        super(client, ServiceAccount.class, "/api/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "serviceaccounts";
    }
}
