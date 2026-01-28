package io.elev8.resources.dynamic;

import io.elev8.core.client.KubernetesClient;
import io.elev8.core.discovery.APIResource;
import io.elev8.core.discovery.CachedDiscoveryClient;
import io.elev8.core.discovery.DefaultDiscoveryClient;
import io.elev8.core.discovery.DiscoveryClient;
import io.elev8.core.discovery.DiscoveryException;
import io.elev8.resources.ResourceException;
import io.elev8.resources.generic.GenericClusterResourceManager;
import io.elev8.resources.generic.GenericKubernetesResource;
import io.elev8.resources.generic.GenericResourceContext;
import io.elev8.resources.generic.GenericResourceManager;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class DefaultDynamicClient implements DynamicClient {

    private final KubernetesClient kubernetesClient;
    private final DiscoveryClient discoveryClient;
    private final Map<String, GenericResourceManager> namespacedManagerCache = new ConcurrentHashMap<>();
    private final Map<String, GenericClusterResourceManager> clusterManagerCache = new ConcurrentHashMap<>();

    public DefaultDynamicClient(final KubernetesClient kubernetesClient) {
        this(kubernetesClient, Duration.ofMinutes(10));
    }

    public DefaultDynamicClient(final KubernetesClient kubernetesClient, final Duration cacheTtl) {
        this.kubernetesClient = kubernetesClient;
        this.discoveryClient = new CachedDiscoveryClient(
                new DefaultDiscoveryClient(kubernetesClient), cacheTtl);
    }

    public DefaultDynamicClient(final KubernetesClient kubernetesClient, final DiscoveryClient discoveryClient) {
        this.kubernetesClient = kubernetesClient;
        this.discoveryClient = discoveryClient;
    }

    @Override
    public DiscoveryClient discovery() {
        return discoveryClient;
    }

    @Override
    public GenericResourceManager resources(final String group, final String version, final String plural) {
        final String cacheKey = buildCacheKey(group, version, plural);
        return namespacedManagerCache.computeIfAbsent(cacheKey, k -> {
            final GenericResourceContext context = GenericResourceContext.forNamespacedResource(
                    group, version, null, plural);
            return new GenericResourceManager(kubernetesClient, context);
        });
    }

    @Override
    public GenericClusterResourceManager clusterResources(final String group, final String version, final String plural) {
        final String cacheKey = buildCacheKey(group, version, plural);
        return clusterManagerCache.computeIfAbsent(cacheKey, k -> {
            final GenericResourceContext context = GenericResourceContext.forClusterResource(
                    group, version, null, plural);
            return new GenericClusterResourceManager(kubernetesClient, context);
        });
    }

    @Override
    public GenericResourceManager resourcesForKind(final String kind) throws DiscoveryException {
        final APIResource resource = findResourceOrThrow(kind);
        if (!resource.isNamespaced()) {
            throw new DiscoveryException("Resource " + kind + " is cluster-scoped, use clusterResourcesForKind()");
        }
        return resources(resource.getGroup(), resource.getVersion(), resource.getName());
    }

    @Override
    public GenericClusterResourceManager clusterResourcesForKind(final String kind) throws DiscoveryException {
        final APIResource resource = findResourceOrThrow(kind);
        if (resource.isNamespaced()) {
            throw new DiscoveryException("Resource " + kind + " is namespace-scoped, use resourcesForKind()");
        }
        return clusterResources(resource.getGroup(), resource.getVersion(), resource.getName());
    }

    @Override
    public GenericKubernetesResource get(final String kind, final String namespace, final String name)
            throws DiscoveryException, ResourceException {
        return resourcesForKind(kind).get(namespace, name);
    }

    @Override
    public GenericKubernetesResource getClusterScoped(final String kind, final String name)
            throws DiscoveryException, ResourceException {
        return clusterResourcesForKind(kind).get(name);
    }

    @Override
    public List<GenericKubernetesResource> list(final String kind, final String namespace)
            throws DiscoveryException, ResourceException {
        return resourcesForKind(kind).list(namespace);
    }

    @Override
    public List<GenericKubernetesResource> listClusterScoped(final String kind)
            throws DiscoveryException, ResourceException {
        return clusterResourcesForKind(kind).list();
    }

    @Override
    public GenericKubernetesResource create(final String kind, final String namespace,
                                            final GenericKubernetesResource resource)
            throws DiscoveryException, ResourceException {
        return resourcesForKind(kind).create(resource);
    }

    @Override
    public GenericKubernetesResource createClusterScoped(final String kind,
                                                         final GenericKubernetesResource resource)
            throws DiscoveryException, ResourceException {
        return clusterResourcesForKind(kind).create(resource);
    }

    @Override
    public GenericKubernetesResource update(final String kind, final String namespace,
                                            final GenericKubernetesResource resource)
            throws DiscoveryException, ResourceException {
        return resourcesForKind(kind).update(resource);
    }

    @Override
    public GenericKubernetesResource updateClusterScoped(final String kind,
                                                         final GenericKubernetesResource resource)
            throws DiscoveryException, ResourceException {
        return clusterResourcesForKind(kind).update(resource);
    }

    @Override
    public void delete(final String kind, final String namespace, final String name)
            throws DiscoveryException, ResourceException {
        resourcesForKind(kind).delete(namespace, name);
    }

    @Override
    public void deleteClusterScoped(final String kind, final String name)
            throws DiscoveryException, ResourceException {
        clusterResourcesForKind(kind).delete(name);
    }

    private APIResource findResourceOrThrow(final String kind) throws DiscoveryException {
        final Optional<APIResource> resource = discoveryClient.findResource(kind);
        if (resource.isEmpty()) {
            throw new DiscoveryException("Resource kind not found: " + kind);
        }
        return resource.get();
    }

    private String buildCacheKey(final String group, final String version, final String plural) {
        if (group == null || group.isEmpty()) {
            return version + "/" + plural;
        }
        return group + "/" + version + "/" + plural;
    }
}
