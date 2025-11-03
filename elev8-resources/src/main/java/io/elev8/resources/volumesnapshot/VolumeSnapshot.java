package io.elev8.resources.volumesnapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * VolumeSnapshot is a user's request for either creating a point-in-time snapshot of a
 * persistent volume, or binding to a pre-existing snapshot.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VolumeSnapshot extends AbstractResource {

    private VolumeSnapshotSpec spec;
    private VolumeSnapshotStatus status;

    public VolumeSnapshot() {
        super("snapshot.storage.k8s.io/v1", "VolumeSnapshot", null);
    }

    private VolumeSnapshot(final Builder builder) {
        super("snapshot.storage.k8s.io/v1", "VolumeSnapshot", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private VolumeSnapshotSpec spec;
        private VolumeSnapshotStatus status;

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

        public Builder spec(final VolumeSnapshotSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final VolumeSnapshotStatus status) {
            this.status = status;
            return this;
        }

        public VolumeSnapshot build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("VolumeSnapshot name is required");
            }
            if (metadata.getNamespace() == null || metadata.getNamespace().isEmpty()) {
                throw new IllegalArgumentException("VolumeSnapshot namespace is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("VolumeSnapshot spec is required");
            }
            return new VolumeSnapshot(this);
        }
    }
}
