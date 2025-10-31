package io.elev8.resources.networkpolicy;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for NetworkPolicy resources.
 * Provides CRUD operations for Kubernetes NetworkPolicies in the networking.k8s.io/v1 API group.
 *
 * NetworkPolicy provides network segmentation and controls ingress/egress traffic for pods
 * based on pod selectors, namespace selectors, and IP blocks. It enables fine-grained control
 * over network traffic for enhanced security and compliance.
 */
public final class NetworkPolicyManager extends AbstractResourceManager<NetworkPolicy> {

    public NetworkPolicyManager(final KubernetesClient client) {
        super(client, NetworkPolicy.class, "/apis/networking.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "networkpolicies";
    }
}
