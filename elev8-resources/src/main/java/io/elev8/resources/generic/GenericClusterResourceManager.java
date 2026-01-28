package io.elev8.resources.generic;

import com.fasterxml.jackson.core.type.TypeReference;
import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientException;
import io.elev8.core.http.HttpClient;
import io.elev8.core.http.HttpResponse;
import io.elev8.core.patch.ApplyOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.watch.ResourceChangeStream;
import io.elev8.core.watch.StreamOptions;
import io.elev8.core.watch.WatchEvent;
import io.elev8.core.watch.WatchOptions;
import io.elev8.core.watch.Watcher;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.ClusterResourceManager;
import io.elev8.resources.ResourceException;
import io.elev8.resources.ResourceList;
import io.elev8.resources.WatchStreamAdapter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Manager for cluster-scoped custom resources using dynamic typing.
 * Enables CRUD operations on cluster-scoped custom resources without compile-time type information.
 */
@Slf4j
public final class GenericClusterResourceManager implements ClusterResourceManager<GenericKubernetesResource> {

    private final KubernetesClient client;

    @Getter
    private final GenericResourceContext context;

    /**
     * Create a new GenericClusterResourceManager.
     *
     * @param client the Kubernetes client
     * @param context the resource context
     */
    public GenericClusterResourceManager(final KubernetesClient client, final GenericResourceContext context) {
        if (context.isNamespaced()) {
            throw new IllegalArgumentException(
                    "GenericClusterResourceManager is for cluster-scoped resources. " +
                    "Use GenericResourceManager for namespace-scoped resources.");
        }
        this.client = client;
        this.context = context;
    }

    /**
     * Create a new GenericClusterResourceManager with inline context parameters.
     *
     * @param client the Kubernetes client
     * @param group the API group
     * @param version the API version
     * @param kind the resource kind
     * @param plural the plural name
     */
    public GenericClusterResourceManager(final KubernetesClient client,
                                         final String group,
                                         final String version,
                                         final String kind,
                                         final String plural) {
        this(client, GenericResourceContext.forClusterResource(group, version, kind, plural));
    }

    @Override
    public List<GenericKubernetesResource> list() throws ResourceException {
        try {
            final String path = buildClusterPath();
            log.debug("Listing generic cluster resources at path: {}", path);

            final HttpResponse response = client.get(path);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to list resources: " + response.getBody(),
                        response.getStatusCode());
            }

            final ResourceList<GenericKubernetesResource> resourceList =
                    AbstractResource.getObjectMapper().readValue(
                            response.getBody(),
                            new TypeReference<ResourceList<GenericKubernetesResource>>() {});

            return resourceList.getItems();

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to list resources", e);
        } catch (Exception e) {
            throw new ResourceException("Failed to parse resource list", e);
        }
    }

    @Override
    public GenericKubernetesResource get(final String name) throws ResourceException {
        try {
            final String path = buildResourcePath(name);
            log.debug("Getting generic cluster resource at path: {}", path);

            final HttpResponse response = client.get(path);

            if (response.isNotFound()) {
                throw new ResourceException("Resource not found: " + name, 404);
            }

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to get resource: " + response.getBody(),
                        response.getStatusCode());
            }

            return AbstractResource.fromJson(response.getBody(), GenericKubernetesResource.class);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to get resource", e);
        }
    }

    @Override
    public GenericKubernetesResource create(final GenericKubernetesResource resource) throws ResourceException {
        try {
            final String path = buildClusterPath();
            final String body = resource.toJson();
            log.debug("Creating generic cluster resource at path: {}", path);

            final HttpResponse response = client.post(path, body);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to create resource: " + response.getBody(),
                        response.getStatusCode());
            }

            return AbstractResource.fromJson(response.getBody(), GenericKubernetesResource.class);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to create resource", e);
        }
    }

    @Override
    public GenericKubernetesResource update(final GenericKubernetesResource resource) throws ResourceException {
        try {
            final String name = resource.getName();

            if (name == null) {
                throw new ResourceException("Resource name is required for update");
            }

            final String path = buildResourcePath(name);
            final String body = resource.toJson();
            log.debug("Updating generic cluster resource at path: {}", path);

            final HttpResponse response = client.put(path, body);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to update resource: " + response.getBody(),
                        response.getStatusCode());
            }

            return AbstractResource.fromJson(response.getBody(), GenericKubernetesResource.class);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to update resource", e);
        }
    }

    @Override
    public void delete(final String name) throws ResourceException {
        try {
            final String path = buildResourcePath(name);
            log.debug("Deleting generic cluster resource at path: {}", path);

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
    public GenericKubernetesResource patch(final String name,
                                           final PatchOptions options,
                                           final String patchBody) throws ResourceException {
        try {
            final String path = buildResourcePath(name);
            log.debug("Patching generic cluster resource at path: {} with patch type: {}",
                    path, options != null ? options.getPatchType() : "default");

            final HttpResponse response = client.patch(path, options, patchBody);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to patch resource: " + response.getBody(),
                        response.getStatusCode());
            }

            return AbstractResource.fromJson(response.getBody(), GenericKubernetesResource.class);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to patch cluster resource", e);
        }
    }

    @Override
    public GenericKubernetesResource apply(final String name,
                                           final ApplyOptions options,
                                           final String manifest) throws ResourceException {
        if (options == null) {
            throw new ResourceException("ApplyOptions are required for Server-side Apply");
        }

        options.validate();

        try {
            final String path = buildResourcePath(name);
            log.debug("Applying generic cluster resource at path: {} with field manager: {}",
                    path, options.getFieldManager());

            final PatchOptions patchOptions = options.toPatchOptions();
            final HttpResponse response = client.patch(path, patchOptions, manifest);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to apply cluster resource: " + response.getBody(),
                        response.getStatusCode());
            }

            return AbstractResource.fromJson(response.getBody(), GenericKubernetesResource.class);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to apply cluster resource", e);
        }
    }

    @Override
    public String getApiPath() {
        return context.getApiPath();
    }

    @Override
    public void watch(final WatchOptions options,
                      final Watcher<GenericKubernetesResource> watcher) throws ResourceException {
        try {
            final String path = buildClusterPath();
            log.debug("Watching generic cluster resources at path: {}", path);

            final HttpClient.StreamHandler handler = new HttpClient.StreamHandler() {
                @Override
                public void onLine(final String line) {
                    try {
                        final WatchEvent<GenericKubernetesResource> event =
                                AbstractResource.getObjectMapper().readValue(
                                        line,
                                        AbstractResource.getObjectMapper().getTypeFactory()
                                                .constructParametricType(WatchEvent.class,
                                                        GenericKubernetesResource.class));
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
    public ResourceChangeStream<GenericKubernetesResource> stream(final StreamOptions options)
            throws ResourceException {
        final StreamOptions effectiveOptions = options != null ? options : StreamOptions.defaults();
        final ResourceChangeStream<GenericKubernetesResource> stream = new ResourceChangeStream<>(
                effectiveOptions.getQueueCapacity(),
                () -> {}
        );

        final WatchStreamAdapter<GenericKubernetesResource> adapter = new WatchStreamAdapter<>(
                stream,
                effectiveOptions.isTrackPreviousState()
        );

        log.debug("Starting generic cluster resource change stream");
        watch(effectiveOptions.getWatchOptions(), adapter);

        return stream;
    }

    private String buildClusterPath() {
        return context.getApiPath() + "/" + context.getPlural();
    }

    private String buildResourcePath(final String name) {
        return buildClusterPath() + "/" + name;
    }
}
