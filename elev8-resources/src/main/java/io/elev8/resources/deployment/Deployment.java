package io.elev8.resources.deployment;

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
public class Deployment extends AbstractResource {

    DeploymentSpec spec;
    DeploymentStatus status;

    public Deployment() {
        super("apps/v1", "Deployment", null);
    }

    private Deployment(final Builder builder) {
        super("apps/v1", "Deployment", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private DeploymentSpec spec;
        private DeploymentStatus status;

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

        public Builder spec(final DeploymentSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final DeploymentStatus status) {
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

    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeploymentStatus {
        Integer replicas;
        Integer updatedReplicas;
        Integer readyReplicas;
        Integer availableReplicas;
        Long observedGeneration;
    }
}
