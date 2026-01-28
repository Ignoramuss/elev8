package io.elev8.core.discovery;

import java.util.List;
import java.util.Optional;

public interface DiscoveryClient {

    APIGroupList getServerGroups() throws DiscoveryException;

    APIResourceList getServerResources(String groupVersion) throws DiscoveryException;

    List<APIResource> getPreferredResources() throws DiscoveryException;

    Optional<APIResource> findResource(String kind) throws DiscoveryException;

    Optional<APIResource> findResource(String group, String version, String kind) throws DiscoveryException;

    boolean isResourceAvailable(String group, String version, String kind) throws DiscoveryException;

    List<String> getCoreAPIVersions() throws DiscoveryException;

    void invalidateCache();
}
