package io.elev8.resources.lease;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * LeaseSpec defines the specification for a Lease object used for leader election.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeaseSpec {

    /**
     * The identity of the current holder of the lease.
     * If empty, the lease is available for acquisition.
     */
    private String holderIdentity;

    /**
     * The duration that non-leader candidates will wait before
     * forcefully acquiring leadership. This is measured against the
     * time of last observed renewTime.
     */
    private Integer leaseDurationSeconds;

    /**
     * The time when the current lease holder acquired the lease.
     */
    private Instant acquireTime;

    /**
     * The time when the current holder of the lease has last updated
     * the lease. This is used to determine if the lease has expired.
     */
    private Instant renewTime;

    /**
     * The number of times the lease has transitioned between holders.
     */
    private Integer leaseTransitions;
}
