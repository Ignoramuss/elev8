package io.elev8.resources.persistentvolume;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * PersistentVolume (PV) is a piece of storage in the cluster that has been provisioned
 * by an administrator or dynamically provisioned using Storage Classes. It is a resource
 * in the cluster just like a node is a cluster resource. PVs are volume plugins like
 * Volumes, but have a lifecycle independent of any individual Pod that uses the PV.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersistentVolume extends AbstractResource {

    private PersistentVolumeSpec spec;
    private PersistentVolumeStatus status;

    public PersistentVolume() {
        super("v1", "PersistentVolume", null);
    }

    private PersistentVolume(final Builder builder) {
        super("v1", "PersistentVolume", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private PersistentVolumeSpec spec;
        private PersistentVolumeStatus status;

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

        public Builder spec(final PersistentVolumeSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final PersistentVolumeStatus status) {
            this.status = status;
            return this;
        }

        public PersistentVolume build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("PersistentVolume name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("PersistentVolume spec is required");
            }
            return new PersistentVolume(this);
        }
    }
}
