package io.elev8.resources.replicaset;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ReplicaSet ensures that a specified number of pod replicas are running at any given time.
 * A ReplicaSet is defined by a selector that specifies how to identify pods it can acquire,
 * a number of replicas indicating how many Pods it should be maintaining, and a pod template
 * specifying the data of new Pods it should create to meet the number of replicas criteria.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReplicaSet extends AbstractResource {

    private ReplicaSetSpec spec;
    private ReplicaSetStatus status;

    public ReplicaSet() {
        super("apps/v1", "ReplicaSet", null);
    }

    private ReplicaSet(final Builder builder) {
        super("apps/v1", "ReplicaSet", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private ReplicaSetSpec spec;
        private ReplicaSetStatus status;

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

        public Builder spec(final ReplicaSetSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final ReplicaSetStatus status) {
            this.status = status;
            return this;
        }

        public ReplicaSet build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("ReplicaSet name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("ReplicaSet spec is required");
            }
            return new ReplicaSet(this);
        }
    }
}
