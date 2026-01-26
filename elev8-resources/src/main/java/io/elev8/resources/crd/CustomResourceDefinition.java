package io.elev8.resources.crd;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * CustomResourceDefinition represents a resource that should be exposed on the API server.
 * Its name MUST be in the format &lt;.spec.name&gt;.&lt;.spec.group&gt;.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomResourceDefinition extends AbstractResource {

    private CustomResourceDefinitionSpec spec;
    private CustomResourceDefinitionStatus status;

    public CustomResourceDefinition() {
        super("apiextensions.k8s.io/v1", "CustomResourceDefinition", null);
    }

    private CustomResourceDefinition(final Builder builder) {
        super("apiextensions.k8s.io/v1", "CustomResourceDefinition", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private CustomResourceDefinitionSpec spec;
        private CustomResourceDefinitionStatus status;

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
                    .annotations(this.metadata.getAnnotations())
                    .label(key, value)
                    .build();
            return this;
        }

        public Builder annotation(final String key, final String value) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata = Metadata.builder()
                    .name(this.metadata.getName())
                    .labels(this.metadata.getLabels())
                    .annotations(this.metadata.getAnnotations())
                    .annotation(key, value)
                    .build();
            return this;
        }

        public Builder spec(final CustomResourceDefinitionSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final CustomResourceDefinitionStatus status) {
            this.status = status;
            return this;
        }

        public CustomResourceDefinition build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("CustomResourceDefinition name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("CustomResourceDefinition spec is required");
            }
            if (spec.getGroup() == null || spec.getGroup().isEmpty()) {
                throw new IllegalArgumentException("CustomResourceDefinition spec.group is required");
            }
            if (spec.getNames() == null) {
                throw new IllegalArgumentException("CustomResourceDefinition spec.names is required");
            }
            if (spec.getNames().getKind() == null || spec.getNames().getKind().isEmpty()) {
                throw new IllegalArgumentException("CustomResourceDefinition spec.names.kind is required");
            }
            if (spec.getNames().getPlural() == null || spec.getNames().getPlural().isEmpty()) {
                throw new IllegalArgumentException("CustomResourceDefinition spec.names.plural is required");
            }
            if (spec.getVersions() == null || spec.getVersions().isEmpty()) {
                throw new IllegalArgumentException("CustomResourceDefinition spec.versions is required");
            }
            return new CustomResourceDefinition(this);
        }
    }
}
