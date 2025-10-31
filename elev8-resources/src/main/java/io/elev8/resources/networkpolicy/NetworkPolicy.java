package io.elev8.resources.networkpolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * NetworkPolicy describes what network traffic is allowed for a set of Pods.
 * It provides network segmentation by controlling ingress and egress traffic based on
 * pod selectors, namespace selectors, and IP blocks.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NetworkPolicy extends AbstractResource {

    private NetworkPolicySpec spec;

    public NetworkPolicy() {
        super("networking.k8s.io/v1", "NetworkPolicy", null);
    }

    private NetworkPolicy(final Builder builder) {
        super("networking.k8s.io/v1", "NetworkPolicy", builder.metadata);
        this.spec = builder.spec;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private NetworkPolicySpec spec;

        private Builder() {
        }

        public Builder metadata(final Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder name(final String name) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata.setName(name);
            return this;
        }

        public Builder namespace(final String namespace) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata.setNamespace(namespace);
            return this;
        }

        public Builder label(final String key, final String value) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata = Metadata.builder()
                    .name(this.metadata.getName())
                    .namespace(this.metadata.getNamespace())
                    .labels(this.metadata.getLabels())
                    .label(key, value)
                    .build();
            return this;
        }

        public Builder spec(final NetworkPolicySpec spec) {
            this.spec = spec;
            return this;
        }

        public NetworkPolicy build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("NetworkPolicy name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("NetworkPolicy spec is required");
            }
            return new NetworkPolicy(this);
        }
    }
}
