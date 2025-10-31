package io.elev8.resources.clusterrole;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import io.elev8.resources.role.RoleSpec;
import lombok.Getter;
import lombok.Setter;

/**
 * ClusterRole is a cluster-level, logical grouping of PolicyRules that can be referenced as a unit
 * by a RoleBinding or ClusterRoleBinding.
 * ClusterRoles define what actions can be performed on which resources across the entire cluster.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClusterRole extends AbstractResource {

    private RoleSpec spec;

    public ClusterRole() {
        super("rbac.authorization.k8s.io/v1", "ClusterRole", null);
    }

    private ClusterRole(final Builder builder) {
        super("rbac.authorization.k8s.io/v1", "ClusterRole", builder.metadata);
        this.spec = builder.spec;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private RoleSpec spec;

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

        public Builder spec(final RoleSpec spec) {
            this.spec = spec;
            return this;
        }

        public ClusterRole build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("ClusterRole name is required");
            }
            return new ClusterRole(this);
        }
    }
}
