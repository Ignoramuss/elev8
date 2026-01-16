package io.elev8.resources.lease;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * Lease defines a lease concept used for leader election and coordination.
 * Leases are namespace-scoped resources in the coordination.k8s.io/v1 API group.
 *
 * <p>A Lease object represents a distributed lock that can be held by a single
 * identity at a time. It is commonly used for leader election in Kubernetes
 * controllers and operators.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * Lease lease = Lease.builder()
 *     .namespace("kube-system")
 *     .name("my-controller-leader")
 *     .holderIdentity("controller-pod-abc123")
 *     .leaseDurationSeconds(15)
 *     .build();
 * }</pre>
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Lease extends AbstractResource {

    private static final String API_VERSION = "coordination.k8s.io/v1";
    private static final String KIND = "Lease";

    private LeaseSpec spec;

    public Lease() {
        super(API_VERSION, KIND, null);
    }

    private Lease(final Builder builder) {
        super(API_VERSION, KIND, builder.metadata);
        this.spec = builder.spec;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private LeaseSpec spec;
        private LeaseSpec.LeaseSpecBuilder specBuilder;

        public Builder metadata(final Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder name(final String name) {
            ensureMetadata();
            this.metadata.setName(name);
            return this;
        }

        public Builder namespace(final String namespace) {
            ensureMetadata();
            this.metadata.setNamespace(namespace);
            return this;
        }

        public Builder spec(final LeaseSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder holderIdentity(final String holderIdentity) {
            ensureSpecBuilder();
            this.specBuilder.holderIdentity(holderIdentity);
            return this;
        }

        public Builder leaseDurationSeconds(final Integer leaseDurationSeconds) {
            ensureSpecBuilder();
            this.specBuilder.leaseDurationSeconds(leaseDurationSeconds);
            return this;
        }

        public Builder acquireTime(final java.time.Instant acquireTime) {
            ensureSpecBuilder();
            this.specBuilder.acquireTime(acquireTime);
            return this;
        }

        public Builder renewTime(final java.time.Instant renewTime) {
            ensureSpecBuilder();
            this.specBuilder.renewTime(renewTime);
            return this;
        }

        public Builder leaseTransitions(final Integer leaseTransitions) {
            ensureSpecBuilder();
            this.specBuilder.leaseTransitions(leaseTransitions);
            return this;
        }

        public Lease build() {
            if (this.spec == null && this.specBuilder != null) {
                this.spec = this.specBuilder.build();
            }
            return new Lease(this);
        }

        private void ensureMetadata() {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
        }

        private void ensureSpecBuilder() {
            if (this.specBuilder == null) {
                this.specBuilder = LeaseSpec.builder();
            }
        }
    }
}
