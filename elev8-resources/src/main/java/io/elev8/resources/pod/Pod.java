package io.elev8.resources.pod;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pod extends AbstractResource {

    private PodSpec spec;
    private PodStatus status;

    public Pod() {
        super("v1", "Pod", null);
    }

    private Pod(Builder builder) {
        super("v1", "Pod", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public PodSpec getSpec() {
        return spec;
    }

    public void setSpec(PodSpec spec) {
        this.spec = spec;
    }

    public PodStatus getStatus() {
        return status;
    }

    public void setStatus(PodStatus status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Metadata metadata;
        private PodSpec spec;
        private PodStatus status;

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

        public Builder spec(PodSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(PodStatus status) {
            this.status = status;
            return this;
        }

        public Pod build() {
            if (metadata == null || metadata.getName() == null) {
                throw new IllegalArgumentException("Pod name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("Pod spec is required");
            }
            return new Pod(this);
        }
    }
}
