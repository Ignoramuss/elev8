package io.elev8.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientException;
import io.elev8.core.http.HttpClient;
import io.elev8.core.http.HttpResponse;
import io.elev8.core.list.ListOptions;
import io.elev8.core.patch.ApplyOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.watch.ResourceChangeStream;
import io.elev8.core.watch.StreamOptions;
import io.elev8.core.watch.WatchEvent;
import io.elev8.core.watch.WatchOptions;
import io.elev8.core.watch.Watcher;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Abstract base class for managing cluster-scoped Kubernetes resources.
 * Cluster-scoped resources (like PersistentVolume, Namespace, ClusterRole, etc.)
 * are not confined to a specific namespace and exist at the cluster level.
 */
@Slf4j
public abstract class AbstractClusterResourceManager<T extends KubernetesResource>
        implements ClusterResourceManager<T> {

    protected final KubernetesClient client;
    protected final Class<T> resourceClass;
    protected final String apiPath;

    protected AbstractClusterResourceManager(final KubernetesClient client,
                                            final Class<T> resourceClass,
                                            final String apiPath) {
        this.client = client;
        this.resourceClass = resourceClass;
        this.apiPath = apiPath;
    }

    @Override
    public List<T> list() throws ResourceException {
        try {
            final String path = buildClusterPath();
            log.debug("Listing cluster resources at path: {}", path);

            final HttpResponse response = client.get(path);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to list resources: " + response.getBody(),
                        response.getStatusCode());
            }

            final ResourceList<T> resourceList = AbstractResource.getObjectMapper().readValue(
                    response.getBody(),
                    new TypeReference<ResourceList<T>>() {});

            return resourceList.getItems();

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to list resources", e);
        } catch (Exception e) {
            throw new ResourceException("Failed to parse resource list", e);
        }
    }

    @Override
    public List<T> list(final ListOptions options) throws ResourceException {
        try {
            final String path = buildClusterPath();
            log.debug("Listing cluster resources at path: {} with options", path);

            final HttpResponse response = client.get(path, options);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to list resources: " + response.getBody(),
                        response.getStatusCode());
            }

            final ResourceList<T> resourceList = AbstractResource.getObjectMapper().readValue(
                    response.getBody(),
                    new TypeReference<ResourceList<T>>() {});

            return resourceList.getItems();

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to list resources", e);
        } catch (Exception e) {
            throw new ResourceException("Failed to parse resource list", e);
        }
    }

    @Override
    public T get(final String name) throws ResourceException {
        try {
            final String path = buildResourcePath(name);
            log.debug("Getting cluster resource at path: {}", path);

            final HttpResponse response = client.get(path);

            if (response.isNotFound()) {
                throw new ResourceException("Resource not found: " + name, 404);
            }

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to get resource: " + response.getBody(),
                        response.getStatusCode());
            }

            return AbstractResource.fromJson(response.getBody(), resourceClass);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to get resource", e);
        }
    }

    @Override
    public T create(final T resource) throws ResourceException {
        try {
            final String path = buildClusterPath();
            final String body = resource.toJson();
            log.debug("Creating cluster resource at path: {}", path);

            final HttpResponse response = client.post(path, body);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to create resource: " + response.getBody(),
                        response.getStatusCode());
            }

            return AbstractResource.fromJson(response.getBody(), resourceClass);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to create resource", e);
        }
    }

    @Override
    public T update(final T resource) throws ResourceException {
        try {
            final String name = resource.getName();

            if (name == null) {
                throw new ResourceException("Resource name is required for update");
            }

            final String path = buildResourcePath(name);
            final String body = resource.toJson();
            log.debug("Updating cluster resource at path: {}", path);

            final HttpResponse response = client.put(path, body);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to update resource: " + response.getBody(),
                        response.getStatusCode());
            }

            return AbstractResource.fromJson(response.getBody(), resourceClass);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to update resource", e);
        }
    }

    @Override
    public void delete(final String name) throws ResourceException {
        try {
            final String path = buildResourcePath(name);
            log.debug("Deleting cluster resource at path: {}", path);

            final HttpResponse response = client.delete(path);

            if (!response.isSuccessful() && !response.isNotFound()) {
                throw new ResourceException(
                        "Failed to delete resource: " + response.getBody(),
                        response.getStatusCode());
            }

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to delete resource", e);
        }
    }

    @Override
    public T patch(final String name, final PatchOptions options, final String patchBody)
            throws ResourceException {
        try {
            final String path = buildResourcePath(name);
            log.debug("Patching cluster resource at path: {} with patch type: {}",
                    path, options != null ? options.getPatchType() : "default");

            final HttpResponse response = client.patch(path, options, patchBody);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to patch resource: " + response.getBody(),
                        response.getStatusCode());
            }

            return AbstractResource.fromJson(response.getBody(), resourceClass);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to patch cluster resource", e);
        }
    }

    @Override
    public T apply(final String name, final ApplyOptions options, final String manifest)
            throws ResourceException {
        if (options == null) {
            throw new ResourceException("ApplyOptions are required for Server-side Apply");
        }

        options.validate();

        try {
            final String path = buildResourcePath(name);
            log.debug("Applying cluster resource at path: {} with field manager: {}",
                    path, options.getFieldManager());

            final PatchOptions patchOptions = options.toPatchOptions();
            final HttpResponse response = client.patch(path, patchOptions, manifest);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to apply cluster resource: " + response.getBody(),
                        response.getStatusCode());
            }

            return AbstractResource.fromJson(response.getBody(), resourceClass);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to apply cluster resource", e);
        }
    }

    @Override
    public String getApiPath() {
        return apiPath;
    }

    @Override
    public void watch(final WatchOptions options, final Watcher<T> watcher) throws ResourceException {
        try {
            final String path = buildClusterPath();
            log.debug("Watching cluster resources at path: {}", path);

            final HttpClient.StreamHandler handler = new HttpClient.StreamHandler() {
                @Override
                public void onLine(final String line) {
                    try {
                        final WatchEvent<T> event = AbstractResource.getObjectMapper().readValue(
                                line,
                                AbstractResource.getObjectMapper().getTypeFactory()
                                        .constructParametricType(WatchEvent.class, resourceClass));
                        watcher.onEvent(event);
                    } catch (Exception e) {
                        log.error("Failed to parse watch event", e);
                        watcher.onError(e);
                    }
                }

                @Override
                public void onError(final Exception exception) {
                    watcher.onError(exception);
                }

                @Override
                public void onClose() {
                    watcher.onClose();
                }
            };

            client.watch(path, options, handler);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to watch cluster resources", e);
        }
    }

    @Override
    public ResourceChangeStream<T> stream(final StreamOptions options) throws ResourceException {
        final StreamOptions effectiveOptions = options != null ? options : StreamOptions.defaults();
        final ResourceChangeStream<T> stream = new ResourceChangeStream<>(
                effectiveOptions.getQueueCapacity(),
                () -> {}
        );

        final WatchStreamAdapter<T> adapter = new WatchStreamAdapter<>(
                stream,
                effectiveOptions.isTrackPreviousState()
        );

        log.debug("Starting resource change stream for cluster-scoped resources");
        watch(effectiveOptions.getWatchOptions(), adapter);

        return stream;
    }

    /**
     * Build the path for cluster-scoped resource operations.
     * Example: /api/v1/persistentvolumes
     *
     * @return the cluster path
     */
    protected String buildClusterPath() {
        return apiPath + "/" + getResourceTypePlural();
    }

    /**
     * Build the path for a specific cluster-scoped resource.
     * Example: /api/v1/persistentvolumes/my-pv
     *
     * @param name the resource name
     * @return the resource path
     */
    protected String buildResourcePath(final String name) {
        return buildClusterPath() + "/" + name;
    }

    /**
     * Get the plural form of the resource type.
     * Example: "persistentvolumes", "namespaces", "clusterroles"
     *
     * @return the plural resource type
     */
    protected abstract String getResourceTypePlural();
}
