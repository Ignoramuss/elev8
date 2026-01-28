package io.elev8.core.discovery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachedDiscoveryClientTest {

    @Mock
    private DiscoveryClient delegate;

    private CachedDiscoveryClient cachedClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cachedClient = new CachedDiscoveryClient(delegate, Duration.ofMinutes(10));
    }

    @Test
    void shouldCacheServerGroups() throws Exception {
        final APIGroupList groups = APIGroupList.builder()
                .apiVersion("v1")
                .group(APIGroup.builder().name("apps").build())
                .build();
        when(delegate.getServerGroups()).thenReturn(groups);

        final APIGroupList first = cachedClient.getServerGroups();
        final APIGroupList second = cachedClient.getServerGroups();

        assertThat(first).isSameAs(second);
        verify(delegate, times(1)).getServerGroups();
    }

    @Test
    void shouldCacheServerResources() throws Exception {
        final APIResourceList resources = APIResourceList.builder()
                .groupVersion("v1")
                .resource(APIResource.builder().name("pods").kind("Pod").build())
                .build();
        when(delegate.getServerResources("v1")).thenReturn(resources);

        final APIResourceList first = cachedClient.getServerResources("v1");
        final APIResourceList second = cachedClient.getServerResources("v1");

        assertThat(first).isSameAs(second);
        verify(delegate, times(1)).getServerResources("v1");
    }

    @Test
    void shouldCacheCoreAPIVersions() throws Exception {
        when(delegate.getCoreAPIVersions()).thenReturn(List.of("v1"));

        final List<String> first = cachedClient.getCoreAPIVersions();
        final List<String> second = cachedClient.getCoreAPIVersions();

        assertThat(first).isSameAs(second);
        verify(delegate, times(1)).getCoreAPIVersions();
    }

    @Test
    void shouldInvalidateCache() throws Exception {
        final APIGroupList groups = APIGroupList.builder()
                .apiVersion("v1")
                .build();
        when(delegate.getServerGroups()).thenReturn(groups);
        when(delegate.getCoreAPIVersions()).thenReturn(List.of("v1"));

        cachedClient.getServerGroups();
        cachedClient.getCoreAPIVersions();

        cachedClient.invalidateCache();

        cachedClient.getServerGroups();
        cachedClient.getCoreAPIVersions();

        verify(delegate, times(2)).getServerGroups();
        verify(delegate, times(2)).getCoreAPIVersions();
        verify(delegate, times(1)).invalidateCache();
    }

    @Test
    void shouldInvalidateResourcesCache() throws Exception {
        final APIResourceList resources = APIResourceList.builder()
                .groupVersion("v1")
                .build();
        when(delegate.getServerResources("v1")).thenReturn(resources);

        cachedClient.getServerResources("v1");
        cachedClient.invalidateCache();
        cachedClient.getServerResources("v1");

        verify(delegate, times(2)).getServerResources("v1");
    }

    @Test
    void shouldDelegateGetPreferredResources() throws Exception {
        final List<APIResource> resources = List.of(
                APIResource.builder().name("pods").kind("Pod").build()
        );
        when(delegate.getPreferredResources()).thenReturn(resources);

        final List<APIResource> result = cachedClient.getPreferredResources();

        assertThat(result).isEqualTo(resources);
        verify(delegate).getPreferredResources();
    }

    @Test
    void shouldDelegateFindResourceByKind() throws Exception {
        final APIResource pod = APIResource.builder().name("pods").kind("Pod").build();
        when(delegate.findResource("Pod")).thenReturn(Optional.of(pod));

        final Optional<APIResource> result = cachedClient.findResource("Pod");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("pods");
        verify(delegate).findResource("Pod");
    }

    @Test
    void shouldDelegateFindResourceByGvk() throws Exception {
        final APIResource deployment = APIResource.builder()
                .name("deployments")
                .kind("Deployment")
                .build();
        when(delegate.findResource("apps", "v1", "Deployment")).thenReturn(Optional.of(deployment));

        final Optional<APIResource> result = cachedClient.findResource("apps", "v1", "Deployment");

        assertThat(result).isPresent();
        verify(delegate).findResource("apps", "v1", "Deployment");
    }

    @Test
    void shouldDelegateIsResourceAvailable() throws Exception {
        when(delegate.isResourceAvailable("apps", "v1", "Deployment")).thenReturn(true);

        final boolean result = cachedClient.isResourceAvailable("apps", "v1", "Deployment");

        assertThat(result).isTrue();
        verify(delegate).isResourceAvailable("apps", "v1", "Deployment");
    }

    @Test
    void shouldReturnConfiguredTtl() {
        final Duration ttl = Duration.ofMinutes(5);
        final CachedDiscoveryClient client = new CachedDiscoveryClient(delegate, ttl);

        assertThat(client.getCacheTtl()).isEqualTo(ttl);
    }

    @Test
    void shouldUseDefaultTtl() {
        final CachedDiscoveryClient client = new CachedDiscoveryClient(delegate);

        assertThat(client.getCacheTtl()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void shouldRefetchAfterTtlExpires() throws Exception {
        final CachedDiscoveryClient shortTtlClient = new CachedDiscoveryClient(delegate, Duration.ofMillis(1));

        final APIGroupList groups = APIGroupList.builder()
                .apiVersion("v1")
                .build();
        when(delegate.getServerGroups()).thenReturn(groups);

        shortTtlClient.getServerGroups();
        Thread.sleep(10);
        shortTtlClient.getServerGroups();

        verify(delegate, times(2)).getServerGroups();
    }

    @Test
    void shouldCacheDifferentGroupVersionsSeparately() throws Exception {
        final APIResourceList v1Resources = APIResourceList.builder()
                .groupVersion("v1")
                .build();
        final APIResourceList appsV1Resources = APIResourceList.builder()
                .groupVersion("apps/v1")
                .build();
        when(delegate.getServerResources("v1")).thenReturn(v1Resources);
        when(delegate.getServerResources("apps/v1")).thenReturn(appsV1Resources);

        final APIResourceList firstV1 = cachedClient.getServerResources("v1");
        final APIResourceList firstAppsV1 = cachedClient.getServerResources("apps/v1");
        final APIResourceList secondV1 = cachedClient.getServerResources("v1");
        final APIResourceList secondAppsV1 = cachedClient.getServerResources("apps/v1");

        assertThat(firstV1).isSameAs(secondV1);
        assertThat(firstAppsV1).isSameAs(secondAppsV1);
        assertThat(firstV1).isNotSameAs(firstAppsV1);
        verify(delegate, times(1)).getServerResources("v1");
        verify(delegate, times(1)).getServerResources("apps/v1");
    }
}
