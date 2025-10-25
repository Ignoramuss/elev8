package io.elev8.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientException;
import io.elev8.core.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractResourceManager<T extends KubernetesResource> implements ResourceManager<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractResourceManager.class);

    protected final KubernetesClient client;
    protected final Class<T> resourceClass;
    protected final String apiPath;

    protected AbstractResourceManager(KubernetesClient client, Class<T> resourceClass, String apiPath) {
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
    public String getApiPath() {
        return apiPath;
    }

    protected String buildNamespacePath(String namespace) {
        return apiPath + "/namespaces/" + namespace + "/" + getResourceTypePlural();
    }

    protected String buildResourcePath(String namespace, String name) {
        return buildNamespacePath(namespace) + "/" + name;
    }

    protected abstract String getResourceTypePlural();
}
