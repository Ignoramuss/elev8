package io.elev8.resources.poddisruptionbudget;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * PodDisruptionBudget is an object to define the max disruption that can be caused to a collection of pods.
 * It limits the number of pods of a replicated application that are down simultaneously from voluntary disruptions.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PodDisruptionBudget extends AbstractResource {

    private PodDisruptionBudgetSpec spec;
    private PodDisruptionBudgetStatus status;

    public PodDisruptionBudget() {
        super("policy/v1", "PodDisruptionBudget", null);
    }

    private PodDisruptionBudget(final Builder builder) {
        super("policy/v1", "PodDisruptionBudget", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private PodDisruptionBudgetSpec spec;
        private PodDisruptionBudgetStatus status;

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

        public Builder spec(final PodDisruptionBudgetSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final PodDisruptionBudgetStatus status) {
            this.status = status;
            return this;
        }

        public PodDisruptionBudget build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("PodDisruptionBudget name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("PodDisruptionBudget spec is required");
            }
            return new PodDisruptionBudget(this);
        }
    }
}
