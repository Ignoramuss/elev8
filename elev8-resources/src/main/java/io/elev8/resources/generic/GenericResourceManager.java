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
import io.elev8.resources.ResourceException;
import io.elev8.resources.ResourceList;
import io.elev8.resources.ResourceManager;
import io.elev8.resources.WatchStreamAdapter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Manager for namespace-scoped custom resources using dynamic typing.
 * Enables CRUD operations on custom resources without compile-time type information.
 */
@Slf4j
public final class GenericResourceManager implements ResourceManager<GenericKubernetesResource> {

    private final KubernetesClient client;

    @Getter
    private final GenericResourceContext context;

    /**
     * Create a new GenericResourceManager.
     *
     * @param client the Kubernetes client
     * @param context the resource context
     */
    public GenericResourceManager(final KubernetesClient client, final GenericResourceContext context) {
        if (context.isClusterScoped()) {
            throw new IllegalArgumentException(
                    "GenericResourceManager is for namespace-scoped resources. " +
                    "Use GenericClusterResourceManager for cluster-scoped resources.");
        }
        this.client = client;
        this.context = context;
    }

    /**
     * Create a new GenericResourceManager with inline context parameters.
     *
     * @param client the Kubernetes client
     * @param group the API group
     * @param version the API version
     * @param kind the resource kind
     * @param plural the plural name
     */
    public GenericResourceManager(final KubernetesClient client,
                                  final String group,
                                  final String version,
                                  final String kind,
                                  final String plural) {
        this(client, GenericResourceContext.forNamespacedResource(group, version, kind, plural));
    }

    @Override
    public List<GenericKubernetesResource> list(final String namespace) throws ResourceException {
        try {
            final String path = buildNamespacePath(namespace);
            log.debug("Listing generic resources at path: {}", path);

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
    public List<GenericKubernetesResource> listAllNamespaces() throws ResourceException {
        try {
            final String path = context.getApiPath() + "/" + context.getPlural();
            log.debug("Listing generic resources across all namespaces at path: {}", path);

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
    public GenericKubernetesResource get(final String namespace, final String name) throws ResourceException {
        try {
            final String path = buildResourcePath(namespace, name);
            log.debug("Getting generic resource at path: {}", path);

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
            final String namespace = resource.getNamespace();
            if (namespace == null) {
                throw new ResourceException("Resource namespace is required for creation");
            }

            final String path = buildNamespacePath(namespace);
            final String body = resource.toJson();
            log.debug("Creating generic resource at path: {}", path);

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
            final String namespace = resource.getNamespace();
            final String name = resource.getName();

            if (namespace == null || name == null) {
                throw new ResourceException("Resource namespace and name are required for update");
            }

            final String path = buildResourcePath(namespace, name);
            final String body = resource.toJson();
            log.debug("Updating generic resource at path: {}", path);

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
    public void delete(final String namespace, final String name) throws ResourceException {
        try {
            final String path = buildResourcePath(namespace, name);
            log.debug("Deleting generic resource at path: {}", path);

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
    public GenericKubernetesResource patch(final String namespace,
                                           final String name,
                                           final PatchOptions options,
                                           final String patchBody) throws ResourceException {
        try {
            final String path = buildResourcePath(namespace, name);
            log.debug("Patching generic resource at path: {} with patch type: {}",
                    path, options != null ? options.getPatchType() : "default");

            final HttpResponse response = client.patch(path, options, patchBody);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to patch resource: " + response.getBody(),
                        response.getStatusCode());
            }

            return AbstractResource.fromJson(response.getBody(), GenericKubernetesResource.class);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to patch resource", e);
        }
    }

    @Override
    public GenericKubernetesResource apply(final String namespace,
                                           final String name,
                                           final ApplyOptions options,
                                           final String manifest) throws ResourceException {
        if (options == null) {
            throw new ResourceException("ApplyOptions are required for Server-side Apply");
        }

        options.validate();

        try {
            final String path = buildResourcePath(namespace, name);
            log.debug("Applying generic resource at path: {} with field manager: {}",
                    path, options.getFieldManager());

            final PatchOptions patchOptions = options.toPatchOptions();
            final HttpResponse response = client.patch(path, patchOptions, manifest);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to apply resource: " + response.getBody(),
                        response.getStatusCode());
            }

            return AbstractResource.fromJson(response.getBody(), GenericKubernetesResource.class);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to apply resource", e);
        }
    }

    @Override
    public String getApiPath() {
        return context.getApiPath();
    }

    @Override
    public void watch(final String namespace,
                      final WatchOptions options,
                      final Watcher<GenericKubernetesResource> watcher) throws ResourceException {
        try {
            final String path = buildNamespacePath(namespace);
            log.debug("Watching generic resources at path: {}", path);

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
            throw new ResourceException("Failed to watch resources", e);
        }
    }

    @Override
    public void watchAllNamespaces(final WatchOptions options,
                                   final Watcher<GenericKubernetesResource> watcher) throws ResourceException {
        try {
            final String path = context.getApiPath() + "/" + context.getPlural();
            log.debug("Watching generic resources across all namespaces at path: {}", path);

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
            throw new ResourceException("Failed to watch resources", e);
        }
    }

    @Override
    public ResourceChangeStream<GenericKubernetesResource> stream(final String namespace,
                                                                  final StreamOptions options)
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

        log.debug("Starting generic resource change stream for namespace: {}", namespace);
        watch(namespace, effectiveOptions.getWatchOptions(), adapter);

        return stream;
    }

    @Override
    public ResourceChangeStream<GenericKubernetesResource> streamAllNamespaces(final StreamOptions options)
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

        log.debug("Starting generic resource change stream for all namespaces");
        watchAllNamespaces(effectiveOptions.getWatchOptions(), adapter);

        return stream;
    }

    private String buildNamespacePath(final String namespace) {
        return context.getApiPath() + "/namespaces/" + namespace + "/" + context.getPlural();
    }

    private String buildResourcePath(final String namespace, final String name) {
        return buildNamespacePath(namespace) + "/" + name;
    }
}
