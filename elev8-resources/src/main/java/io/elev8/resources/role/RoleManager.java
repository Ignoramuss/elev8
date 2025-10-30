package io.elev8.resources.role;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for Role resources.
 * Provides CRUD operations for Kubernetes Roles in the rbac.authorization.k8s.io/v1 API group.
 *
 * Roles are namespaced and define permissions (via PolicyRules) for accessing Kubernetes resources
 * within a specific namespace. They must be bound to subjects (users, groups, or service accounts)
 * via RoleBindings to take effect.
 */
public final class RoleManager extends AbstractResourceManager<Role> {

    public RoleManager(final KubernetesClient client) {
        super(client, Role.class, "/apis/rbac.authorization.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "roles";
    }
}
