package io.elev8.resources.aggregation;

import io.elev8.core.list.ListOptions;
import io.elev8.resources.KubernetesResource;
import io.elev8.resources.ResourceException;
import io.elev8.resources.cloud.CloudKubernetesClient;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Entry point for querying multiple resource types in a single call.
 *
 * <p>Example usage:
 * <pre>{@code
 * AggregateResult result = client.aggregate()
 *     .inNamespace("default")
 *     .common()
 *     .withLabelSelector("app=myapp")
 *     .list();
 * }</pre>
 */
public final class ResourceAggregator {

    private final CloudKubernetesClient client;

    public ResourceAggregator(final CloudKubernetesClient client) {
        this.client = Objects.requireNonNull(client, "client must not be null");
    }

    /**
     * Begin a namespaced query.
     *
     * @param namespace the namespace to query
     * @return a new namespaced query builder
     */
    public NamespacedQuery inNamespace(final String namespace) {
        return new NamespacedQuery(client, namespace);
    }

    /**
     * Begin a cluster-scoped query.
     *
     * @return a new cluster query builder
     */
    public ClusterQuery clusterScoped() {
        return new ClusterQuery(client);
    }

    // ---- Namespaced query ----

    public static final class NamespacedQuery {

        private final CloudKubernetesClient client;
        private final String namespace;
        private final Set<ResourceType> types = EnumSet.noneOf(ResourceType.class);
        private ListOptions listOptions;

        private NamespacedQuery(final CloudKubernetesClient client, final String namespace) {
            this.client = client;
            this.namespace = Objects.requireNonNull(namespace, "namespace must not be null");
        }

        /**
         * Add specific resource types to the query.
         *
         * @throws IllegalArgumentException if any type is not namespaced
         */
        public NamespacedQuery types(final ResourceType... resourceTypes) {
            for (final ResourceType type : resourceTypes) {
                validateNamespaced(type);
            }
            types.addAll(Arrays.asList(resourceTypes));
            return this;
        }

        /**
         * Add specific resource types to the query.
         *
         * @throws IllegalArgumentException if any type is not namespaced
         */
        public NamespacedQuery types(final Set<ResourceType> resourceTypes) {
            for (final ResourceType type : resourceTypes) {
                validateNamespaced(type);
            }
            types.addAll(resourceTypes);
            return this;
        }

        /**
         * Add all namespaced resource types to the query.
         */
        public NamespacedQuery allTypes() {
            types.addAll(ResourceType.ALL_NAMESPACED);
            return this;
        }

        /**
         * Add the common set of resource types (mirroring {@code kubectl get all}).
         */
        public NamespacedQuery common() {
            types.addAll(ResourceType.COMMON);
            return this;
        }

        /**
         * Set list options for filtering and pagination.
         */
        public NamespacedQuery withListOptions(final ListOptions options) {
            this.listOptions = options;
            return this;
        }

        /**
         * Convenience method to set a label selector string.
         */
        public NamespacedQuery withLabelSelector(final String labelSelector) {
            this.listOptions = ListOptions.withLabelSelector(labelSelector);
            return this;
        }

        /**
         * Execute the query and return all matching resources.
         *
         * @throws ResourceException if any manager call fails
         * @throws IllegalStateException if no types have been selected
         */
        public AggregateResult list() throws ResourceException {
            if (types.isEmpty()) {
                throw new IllegalStateException("No resource types selected");
            }
            final Map<ResourceType, List<? extends KubernetesResource>> results = new EnumMap<>(ResourceType.class);
            for (final ResourceType type : types) {
                results.put(type, listNamespaced(client, type, namespace, listOptions));
            }
            return new AggregateResult(results);
        }

        /**
         * Execute the query and return counts per type.
         *
         * @throws ResourceException if any manager call fails
         * @throws IllegalStateException if no types have been selected
         */
        public ResourceCounts count() throws ResourceException {
            final AggregateResult result = list();
            return result.counts();
        }

        private static void validateNamespaced(final ResourceType type) {
            if (!type.isNamespaced()) {
                throw new IllegalArgumentException("Not a namespaced type: " + type);
            }
        }
    }

    // ---- Cluster query ----

    public static final class ClusterQuery {

        private final CloudKubernetesClient client;
        private final Set<ResourceType> types = EnumSet.noneOf(ResourceType.class);
        private ListOptions listOptions;

        private ClusterQuery(final CloudKubernetesClient client) {
            this.client = client;
        }

        /**
         * Add specific resource types to the query.
         *
         * @throws IllegalArgumentException if any type is not cluster-scoped
         */
        public ClusterQuery types(final ResourceType... resourceTypes) {
            for (final ResourceType type : resourceTypes) {
                validateClusterScoped(type);
            }
            types.addAll(Arrays.asList(resourceTypes));
            return this;
        }

        /**
         * Add specific resource types to the query.
         *
         * @throws IllegalArgumentException if any type is not cluster-scoped
         */
        public ClusterQuery types(final Set<ResourceType> resourceTypes) {
            for (final ResourceType type : resourceTypes) {
                validateClusterScoped(type);
            }
            types.addAll(resourceTypes);
            return this;
        }

        /**
         * Add all cluster-scoped resource types to the query.
         */
        public ClusterQuery allTypes() {
            types.addAll(ResourceType.ALL_CLUSTER);
            return this;
        }

        /**
         * Set list options for filtering and pagination.
         */
        public ClusterQuery withListOptions(final ListOptions options) {
            this.listOptions = options;
            return this;
        }

        /**
         * Convenience method to set a label selector string.
         */
        public ClusterQuery withLabelSelector(final String labelSelector) {
            this.listOptions = ListOptions.withLabelSelector(labelSelector);
            return this;
        }

        /**
         * Execute the query and return all matching resources.
         *
         * @throws ResourceException if any manager call fails
         * @throws IllegalStateException if no types have been selected
         */
        public AggregateResult list() throws ResourceException {
            if (types.isEmpty()) {
                throw new IllegalStateException("No resource types selected");
            }
            final Map<ResourceType, List<? extends KubernetesResource>> results = new EnumMap<>(ResourceType.class);
            for (final ResourceType type : types) {
                results.put(type, listCluster(client, type, listOptions));
            }
            return new AggregateResult(results);
        }

        /**
         * Execute the query and return counts per type.
         *
         * @throws ResourceException if any manager call fails
         * @throws IllegalStateException if no types have been selected
         */
        public ResourceCounts count() throws ResourceException {
            final AggregateResult result = list();
            return result.counts();
        }

        private static void validateClusterScoped(final ResourceType type) {
            if (!type.isClusterScoped()) {
                throw new IllegalArgumentException("Not a cluster-scoped type: " + type);
            }
        }
    }

    // ---- Dispatch ----

    private static List<? extends KubernetesResource> listNamespaced(
            final CloudKubernetesClient client,
            final ResourceType type,
            final String namespace,
            final ListOptions options) throws ResourceException {
        return switch (type) {
            case POD -> options != null ? client.pods().list(namespace, options) : client.pods().list(namespace);
            case SERVICE -> options != null ? client.services().list(namespace, options) : client.services().list(namespace);
            case CONFIG_MAP -> options != null ? client.configMaps().list(namespace, options) : client.configMaps().list(namespace);
            case SECRET -> options != null ? client.secrets().list(namespace, options) : client.secrets().list(namespace);
            case EVENT -> options != null ? client.events().list(namespace, options) : client.events().list(namespace);
            case PERSISTENT_VOLUME_CLAIM -> options != null ? client.persistentVolumeClaims().list(namespace, options) : client.persistentVolumeClaims().list(namespace);
            case SERVICE_ACCOUNT -> options != null ? client.serviceAccounts().list(namespace, options) : client.serviceAccounts().list(namespace);
            case LIMIT_RANGE -> options != null ? client.limitRanges().list(namespace, options) : client.limitRanges().list(namespace);
            case RESOURCE_QUOTA -> options != null ? client.resourceQuotas().list(namespace, options) : client.resourceQuotas().list(namespace);
            case DEPLOYMENT -> options != null ? client.deployments().list(namespace, options) : client.deployments().list(namespace);
            case DAEMON_SET -> options != null ? client.daemonSets().list(namespace, options) : client.daemonSets().list(namespace);
            case STATEFUL_SET -> options != null ? client.statefulSets().list(namespace, options) : client.statefulSets().list(namespace);
            case REPLICA_SET -> options != null ? client.replicaSets().list(namespace, options) : client.replicaSets().list(namespace);
            case JOB -> options != null ? client.jobs().list(namespace, options) : client.jobs().list(namespace);
            case CRON_JOB -> options != null ? client.cronJobs().list(namespace, options) : client.cronJobs().list(namespace);
            case INGRESS -> options != null ? client.ingresses().list(namespace, options) : client.ingresses().list(namespace);
            case NETWORK_POLICY -> options != null ? client.networkPolicies().list(namespace, options) : client.networkPolicies().list(namespace);
            case HORIZONTAL_POD_AUTOSCALER -> options != null ? client.horizontalPodAutoscalers().list(namespace, options) : client.horizontalPodAutoscalers().list(namespace);
            case VERTICAL_POD_AUTOSCALER -> options != null ? client.verticalPodAutoscalers().list(namespace, options) : client.verticalPodAutoscalers().list(namespace);
            case POD_DISRUPTION_BUDGET -> options != null ? client.podDisruptionBudgets().list(namespace, options) : client.podDisruptionBudgets().list(namespace);
            case LEASE -> options != null ? client.leases().list(namespace, options) : client.leases().list(namespace);
            case ROLE -> options != null ? client.roles().list(namespace, options) : client.roles().list(namespace);
            case ROLE_BINDING -> options != null ? client.roleBindings().list(namespace, options) : client.roleBindings().list(namespace);
            default -> throw new IllegalArgumentException("Not a namespaced type: " + type);
        };
    }

    private static List<? extends KubernetesResource> listCluster(
            final CloudKubernetesClient client,
            final ResourceType type,
            final ListOptions options) throws ResourceException {
        return switch (type) {
            case NAMESPACE -> options != null ? client.namespaces().list(options) : client.namespaces().list();
            case PERSISTENT_VOLUME -> options != null ? client.persistentVolumes().list(options) : client.persistentVolumes().list();
            case CLUSTER_ROLE -> options != null ? client.clusterRoles().list(options) : client.clusterRoles().list();
            case CLUSTER_ROLE_BINDING -> options != null ? client.clusterRoleBindings().list(options) : client.clusterRoleBindings().list();
            case CUSTOM_RESOURCE_DEFINITION -> options != null ? client.customResourceDefinitions().list(options) : client.customResourceDefinitions().list();
            default -> throw new IllegalArgumentException("Not a cluster-scoped type: " + type);
        };
    }
}
