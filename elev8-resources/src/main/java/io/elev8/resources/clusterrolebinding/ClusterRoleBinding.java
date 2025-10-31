package io.elev8.resources.clusterrolebinding;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * ClusterRoleBinding references a ClusterRole, but grants the permissions defined in that role
 * to a user or set of users at the cluster level.
 * It holds a list of subjects (users, groups, or service accounts), and a reference to the role being granted.
 * ClusterRoleBindings are cluster-scoped and grant permissions across the entire cluster.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClusterRoleBinding extends AbstractResource {

    private ClusterRoleBindingSpec spec;

    public ClusterRoleBinding() {
        super("rbac.authorization.k8s.io/v1", "ClusterRoleBinding", null);
    }

    private ClusterRoleBinding(final Builder builder) {
        super("rbac.authorization.k8s.io/v1", "ClusterRoleBinding", builder.metadata);
        this.spec = builder.spec;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private ClusterRoleBindingSpec spec;

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

        public Builder spec(final ClusterRoleBindingSpec spec) {
            this.spec = spec;
            return this;
        }

        public ClusterRoleBinding build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("ClusterRoleBinding name is required");
            }
            return new ClusterRoleBinding(this);
        }
    }
}
