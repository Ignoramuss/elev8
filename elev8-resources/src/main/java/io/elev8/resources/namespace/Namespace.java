package io.elev8.resources.namespace;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Namespace provides a scope for names and is intended to be used in environments
 * with many users spread across multiple teams or projects.
 * Namespaces are a way to divide cluster resources between multiple users.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Namespace extends AbstractResource {

    /**
     * Phase constant for an active namespace.
     */
    public static final String PHASE_ACTIVE = "Active";

    /**
     * Phase constant for a terminating namespace.
     */
    public static final String PHASE_TERMINATING = "Terminating";

    private NamespaceSpec spec;
    private NamespaceStatus status;

    public Namespace() {
        super("v1", "Namespace", null);
    }

    private Namespace(final Builder builder) {
        super("v1", "Namespace", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private NamespaceSpec spec;
        private NamespaceStatus status;

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

        public Builder label(final String key, final String value) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata = Metadata.builder()
                    .name(this.metadata.getName())
                    .labels(this.metadata.getLabels())
                    .label(key, value)
                    .build();
            return this;
        }

        public Builder spec(final NamespaceSpec spec) {
            this.spec = spec;
            return this;
        }

        /**
         * Add a finalizer to the namespace.
         * Finalizers must be empty before a namespace can be permanently removed.
         *
         * @param finalizer the finalizer to add
         * @return this builder
         */
        public Builder addFinalizer(final String finalizer) {
            if (this.spec == null) {
                this.spec = NamespaceSpec.builder().finalizer(finalizer).build();
            } else {
                this.spec = this.spec.toBuilder().finalizer(finalizer).build();
            }
            return this;
        }

        /**
         * Set the list of finalizers for the namespace.
         *
         * @param finalizers the list of finalizers
         * @return this builder
         */
        public Builder finalizers(final List<String> finalizers) {
            if (this.spec == null) {
                this.spec = NamespaceSpec.builder().build();
            }
            this.spec = this.spec.toBuilder()
                    .clearFinalizers()
                    .finalizers(finalizers)
                    .build();
            return this;
        }

        public Builder status(final NamespaceStatus status) {
            this.status = status;
            return this;
        }

        public Namespace build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("Namespace name is required");
            }
            return new Namespace(this);
        }
    }
}
