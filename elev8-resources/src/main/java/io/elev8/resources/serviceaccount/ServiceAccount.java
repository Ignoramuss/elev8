package io.elev8.resources.serviceaccount;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * ServiceAccount binds together a name, a principal that can be authenticated and authorized,
 * and a set of secrets. ServiceAccounts provide an identity for processes that run in a Pod.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceAccount extends AbstractResource {

    private ServiceAccountSpec spec;
    private ServiceAccountStatus status;

    public ServiceAccount() {
        super("v1", "ServiceAccount", null);
    }

    private ServiceAccount(final Builder builder) {
        super("v1", "ServiceAccount", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private ServiceAccountSpec spec;
        private ServiceAccountStatus status;

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

        public Builder spec(final ServiceAccountSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final ServiceAccountStatus status) {
            this.status = status;
            return this;
        }

        public ServiceAccount build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("ServiceAccount name is required");
            }
            return new ServiceAccount(this);
        }
    }
}
