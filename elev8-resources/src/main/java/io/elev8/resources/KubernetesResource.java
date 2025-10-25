package io.elev8.resources;

/**
 * Base interface for all Kubernetes resources.
 */
public interface KubernetesResource {

    /**
     * Get the API version of this resource.
     *
     * @return the API version (e.g., "v1", "apps/v1")
     */
    String getApiVersion();

    /**
     * Get the kind of this resource.
     *
     * @return the resource kind (e.g., "Pod", "Service", "Deployment")
     */
    String getKind();

    /**
     * Get the metadata for this resource.
     *
     * @return the resource metadata
     */
    Metadata getMetadata();

    /**
     * Convert this resource to JSON.
     *
     * @return JSON representation of the resource
     */
    String toJson();

    /**
     * Get the resource name.
     *
     * @return the resource name
     */
    default String getName() {
        return getMetadata() != null ? getMetadata().getName() : null;
    }

    /**
     * Get the resource namespace.
     *
     * @return the resource namespace
     */
    default String getNamespace() {
        return getMetadata() != null ? getMetadata().getNamespace() : null;
    }
}
