package io.elev8.resources.ingress;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * Ingress is a collection of rules that allow inbound connections to reach the
 * endpoints defined by a backend. An Ingress can be configured to give services
 * externally-reachable urls, load balance traffic, terminate SSL, offer name
 * based virtual hosting etc.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Ingress extends AbstractResource {

    private IngressSpec spec;
    private IngressStatus status;

    public Ingress() {
        super("networking.k8s.io/v1", "Ingress", null);
    }

    private Ingress(final Builder builder) {
        super("networking.k8s.io/v1", "Ingress", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private IngressSpec spec;
        private IngressStatus status;

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

        public Builder spec(final IngressSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final IngressStatus status) {
            this.status = status;
            return this;
        }

        public Ingress build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("Ingress name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("Ingress spec is required");
            }
            return new Ingress(this);
        }
    }
}
