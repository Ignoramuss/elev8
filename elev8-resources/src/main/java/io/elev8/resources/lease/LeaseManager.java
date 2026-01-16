package io.elev8.resources.lease;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for Lease resources in the coordination.k8s.io/v1 API group.
 * Provides CRUD operations for namespace-scoped Lease objects used in leader election.
 *
 * <p>Leases are commonly used for:</p>
 * <ul>
 *   <li>Leader election in distributed controllers</li>
 *   <li>Node heartbeats (kubelet uses Lease objects)</li>
 *   <li>Distributed locking patterns</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * LeaseManager leaseManager = eksClient.leases();
 *
 * // Create a lease for leader election
 * Lease lease = Lease.builder()
 *     .namespace("kube-system")
 *     .name("my-controller-leader")
 *     .holderIdentity("pod-abc123")
 *     .leaseDurationSeconds(15)
 *     .build();
 *
 * Lease created = leaseManager.create(lease);
 * }</pre>
 */
public class LeaseManager extends AbstractResourceManager<Lease> {

    private static final String API_PATH = "/apis/coordination.k8s.io/v1";

    public LeaseManager(final KubernetesClient client) {
        super(client, Lease.class, API_PATH);
    }

    @Override
    protected String getResourceTypePlural() {
        return "leases";
    }
}
