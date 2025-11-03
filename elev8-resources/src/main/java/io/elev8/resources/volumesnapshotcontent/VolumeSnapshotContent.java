package io.elev8.resources.volumesnapshotcontent;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * VolumeSnapshotContent represents the actual snapshot content in the underlying storage system.
 * It is a cluster-scoped resource that has a bidirectional binding with VolumeSnapshot.
 * VolumeSnapshotContent contains the details about the snapshot such as the snapshot ID on the
 * storage system and the binding reference to the VolumeSnapshot resource.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VolumeSnapshotContent extends AbstractResource {

    private VolumeSnapshotContentSpec spec;
    private VolumeSnapshotContentStatus status;

    public VolumeSnapshotContent() {
        super("snapshot.storage.k8s.io/v1", "VolumeSnapshotContent", null);
    }

    private VolumeSnapshotContent(final Builder builder) {
        super("snapshot.storage.k8s.io/v1", "VolumeSnapshotContent", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private VolumeSnapshotContentSpec spec;
        private VolumeSnapshotContentStatus status;

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

        public Builder spec(final VolumeSnapshotContentSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final VolumeSnapshotContentStatus status) {
            this.status = status;
            return this;
        }

        public VolumeSnapshotContent build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("VolumeSnapshotContent name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("VolumeSnapshotContent spec is required");
            }
            return new VolumeSnapshotContent(this);
        }
    }
}
