package io.elev8.resources.rolebinding;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for RoleBinding resources.
 * Provides CRUD operations for Kubernetes RoleBindings in the rbac.authorization.k8s.io/v1 API group.
 *
 * RoleBindings are namespaced and grant the permissions defined in a Role to a set of subjects
 * (users, groups, or service accounts). RoleBindings can reference Roles in the same namespace
 * or ClusterRoles in the global namespace.
 */
public final class RoleBindingManager extends AbstractResourceManager<RoleBinding> {

    public RoleBindingManager(final KubernetesClient client) {
        super(client, RoleBinding.class, "/apis/rbac.authorization.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "rolebindings";
    }
}
