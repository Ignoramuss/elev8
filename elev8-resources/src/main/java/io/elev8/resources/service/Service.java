package io.elev8.resources.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Service extends AbstractResource {

    ServiceSpec spec;
    ServiceStatus status;

    public Service() {
        super("v1", "Service", null);
    }

    private Service(final Builder builder) {
        super("v1", "Service", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private ServiceSpec spec;
        private ServiceStatus status;

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

        public Builder spec(final ServiceSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final ServiceStatus status) {
            this.status = status;
            return this;
        }

        public Service build() {
            if (metadata == null || metadata.getName() == null) {
                throw new IllegalArgumentException("Service name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("Service spec is required");
            }
            return new Service(this);
        }
    }

    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServiceStatus {
        LoadBalancerStatus loadBalancer;

        @Data
        @NoArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class LoadBalancerStatus {
            String ingress;
        }
    }
}
