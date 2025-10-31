package io.elev8.resources.horizontalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * HorizontalPodAutoscaler is a configuration for a horizontal pod autoscaler, which automatically
 * manages the replica count of any resource implementing the scale subresource based on the metrics specified.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HorizontalPodAutoscaler extends AbstractResource {

    private HorizontalPodAutoscalerSpec spec;
    private HorizontalPodAutoscalerStatus status;

    public HorizontalPodAutoscaler() {
        super("autoscaling/v2", "HorizontalPodAutoscaler", null);
    }

    private HorizontalPodAutoscaler(final Builder builder) {
        super("autoscaling/v2", "HorizontalPodAutoscaler", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private HorizontalPodAutoscalerSpec spec;
        private HorizontalPodAutoscalerStatus status;

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

        public Builder spec(final HorizontalPodAutoscalerSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final HorizontalPodAutoscalerStatus status) {
            this.status = status;
            return this;
        }

        public HorizontalPodAutoscaler build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("HorizontalPodAutoscaler name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("HorizontalPodAutoscaler spec is required");
            }
            return new HorizontalPodAutoscaler(this);
        }
    }
}
