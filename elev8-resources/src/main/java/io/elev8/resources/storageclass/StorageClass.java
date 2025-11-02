package io.elev8.resources.storageclass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * StorageClass describes the parameters for a class of storage for which PersistentVolumes can be
 * dynamically provisioned. StorageClasses are cluster-scoped (non-namespaced) resources.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StorageClass extends AbstractResource {

    private String provisioner;
    private Map<String, String> parameters;
    private String reclaimPolicy;
    private String volumeBindingMode;
    private Boolean allowVolumeExpansion;
    private List<String> mountOptions;
    private List<TopologySelectorTerm> allowedTopologies;

    public StorageClass() {
        super("storage.k8s.io/v1", "StorageClass", null);
    }

    private StorageClass(final Builder builder) {
        super("storage.k8s.io/v1", "StorageClass", builder.metadata);
        this.provisioner = builder.provisioner;
        this.parameters = builder.parameters;
        this.reclaimPolicy = builder.reclaimPolicy;
        this.volumeBindingMode = builder.volumeBindingMode;
        this.allowVolumeExpansion = builder.allowVolumeExpansion;
        this.mountOptions = builder.mountOptions;
        this.allowedTopologies = builder.allowedTopologies;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private String provisioner;
        private Map<String, String> parameters;
        private String reclaimPolicy;
        private String volumeBindingMode;
        private Boolean allowVolumeExpansion;
        private List<String> mountOptions;
        private List<TopologySelectorTerm> allowedTopologies;

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

        public Builder provisioner(final String provisioner) {
            this.provisioner = provisioner;
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

        public Builder reclaimPolicy(final String reclaimPolicy) {
            this.reclaimPolicy = reclaimPolicy;
            return this;
        }

        public Builder volumeBindingMode(final String volumeBindingMode) {
            this.volumeBindingMode = volumeBindingMode;
            return this;
        }

        public Builder allowVolumeExpansion(final Boolean allowVolumeExpansion) {
            this.allowVolumeExpansion = allowVolumeExpansion;
            return this;
        }

        public Builder mountOptions(final List<String> mountOptions) {
            this.mountOptions = mountOptions;
            return this;
        }

        public Builder mountOption(final String mountOption) {
            if (this.mountOptions == null) {
                this.mountOptions = new java.util.ArrayList<>();
            }
            this.mountOptions.add(mountOption);
            return this;
        }

        public Builder allowedTopologies(final List<TopologySelectorTerm> allowedTopologies) {
            this.allowedTopologies = allowedTopologies;
            return this;
        }

        public Builder allowedTopology(final TopologySelectorTerm allowedTopology) {
            if (this.allowedTopologies == null) {
                this.allowedTopologies = new java.util.ArrayList<>();
            }
            this.allowedTopologies.add(allowedTopology);
            return this;
        }

        public StorageClass build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("StorageClass name is required");
            }
            if (provisioner == null || provisioner.isEmpty()) {
                throw new IllegalArgumentException("StorageClass provisioner is required");
            }
            return new StorageClass(this);
        }
    }
}
