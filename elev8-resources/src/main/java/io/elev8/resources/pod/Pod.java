package io.elev8.resources.pod;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pod extends AbstractResource {

    PodSpec spec;
    PodStatus status;

    public Pod() {
        super("v1", "Pod", null);
    }

    private Pod(final Builder builder) {
        super("v1", "Pod", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private PodSpec spec;
        private PodStatus status;

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

        public Builder spec(final PodSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final PodStatus status) {
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
