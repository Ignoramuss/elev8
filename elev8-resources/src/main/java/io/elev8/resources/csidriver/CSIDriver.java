package io.elev8.resources.csidriver;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * CSIDriver captures information about a Container Storage Interface (CSI) volume driver
 * deployed on the cluster. CSIDriver objects are cluster-scoped (non-namespaced) resources.
 *
 * The attach detach controller uses this object to determine whether attach is required.
 * Kubelet uses this object to determine whether pod information needs to be passed on mount.
 *
 * The CSIDriver name must match the driver's GetPluginName() return value.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CSIDriver extends AbstractResource {

    private CSIDriverSpec spec;

    public CSIDriver() {
        super("storage.k8s.io/v1", "CSIDriver", null);
    }

    private CSIDriver(final Builder builder) {
        super("storage.k8s.io/v1", "CSIDriver", builder.metadata);
        this.spec = builder.spec;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private CSIDriverSpec spec;

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

        public Builder spec(final CSIDriverSpec spec) {
            this.spec = spec;
            return this;
        }

        public CSIDriver build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("CSIDriver name is required");
            }

            final String name = metadata.getName();
            if (name.length() > 63) {
                throw new IllegalArgumentException("CSIDriver name must be 63 characters or less");
            }
            if (!name.matches("^[a-zA-Z0-9]([a-zA-Z0-9.-]*[a-zA-Z0-9])?$")) {
                throw new IllegalArgumentException(
                        "CSIDriver name must begin and end with alphanumeric characters [a-zA-Z0-9], " +
                                "with dashes (-), dots (.), and alphanumerics between");
            }

            return new CSIDriver(this);
        }
    }
}
