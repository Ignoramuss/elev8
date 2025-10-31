package io.elev8.resources.clusterrole;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractClusterResourceManager;

/**
 * Manager for ClusterRole resources.
 * Provides CRUD operations for Kubernetes ClusterRoles in the rbac.authorization.k8s.io/v1 API group.
 *
 * ClusterRoles are cluster-scoped and define permissions (via PolicyRules) for accessing Kubernetes
 * resources across the entire cluster. They must be bound to subjects (users, groups, or service accounts)
 * via ClusterRoleBindings or RoleBindings to take effect.
 */
public final class ClusterRoleManager extends AbstractClusterResourceManager<ClusterRole> {

    public ClusterRoleManager(final KubernetesClient client) {
        super(client, ClusterRole.class, "/apis/rbac.authorization.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "clusterroles";
    }
}
