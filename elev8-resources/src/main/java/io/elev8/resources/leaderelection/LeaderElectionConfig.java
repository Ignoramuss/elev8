package io.elev8.resources.leaderelection;

import io.elev8.resources.lease.LeaseManager;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

/**
 * Configuration for leader election using Kubernetes Lease objects.
 *
 * <p>The election algorithm uses the following timing parameters:</p>
 * <ul>
 *   <li><b>leaseDuration</b>: How long a leader holds the lock before it expires</li>
 *   <li><b>renewDeadline</b>: How long the leader will try to renew before giving up</li>
 *   <li><b>retryPeriod</b>: How often to retry acquiring or renewing the lease</li>
 * </ul>
 *
 * <p>The following invariant must hold: renewDeadline + retryPeriod < leaseDuration</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * LeaderElectionConfig config = LeaderElectionConfig.builder()
 *     .leaseManager(eksClient.leases())
 *     .namespace("kube-system")
 *     .leaseName("my-controller-leader")
 *     .identity("pod-abc123")
 *     .leaseDuration(Duration.ofSeconds(15))
 *     .renewDeadline(Duration.ofSeconds(10))
 *     .retryPeriod(Duration.ofSeconds(2))
 *     .callbacks(myCallbacks)
 *     .build();
 * }</pre>
 */
@Getter
@Builder
public class LeaderElectionConfig {

    /**
     * The lease manager to use for CRUD operations on Lease objects.
     */
    private final LeaseManager leaseManager;

    /**
     * The namespace where the lease will be created.
     */
    private final String namespace;

    /**
     * The name of the lease resource.
     */
    private final String leaseName;

    /**
     * The unique identity of this leader election participant.
     * Typically includes pod name and UID for uniqueness.
     */
    private final String identity;

    /**
     * The duration that non-leader candidates will wait to force acquire leadership.
     * This is the TTL for the lease lock.
     */
    @Builder.Default
    private final Duration leaseDuration = Duration.ofSeconds(15);

    /**
     * The duration the acting leader will retry refreshing leadership before giving up.
     */
    @Builder.Default
    private final Duration renewDeadline = Duration.ofSeconds(10);

    /**
     * The duration between retries when acquiring or renewing the lease.
     */
    @Builder.Default
    private final Duration retryPeriod = Duration.ofSeconds(2);

    /**
     * Whether to release the lease on cancellation/shutdown.
     */
    @Builder.Default
    private final boolean releaseOnCancel = true;

    /**
     * Callbacks for leader election events.
     */
    private final LeaderCallbacks callbacks;

    /**
     * Validates the configuration.
     *
     * @throws IllegalArgumentException if the configuration is invalid
     */
    public void validate() {
        if (leaseManager == null) {
            throw new IllegalArgumentException("leaseManager is required");
        }
        if (namespace == null || namespace.isEmpty()) {
            throw new IllegalArgumentException("namespace is required");
        }
        if (leaseName == null || leaseName.isEmpty()) {
            throw new IllegalArgumentException("leaseName is required");
        }
        if (identity == null || identity.isEmpty()) {
            throw new IllegalArgumentException("identity is required");
        }
        if (callbacks == null) {
            throw new IllegalArgumentException("callbacks is required");
        }
        if (leaseDuration.compareTo(renewDeadline) <= 0) {
            throw new IllegalArgumentException("leaseDuration must be greater than renewDeadline");
        }
        if (renewDeadline.compareTo(retryPeriod) <= 0) {
            throw new IllegalArgumentException("renewDeadline must be greater than retryPeriod");
        }
    }
}
