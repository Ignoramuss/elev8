package io.elev8.resources.volumesnapshotclass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * VolumeSnapshotClass specifies parameters that a snapshot provisioner uses when creating volume snapshots.
 * It is a cluster-scoped resource used to define different classes of snapshot storage.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VolumeSnapshotClass extends AbstractResource {

    private String driver;
    private String deletionPolicy;
    private Map<String, String> parameters;

    public VolumeSnapshotClass() {
        super("snapshot.storage.k8s.io/v1", "VolumeSnapshotClass", null);
    }

    private VolumeSnapshotClass(final Builder builder) {
        super("snapshot.storage.k8s.io/v1", "VolumeSnapshotClass", builder.metadata);
        this.driver = builder.driver;
        this.deletionPolicy = builder.deletionPolicy;
        this.parameters = builder.parameters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private String driver;
        private String deletionPolicy;
        private Map<String, String> parameters;

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

        public Builder driver(final String driver) {
            this.driver = driver;
            return this;
        }

        public Builder deletionPolicy(final String deletionPolicy) {
            this.deletionPolicy = deletionPolicy;
            return this;
        }

        public Builder parameters(final Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder parameter(final String key, final String value) {
            if (this.parameters == null) {
                this.parameters = new java.util.HashMap<>();
            }
            this.parameters.put(key, value);
            return this;
        }

        public VolumeSnapshotClass build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("VolumeSnapshotClass name is required");
            }
            if (driver == null || driver.isEmpty()) {
                throw new IllegalArgumentException("VolumeSnapshotClass driver is required");
            }
            if (deletionPolicy == null || deletionPolicy.isEmpty()) {
                throw new IllegalArgumentException("VolumeSnapshotClass deletionPolicy is required");
            }
            return new VolumeSnapshotClass(this);
        }
    }
}
