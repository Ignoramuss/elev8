package io.elev8.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientException;
import io.elev8.core.http.HttpClient;
import io.elev8.core.http.HttpResponse;
import io.elev8.core.patch.ApplyOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.watch.WatchEvent;
import io.elev8.core.watch.WatchOptions;
import io.elev8.core.watch.Watcher;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class AbstractResourceManager<T extends KubernetesResource> implements ResourceManager<T> {

    protected final KubernetesClient client;
    protected final Class<T> resourceClass;
    protected final String apiPath;

    protected AbstractResourceManager(final KubernetesClient client, final Class<T> resourceClass, final String apiPath) {
        this.client = client;
        this.resourceClass = resourceClass;
        this.apiPath = apiPath;
    }

    @Override
    public List<T> list(String namespace) throws ResourceException {
        try {
            final String path = buildNamespacePath(namespace);
            log.debug("Listing resources at path: {}", path);

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
    public List<T> listAllNamespaces() throws ResourceException {
        try {
            final String path = apiPath;
            log.debug("Listing resources across all namespaces at path: {}", path);

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
    public T get(String namespace, String name) throws ResourceException {
        try {
            final String path = buildResourcePath(namespace, name);
            log.debug("Getting resource at path: {}", path);

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
    public T create(T resource) throws ResourceException {
        try {
            final String namespace = resource.getNamespace();
            if (namespace == null) {
                throw new ResourceException("Resource namespace is required for creation");
            }

            final String path = buildNamespacePath(namespace);
            final String body = resource.toJson();
            log.debug("Creating resource at path: {}", path);

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
    public T update(T resource) throws ResourceException {
        try {
            final String namespace = resource.getNamespace();
            final String name = resource.getName();

            if (namespace == null || name == null) {
                throw new ResourceException("Resource namespace and name are required for update");
            }

            final String path = buildResourcePath(namespace, name);
            final String body = resource.toJson();
            log.debug("Updating resource at path: {}", path);

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
    public void delete(String namespace, String name) throws ResourceException {
        try {
            final String path = buildResourcePath(namespace, name);
            log.debug("Deleting resource at path: {}", path);

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
    public T patch(final String namespace, final String name, final PatchOptions options, final String patchBody)
            throws ResourceException {
        try {
            final String path = buildResourcePath(namespace, name);
            log.debug("Patching resource at path: {} with patch type: {}",
                    path, options != null ? options.getPatchType() : "default");

            final HttpResponse response = client.patch(path, options, patchBody);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to patch resource: " + response.getBody(),
                        response.getStatusCode());
            }

            return AbstractResource.fromJson(response.getBody(), resourceClass);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to patch resource", e);
        }
    }

    @Override
    public T apply(final String namespace, final String name, final ApplyOptions options, final String manifest)
            throws ResourceException {
        if (options == null) {
            throw new ResourceException("ApplyOptions are required for Server-side Apply");
        }

        options.validate();

        try {
            final String path = buildResourcePath(namespace, name);
            log.debug("Applying resource at path: {} with field manager: {}",
                    path, options.getFieldManager());

            final PatchOptions patchOptions = options.toPatchOptions();
            final HttpResponse response = client.patch(path, patchOptions, manifest);

            if (!response.isSuccessful()) {
                throw new ResourceException(
                        "Failed to apply resource: " + response.getBody(),
                        response.getStatusCode());
            }

            return AbstractResource.fromJson(response.getBody(), resourceClass);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to apply resource", e);
        }
    }

    @Override
    public String getApiPath() {
        return apiPath;
    }

    @Override
    public void watch(final String namespace, final WatchOptions options, final Watcher<T> watcher)
            throws ResourceException {
        try {
            final String path = buildNamespacePath(namespace);
            log.debug("Watching resources at path: {}", path);

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
            throw new ResourceException("Failed to watch resources", e);
        }
    }

    @Override
    public void watchAllNamespaces(final WatchOptions options, final Watcher<T> watcher)
            throws ResourceException {
        try {
            final String path = apiPath;
            log.debug("Watching resources across all namespaces at path: {}", path);

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
            throw new ResourceException("Failed to watch resources", e);
        }
    }

    protected String buildNamespacePath(final String namespace) {
        return apiPath + "/namespaces/" + namespace + "/" + getResourceTypePlural();
    }

    protected String buildResourcePath(final String namespace, final String name) {
        return buildNamespacePath(namespace) + "/" + name;
    }

    protected abstract String getResourceTypePlural();
}
