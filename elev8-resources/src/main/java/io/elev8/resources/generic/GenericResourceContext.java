package io.elev8.resources.generic;

import lombok.Builder;
import lombok.Getter;

/**
 * Immutable context holding configuration for generic custom resource operations.
 * Contains API group, version, kind, plural name, and scope information needed
 * to construct API paths and interact with custom resources.
 */
@Getter
@Builder
public final class GenericResourceContext {

    private final String group;
    private final String version;
    private final String kind;
    private final String plural;
    private final ResourceScope scope;

    /**
     * Enum representing the scope of a Kubernetes resource.
     */
    public enum ResourceScope {
        NAMESPACED,
        CLUSTER
    }

    /**
     * Get the API version in Kubernetes format (e.g., "v1" for core, "group/version" for extensions).
     *
     * @return the API version string
     */
    public String getApiVersion() {
        if (group == null || group.isEmpty()) {
            return version;
        }
        return group + "/" + version;
    }

    /**
     * Get the API path for this resource type (e.g., "/api/v1" for core, "/apis/group/version" for extensions).
     *
     * @return the API path
     */
    public String getApiPath() {
        if (group == null || group.isEmpty()) {
            return "/api/" + version;
        }
        return "/apis/" + group + "/" + version;
    }

    /**
     * Check if this resource is namespace-scoped.
     *
     * @return true if namespace-scoped, false otherwise
     */
    public boolean isNamespaced() {
        return scope == ResourceScope.NAMESPACED;
    }

    /**
     * Check if this resource is cluster-scoped.
     *
     * @return true if cluster-scoped, false otherwise
     */
    public boolean isClusterScoped() {
        return scope == ResourceScope.CLUSTER;
    }

    /**
     * Create a context for a core Kubernetes resource (no group, uses /api/v1).
     *
     * @param version the API version (e.g., "v1")
     * @param kind the resource kind (e.g., "Pod")
     * @param plural the plural name (e.g., "pods")
     * @param scope the resource scope
     * @return the context
     */
    public static GenericResourceContext forCoreResource(final String version,
                                                         final String kind,
                                                         final String plural,
                                                         final ResourceScope scope) {
        return GenericResourceContext.builder()
                .group(null)
                .version(version)
                .kind(kind)
                .plural(plural)
                .scope(scope)
                .build();
    }

    /**
     * Create a context for a custom resource (has group, uses /apis/group/version).
     *
     * @param group the API group (e.g., "stable.example.com")
     * @param version the API version (e.g., "v1")
     * @param kind the resource kind (e.g., "CronTab")
     * @param plural the plural name (e.g., "crontabs")
     * @param scope the resource scope
     * @return the context
     */
    public static GenericResourceContext forCustomResource(final String group,
                                                           final String version,
                                                           final String kind,
                                                           final String plural,
                                                           final ResourceScope scope) {
        return GenericResourceContext.builder()
                .group(group)
                .version(version)
                .kind(kind)
                .plural(plural)
                .scope(scope)
                .build();
    }

    /**
     * Create a context for a namespace-scoped custom resource.
     *
     * @param group the API group
     * @param version the API version
     * @param kind the resource kind
     * @param plural the plural name
     * @return the context
     */
    public static GenericResourceContext forNamespacedResource(final String group,
                                                               final String version,
                                                               final String kind,
                                                               final String plural) {
        return forCustomResource(group, version, kind, plural, ResourceScope.NAMESPACED);
    }

    /**
     * Create a context for a cluster-scoped custom resource.
     *
     * @param group the API group
     * @param version the API version
     * @param kind the resource kind
     * @param plural the plural name
     * @return the context
     */
    public static GenericResourceContext forClusterResource(final String group,
                                                            final String version,
                                                            final String kind,
                                                            final String plural) {
        return forCustomResource(group, version, kind, plural, ResourceScope.CLUSTER);
    }
}
