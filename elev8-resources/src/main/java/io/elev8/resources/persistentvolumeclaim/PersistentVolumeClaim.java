package io.elev8.resources.persistentvolumeclaim;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * PersistentVolumeClaim is a user's request for and claim to a persistent volume.
 * Claims can request specific size and access modes (e.g., they can be mounted
 * ReadWriteOnce, ReadOnlyMany or ReadWriteMany).
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersistentVolumeClaim extends AbstractResource {

    private PersistentVolumeClaimSpec spec;
    private PersistentVolumeClaimStatus status;

    public PersistentVolumeClaim() {
        super("v1", "PersistentVolumeClaim", null);
    }

    private PersistentVolumeClaim(final Builder builder) {
        super("v1", "PersistentVolumeClaim", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private PersistentVolumeClaimSpec spec;
        private PersistentVolumeClaimStatus status;

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

        public Builder spec(final PersistentVolumeClaimSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final PersistentVolumeClaimStatus status) {
            this.status = status;
            return this;
        }

        public PersistentVolumeClaim build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("PersistentVolumeClaim name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("PersistentVolumeClaim spec is required");
            }
            return new PersistentVolumeClaim(this);
        }
    }
}
