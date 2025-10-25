package io.elev8.resources.deployment;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Deployment extends AbstractResource {

    private DeploymentSpec spec;
    private DeploymentStatus status;

    public Deployment() {
        super("apps/v1", "Deployment", null);
    }

    private Deployment(Builder builder) {
        super("apps/v1", "Deployment", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public DeploymentSpec getSpec() {
        return spec;
    }

    public void setSpec(DeploymentSpec spec) {
        this.spec = spec;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Metadata metadata;
        private DeploymentSpec spec;
        private DeploymentStatus status;

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

        public Builder spec(DeploymentSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(DeploymentStatus status) {
            this.status = status;
            return this;
        }

        public Deployment build() {
            if (metadata == null || metadata.getName() == null) {
                throw new IllegalArgumentException("Deployment name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("Deployment spec is required");
            }
            return new Deployment(this);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeploymentStatus {
        private Integer replicas;
        private Integer updatedReplicas;
        private Integer readyReplicas;
        private Integer availableReplicas;
        private Long observedGeneration;

        public DeploymentStatus() {
        }

        public Integer getReplicas() {
            return replicas;
        }

        public void setReplicas(Integer replicas) {
            this.replicas = replicas;
        }

        public Integer getUpdatedReplicas() {
            return updatedReplicas;
        }

        public void setUpdatedReplicas(Integer updatedReplicas) {
            this.updatedReplicas = updatedReplicas;
        }

        public Integer getReadyReplicas() {
            return readyReplicas;
        }

        public void setReadyReplicas(Integer readyReplicas) {
            this.readyReplicas = readyReplicas;
        }

        public Integer getAvailableReplicas() {
            return availableReplicas;
        }

        public void setAvailableReplicas(Integer availableReplicas) {
            this.availableReplicas = availableReplicas;
        }

        public Long getObservedGeneration() {
            return observedGeneration;
        }

        public void setObservedGeneration(Long observedGeneration) {
            this.observedGeneration = observedGeneration;
        }
    }
}
