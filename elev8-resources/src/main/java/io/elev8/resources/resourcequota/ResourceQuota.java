package io.elev8.resources.resourcequota;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * ResourceQuota sets aggregate quota restrictions enforced per namespace.
 * It provides constraints that limit aggregate resource consumption (CPU, memory, storage)
 * and object counts (pods, services, etc.) within a namespace for multi-tenancy and resource governance.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceQuota extends AbstractResource {

    private ResourceQuotaSpec spec;
    private ResourceQuotaStatus status;

    public ResourceQuota() {
        super("v1", "ResourceQuota", null);
    }

    private ResourceQuota(final Builder builder) {
        super("v1", "ResourceQuota", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private ResourceQuotaSpec spec;
        private ResourceQuotaStatus status;

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

        public Builder spec(final ResourceQuotaSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final ResourceQuotaStatus status) {
            this.status = status;
            return this;
        }

        public ResourceQuota build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("ResourceQuota name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("ResourceQuota spec is required");
            }
            return new ResourceQuota(this);
        }
    }
}
