package io.elev8.resources.rolebinding;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * RoleBinding references a Role, but grants the permissions defined in that role to a user or set of users.
 * It holds a list of subjects (users, groups, or service accounts), and a reference to the role being granted.
 * RoleBindings are namespace-scoped and can only reference Roles in the same namespace, or ClusterRoles.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleBinding extends AbstractResource {

    private RoleBindingSpec spec;

    public RoleBinding() {
        super("rbac.authorization.k8s.io/v1", "RoleBinding", null);
    }

    private RoleBinding(final Builder builder) {
        super("rbac.authorization.k8s.io/v1", "RoleBinding", builder.metadata);
        this.spec = builder.spec;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private RoleBindingSpec spec;

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

        public Builder spec(final RoleBindingSpec spec) {
            this.spec = spec;
            return this;
        }

        public RoleBinding build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("RoleBinding name is required");
            }
            return new RoleBinding(this);
        }
    }
}
