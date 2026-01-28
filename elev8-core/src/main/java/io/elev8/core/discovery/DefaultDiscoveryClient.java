package io.elev8.core.discovery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientException;
import io.elev8.core.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public final class DefaultDiscoveryClient implements DiscoveryClient {

    private final KubernetesClient client;
    private final ObjectMapper objectMapper;

    public DefaultDiscoveryClient(final KubernetesClient client) {
        this.client = client;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public APIGroupList getServerGroups() throws DiscoveryException {
        log.debug("Fetching API groups from /apis");
        try {
            final HttpResponse response = client.get("/apis");
            if (!response.isSuccessful()) {
                throw new DiscoveryException("Failed to fetch API groups: " + response.getStatusCode(),
                        response.getStatusCode());
            }
            return objectMapper.readValue(response.getBody(), APIGroupList.class);
        } catch (KubernetesClientException e) {
            throw new DiscoveryException("Failed to fetch API groups", e);
        } catch (JsonProcessingException e) {
            throw new DiscoveryException("Failed to parse API groups response", e);
        }
    }

    @Override
    public APIResourceList getServerResources(final String groupVersion) throws DiscoveryException {
        final String path = buildResourcesPath(groupVersion);
        log.debug("Fetching API resources from {}", path);
        try {
            final HttpResponse response = client.get(path);
            if (!response.isSuccessful()) {
                throw new DiscoveryException("Failed to fetch API resources for " + groupVersion + ": "
                        + response.getStatusCode(), response.getStatusCode());
            }
            final APIResourceList resourceList = objectMapper.readValue(response.getBody(), APIResourceList.class);
            return enrichResourceList(resourceList, groupVersion);
        } catch (KubernetesClientException e) {
            throw new DiscoveryException("Failed to fetch API resources for " + groupVersion, e);
        } catch (JsonProcessingException e) {
            throw new DiscoveryException("Failed to parse API resources response for " + groupVersion, e);
        }
    }

    @Override
    public List<APIResource> getPreferredResources() throws DiscoveryException {
        final List<APIResource> allResources = new ArrayList<>();

        final List<String> coreVersions = getCoreAPIVersions();
        for (final String version : coreVersions) {
            final APIResourceList resourceList = getServerResources(version);
            if (resourceList.getResources() != null) {
                allResources.addAll(resourceList.getResources());
            }
        }

        final APIGroupList groups = getServerGroups();
        for (final APIGroup group : groups.getGroups()) {
            final String preferredGV = group.getPreferredGroupVersion();
            if (preferredGV != null) {
                try {
                    final APIResourceList resourceList = getServerResources(preferredGV);
                    if (resourceList.getResources() != null) {
                        allResources.addAll(resourceList.getResources());
                    }
                } catch (DiscoveryException e) {
                    log.warn("Failed to fetch resources for {}: {}", preferredGV, e.getMessage());
                }
            }
        }

        return allResources;
    }

    @Override
    public Optional<APIResource> findResource(final String kind) throws DiscoveryException {
        final List<String> coreVersions = getCoreAPIVersions();
        for (final String version : coreVersions) {
            final APIResourceList resourceList = getServerResources(version);
            final Optional<APIResource> found = resourceList.findByKind(kind);
            if (found.isPresent()) {
                return found;
            }
        }

        final APIGroupList groups = getServerGroups();
        for (final APIGroup group : groups.getGroups()) {
            final String preferredGV = group.getPreferredGroupVersion();
            if (preferredGV != null) {
                try {
                    final APIResourceList resourceList = getServerResources(preferredGV);
                    final Optional<APIResource> found = resourceList.findByKind(kind);
                    if (found.isPresent()) {
                        return found;
                    }
                } catch (DiscoveryException e) {
                    log.debug("Skipping group {} due to error: {}", preferredGV, e.getMessage());
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<APIResource> findResource(final String group, final String version, final String kind)
            throws DiscoveryException {
        final String groupVersion = group == null || group.isEmpty() ? version : group + "/" + version;
        final APIResourceList resourceList = getServerResources(groupVersion);
        return resourceList.findByKind(kind);
    }

    @Override
    public boolean isResourceAvailable(final String group, final String version, final String kind)
            throws DiscoveryException {
        return findResource(group, version, kind).isPresent();
    }

    @Override
    public List<String> getCoreAPIVersions() throws DiscoveryException {
        log.debug("Fetching core API versions from /api");
        try {
            final HttpResponse response = client.get("/api");
            if (!response.isSuccessful()) {
                throw new DiscoveryException("Failed to fetch core API versions: " + response.getStatusCode(),
                        response.getStatusCode());
            }

            final JsonNode root = objectMapper.readTree(response.getBody());
            final JsonNode versions = root.get("versions");
            if (versions != null && versions.isArray()) {
                return objectMapper.convertValue(versions, new TypeReference<List<String>>() {});
            }
            return List.of();
        } catch (KubernetesClientException e) {
            throw new DiscoveryException("Failed to fetch core API versions", e);
        } catch (JsonProcessingException e) {
            throw new DiscoveryException("Failed to parse core API versions response", e);
        }
    }

    @Override
    public void invalidateCache() {
    }

    private String buildResourcesPath(final String groupVersion) {
        if (groupVersion == null || !groupVersion.contains("/")) {
            return "/api/" + groupVersion;
        }
        return "/apis/" + groupVersion;
    }

    private APIResourceList enrichResourceList(final APIResourceList resourceList, final String groupVersion) {
        if (resourceList.getResources() == null) {
            return resourceList;
        }

        final String group;
        final String version;
        if (groupVersion.contains("/")) {
            final int slashIndex = groupVersion.indexOf('/');
            group = groupVersion.substring(0, slashIndex);
            version = groupVersion.substring(slashIndex + 1);
        } else {
            group = "";
            version = groupVersion;
        }

        final List<APIResource> enrichedResources = new ArrayList<>();
        for (final APIResource resource : resourceList.getResources()) {
            final APIResource enriched = resource.toBuilder()
                    .group(group)
                    .version(version)
                    .build();
            enrichedResources.add(enriched);
        }

        return APIResourceList.builder()
                .apiVersion(resourceList.getApiVersion())
                .kind(resourceList.getKind())
                .groupVersion(resourceList.getGroupVersion())
                .resources(enrichedResources)
                .build();
    }
}
