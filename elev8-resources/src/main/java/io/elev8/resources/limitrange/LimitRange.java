package io.elev8.resources.limitrange;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * LimitRange sets resource usage limits for each kind of resource in a Namespace.
 * It can set default request/limit for compute resources, enforce min/max usage,
 * and enforce the ratio between request and limit for a resource in a namespace.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LimitRange extends AbstractResource {

    private LimitRangeSpec spec;

    public LimitRange() {
        super("v1", "LimitRange", null);
    }

    private LimitRange(final Builder builder) {
        super("v1", "LimitRange", builder.metadata);
        this.spec = builder.spec;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private LimitRangeSpec spec;

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

        public Builder spec(final LimitRangeSpec spec) {
            this.spec = spec;
            return this;
        }

        public LimitRange build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("LimitRange name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("LimitRange spec is required");
            }
            return new LimitRange(this);
        }
    }
}
