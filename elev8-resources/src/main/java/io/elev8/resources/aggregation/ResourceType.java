package io.elev8.resources.aggregation;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Catalog of Kubernetes resource types supported by CloudKubernetesClient.
 * Each constant carries API metadata and scope information used by
 * {@link ResourceAggregator} to dispatch list/count operations.
 */
public enum ResourceType {

    // Namespaced resources
    POD("v1", "Pod", "pods", Scope.NAMESPACED),
    SERVICE("v1", "Service", "services", Scope.NAMESPACED),
    CONFIG_MAP("v1", "ConfigMap", "configmaps", Scope.NAMESPACED),
    SECRET("v1", "Secret", "secrets", Scope.NAMESPACED),
    EVENT("v1", "Event", "events", Scope.NAMESPACED),
    PERSISTENT_VOLUME_CLAIM("v1", "PersistentVolumeClaim", "persistentvolumeclaims", Scope.NAMESPACED),
    SERVICE_ACCOUNT("v1", "ServiceAccount", "serviceaccounts", Scope.NAMESPACED),
    LIMIT_RANGE("v1", "LimitRange", "limitranges", Scope.NAMESPACED),
    RESOURCE_QUOTA("v1", "ResourceQuota", "resourcequotas", Scope.NAMESPACED),
    DEPLOYMENT("apps/v1", "Deployment", "deployments", Scope.NAMESPACED),
    DAEMON_SET("apps/v1", "DaemonSet", "daemonsets", Scope.NAMESPACED),
    STATEFUL_SET("apps/v1", "StatefulSet", "statefulsets", Scope.NAMESPACED),
    REPLICA_SET("apps/v1", "ReplicaSet", "replicasets", Scope.NAMESPACED),
    JOB("batch/v1", "Job", "jobs", Scope.NAMESPACED),
    CRON_JOB("batch/v1", "CronJob", "cronjobs", Scope.NAMESPACED),
    INGRESS("networking.k8s.io/v1", "Ingress", "ingresses", Scope.NAMESPACED),
    NETWORK_POLICY("networking.k8s.io/v1", "NetworkPolicy", "networkpolicies", Scope.NAMESPACED),
    HORIZONTAL_POD_AUTOSCALER("autoscaling/v2", "HorizontalPodAutoscaler", "horizontalpodautoscalers", Scope.NAMESPACED),
    VERTICAL_POD_AUTOSCALER("autoscaling.k8s.io/v1", "VerticalPodAutoscaler", "verticalpodautoscalers", Scope.NAMESPACED),
    POD_DISRUPTION_BUDGET("policy/v1", "PodDisruptionBudget", "poddisruptionbudgets", Scope.NAMESPACED),
    LEASE("coordination.k8s.io/v1", "Lease", "leases", Scope.NAMESPACED),
    ROLE("rbac.authorization.k8s.io/v1", "Role", "roles", Scope.NAMESPACED),
    ROLE_BINDING("rbac.authorization.k8s.io/v1", "RoleBinding", "rolebindings", Scope.NAMESPACED),

    // Cluster-scoped resources
    NAMESPACE("v1", "Namespace", "namespaces", Scope.CLUSTER),
    PERSISTENT_VOLUME("v1", "PersistentVolume", "persistentvolumes", Scope.CLUSTER),
    CLUSTER_ROLE("rbac.authorization.k8s.io/v1", "ClusterRole", "clusterroles", Scope.CLUSTER),
    CLUSTER_ROLE_BINDING("rbac.authorization.k8s.io/v1", "ClusterRoleBinding", "clusterrolebindings", Scope.CLUSTER),
    CUSTOM_RESOURCE_DEFINITION("apiextensions.k8s.io/v1", "CustomResourceDefinition", "customresourcedefinitions", Scope.CLUSTER);

    /**
     * Common resource types mirroring {@code kubectl get all}.
     */
    public static final Set<ResourceType> COMMON = Collections.unmodifiableSet(EnumSet.of(
            POD, SERVICE, DEPLOYMENT, DAEMON_SET, STATEFUL_SET,
            REPLICA_SET, JOB, CRON_JOB, HORIZONTAL_POD_AUTOSCALER
    ));

    /**
     * All namespaced resource types.
     */
    public static final Set<ResourceType> ALL_NAMESPACED = Collections.unmodifiableSet(EnumSet.of(
            POD, SERVICE, CONFIG_MAP, SECRET, EVENT,
            PERSISTENT_VOLUME_CLAIM, SERVICE_ACCOUNT, LIMIT_RANGE, RESOURCE_QUOTA,
            DEPLOYMENT, DAEMON_SET, STATEFUL_SET, REPLICA_SET,
            JOB, CRON_JOB,
            INGRESS, NETWORK_POLICY,
            HORIZONTAL_POD_AUTOSCALER, VERTICAL_POD_AUTOSCALER,
            POD_DISRUPTION_BUDGET, LEASE,
            ROLE, ROLE_BINDING
    ));

    /**
     * All cluster-scoped resource types.
     */
    public static final Set<ResourceType> ALL_CLUSTER = Collections.unmodifiableSet(EnumSet.of(
            NAMESPACE, PERSISTENT_VOLUME,
            CLUSTER_ROLE, CLUSTER_ROLE_BINDING,
            CUSTOM_RESOURCE_DEFINITION
    ));

    private final String apiVersion;
    private final String kind;
    private final String plural;
    private final Scope scope;

    ResourceType(final String apiVersion, final String kind, final String plural, final Scope scope) {
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.plural = plural;
        this.scope = scope;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public String getPlural() {
        return plural;
    }

    public Scope getScope() {
        return scope;
    }

    public boolean isNamespaced() {
        return scope == Scope.NAMESPACED;
    }

    public boolean isClusterScoped() {
        return scope == Scope.CLUSTER;
    }

    public enum Scope {
        NAMESPACED,
        CLUSTER
    }
}
