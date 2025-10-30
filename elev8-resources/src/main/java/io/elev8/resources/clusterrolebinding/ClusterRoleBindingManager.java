package io.elev8.resources.clusterrolebinding;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractClusterResourceManager;

/**
 * Manager for ClusterRoleBinding resources.
 * Provides CRUD operations for Kubernetes ClusterRoleBindings in the rbac.authorization.k8s.io/v1 API group.
 *
 * ClusterRoleBindings are cluster-scoped and grant the permissions defined in a ClusterRole to a set of subjects
 * (users, groups, or service accounts) across the entire cluster. ClusterRoleBindings can only reference ClusterRoles.
 */
public final class ClusterRoleBindingManager extends AbstractClusterResourceManager<ClusterRoleBinding> {

    public ClusterRoleBindingManager(final KubernetesClient client) {
        super(client, ClusterRoleBinding.class, "/apis/rbac.authorization.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "clusterrolebindings";
    }
}
