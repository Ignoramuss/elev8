package io.elev8.resources.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Service extends AbstractResource {

    private ServiceSpec spec;
    private ServiceStatus status;

    public Service() {
        super("v1", "Service", null);
    }

    private Service(Builder builder) {
        super("v1", "Service", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public ServiceSpec getSpec() {
        return spec;
    }

    public void setSpec(ServiceSpec spec) {
        this.spec = spec;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Metadata metadata;
        private ServiceSpec spec;
        private ServiceStatus status;

        public Builder metadata(Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder name(String name) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata.setName(name);
            return this;
        }

        public Builder namespace(String namespace) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata.setNamespace(namespace);
            return this;
        }

        public Builder label(String key, String value) {
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

        public Builder spec(ServiceSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(ServiceStatus status) {
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServiceStatus {
        private LoadBalancerStatus loadBalancer;

        public ServiceStatus() {
        }

        public LoadBalancerStatus getLoadBalancer() {
            return loadBalancer;
        }

        public void setLoadBalancer(LoadBalancerStatus loadBalancer) {
            this.loadBalancer = loadBalancer;
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class LoadBalancerStatus {
            private String ingress;

            public LoadBalancerStatus() {
            }

            public String getIngress() {
                return ingress;
            }

            public void setIngress(String ingress) {
                this.ingress = ingress;
            }
        }
    }
}
