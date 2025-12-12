package io.elev8.resources;

import io.elev8.core.client.KubernetesClient;
import io.elev8.core.patch.ApplyOptions;
import io.elev8.core.patch.PatchOptions;

/**
 * Abstract base class for managing read-only cluster-scoped Kubernetes resources.
 * Read-only resources (like NodeMetrics from the Metrics API) do not support
 * create, update, delete, patch, or apply operations.
 *
 * <p>All write operations will throw a {@link ResourceException} with HTTP status 405
 * (Method Not Allowed).</p>
 *
 * @param <T> the type of Kubernetes resource
 */
public abstract class AbstractReadOnlyClusterResourceManager<T extends KubernetesResource>
        extends AbstractClusterResourceManager<T> {

    private static final int METHOD_NOT_ALLOWED = 405;
    private static final String READ_ONLY_MESSAGE = "This resource is read-only and does not support %s operations";

    protected AbstractReadOnlyClusterResourceManager(final KubernetesClient client,
                                                     final Class<T> resourceClass,
                                                     final String apiPath) {
        super(client, resourceClass, apiPath);
    }

    @Override
    public T create(final T resource) throws ResourceException {
        throw new ResourceException(
                String.format(READ_ONLY_MESSAGE, "create"),
                METHOD_NOT_ALLOWED);
    }

    @Override
    public T update(final T resource) throws ResourceException {
        throw new ResourceException(
                String.format(READ_ONLY_MESSAGE, "update"),
                METHOD_NOT_ALLOWED);
    }

    @Override
    public void delete(final String name) throws ResourceException {
        throw new ResourceException(
                String.format(READ_ONLY_MESSAGE, "delete"),
                METHOD_NOT_ALLOWED);
    }

    @Override
    public T patch(final String name, final PatchOptions options, final String patchBody)
            throws ResourceException {
        throw new ResourceException(
                String.format(READ_ONLY_MESSAGE, "patch"),
                METHOD_NOT_ALLOWED);
    }

    @Override
    public T apply(final String name, final ApplyOptions options, final String manifest)
            throws ResourceException {
        throw new ResourceException(
                String.format(READ_ONLY_MESSAGE, "apply"),
                METHOD_NOT_ALLOWED);
    }
}
