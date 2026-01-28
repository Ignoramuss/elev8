package io.elev8.resources.dynamic;

import io.elev8.core.discovery.DiscoveryClient;
import io.elev8.core.discovery.DiscoveryException;
import io.elev8.resources.ResourceException;
import io.elev8.resources.generic.GenericClusterResourceManager;
import io.elev8.resources.generic.GenericKubernetesResource;
import io.elev8.resources.generic.GenericResourceManager;

import java.util.List;

public interface DynamicClient {

    DiscoveryClient discovery();

    GenericResourceManager resources(String group, String version, String plural);

    GenericClusterResourceManager clusterResources(String group, String version, String plural);

    GenericResourceManager resourcesForKind(String kind) throws DiscoveryException;

    GenericClusterResourceManager clusterResourcesForKind(String kind) throws DiscoveryException;

    GenericKubernetesResource get(String kind, String namespace, String name)
            throws DiscoveryException, ResourceException;

    GenericKubernetesResource getClusterScoped(String kind, String name)
            throws DiscoveryException, ResourceException;

    List<GenericKubernetesResource> list(String kind, String namespace)
            throws DiscoveryException, ResourceException;

    List<GenericKubernetesResource> listClusterScoped(String kind)
            throws DiscoveryException, ResourceException;

    GenericKubernetesResource create(String kind, String namespace, GenericKubernetesResource resource)
            throws DiscoveryException, ResourceException;

    GenericKubernetesResource createClusterScoped(String kind, GenericKubernetesResource resource)
            throws DiscoveryException, ResourceException;

    GenericKubernetesResource update(String kind, String namespace, GenericKubernetesResource resource)
            throws DiscoveryException, ResourceException;

    GenericKubernetesResource updateClusterScoped(String kind, GenericKubernetesResource resource)
            throws DiscoveryException, ResourceException;

    void delete(String kind, String namespace, String name)
            throws DiscoveryException, ResourceException;

    void deleteClusterScoped(String kind, String name)
            throws DiscoveryException, ResourceException;
}
