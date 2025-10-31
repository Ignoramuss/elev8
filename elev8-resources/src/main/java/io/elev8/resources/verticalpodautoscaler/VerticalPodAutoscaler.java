package io.elev8.resources.verticalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * VerticalPodAutoscaler is a configuration for a vertical pod autoscaler, which automatically
 * adjusts the CPU and memory requests (and optionally limits) for containers in pods based on
 * observed usage patterns.
 *
 * NOTE: VPA is a Custom Resource Definition (CRD) and must be installed in the cluster before use.
 * See: https://github.com/kubernetes/autoscaler/tree/master/vertical-pod-autoscaler
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerticalPodAutoscaler extends AbstractResource {

    private VerticalPodAutoscalerSpec spec;
    private VerticalPodAutoscalerStatus status;

    public VerticalPodAutoscaler() {
        super("autoscaling.k8s.io/v1", "VerticalPodAutoscaler", null);
    }

    private VerticalPodAutoscaler(final Builder builder) {
        super("autoscaling.k8s.io/v1", "VerticalPodAutoscaler", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private VerticalPodAutoscalerSpec spec;
        private VerticalPodAutoscalerStatus status;

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

        public Builder spec(final VerticalPodAutoscalerSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final VerticalPodAutoscalerStatus status) {
            this.status = status;
            return this;
        }

        public VerticalPodAutoscaler build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("VerticalPodAutoscaler name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("VerticalPodAutoscaler spec is required");
            }
            return new VerticalPodAutoscaler(this);
        }
    }
}
