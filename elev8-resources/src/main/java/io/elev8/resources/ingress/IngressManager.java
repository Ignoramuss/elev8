package io.elev8.resources.ingress;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for Ingress resources.
 * Provides CRUD operations for Kubernetes Ingresses.
 *
 * Ingress exposes HTTP and HTTPS routes from outside the cluster to services within the cluster.
 * Traffic routing is controlled by rules defined on the Ingress resource.
 */
public final class IngressManager extends AbstractResourceManager<Ingress> {

    public IngressManager(final KubernetesClient client) {
        super(client, Ingress.class, "/apis/networking.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "ingresses";
    }
}
