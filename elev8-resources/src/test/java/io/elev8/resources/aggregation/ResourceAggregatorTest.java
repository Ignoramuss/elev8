package io.elev8.resources.aggregation;

import io.elev8.core.list.ListOptions;
import io.elev8.resources.KubernetesResource;
import io.elev8.resources.ResourceException;
import io.elev8.resources.cloud.CloudKubernetesClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ResourceAggregatorTest {

    private CloudKubernetesClient client;

    // Captured deep-stub manager references — must be captured BEFORE any doReturn/when calls
    private Object podMgr;
    private Object serviceMgr;
    private Object configMapMgr;
    private Object secretMgr;
    private Object eventMgr;
    private Object pvcMgr;
    private Object serviceAccountMgr;
    private Object limitRangeMgr;
    private Object resourceQuotaMgr;
    private Object deploymentMgr;
    private Object daemonSetMgr;
    private Object statefulSetMgr;
    private Object replicaSetMgr;
    private Object jobMgr;
    private Object cronJobMgr;
    private Object ingressMgr;
    private Object networkPolicyMgr;
    private Object hpaMgr;
    private Object vpaMgr;
    private Object pdbMgr;
    private Object leaseMgr;
    private Object roleMgr;
    private Object roleBindingMgr;
    private Object namespaceMgr;
    private Object pvMgr;
    private Object clusterRoleMgr;
    private Object clusterRoleBindingMgr;
    private Object crdMgr;

    @BeforeEach
    void setUp() throws ResourceException {
        client = mock(CloudKubernetesClient.class, Mockito.RETURNS_DEEP_STUBS);

        // Phase 1: Capture ALL manager references from deep stubs first
        podMgr = client.pods();
        serviceMgr = client.services();
        configMapMgr = client.configMaps();
        secretMgr = client.secrets();
        eventMgr = client.events();
        pvcMgr = client.persistentVolumeClaims();
        serviceAccountMgr = client.serviceAccounts();
        limitRangeMgr = client.limitRanges();
        resourceQuotaMgr = client.resourceQuotas();
        deploymentMgr = client.deployments();
        daemonSetMgr = client.daemonSets();
        statefulSetMgr = client.statefulSets();
        replicaSetMgr = client.replicaSets();
        jobMgr = client.jobs();
        cronJobMgr = client.cronJobs();
        ingressMgr = client.ingresses();
        networkPolicyMgr = client.networkPolicies();
        hpaMgr = client.horizontalPodAutoscalers();
        vpaMgr = client.verticalPodAutoscalers();
        pdbMgr = client.podDisruptionBudgets();
        leaseMgr = client.leases();
        roleMgr = client.roles();
        roleBindingMgr = client.roleBindings();
        namespaceMgr = client.namespaces();
        pvMgr = client.persistentVolumes();
        clusterRoleMgr = client.clusterRoles();
        clusterRoleBindingMgr = client.clusterRoleBindings();
        crdMgr = client.customResourceDefinitions();

        // Phase 2: Stub all namespaced managers with empty lists
        stubNamespacedManager(podMgr);
        stubNamespacedManager(serviceMgr);
        stubNamespacedManager(configMapMgr);
        stubNamespacedManager(secretMgr);
        stubNamespacedManager(eventMgr);
        stubNamespacedManager(pvcMgr);
        stubNamespacedManager(serviceAccountMgr);
        stubNamespacedManager(limitRangeMgr);
        stubNamespacedManager(resourceQuotaMgr);
        stubNamespacedManager(deploymentMgr);
        stubNamespacedManager(daemonSetMgr);
        stubNamespacedManager(statefulSetMgr);
        stubNamespacedManager(replicaSetMgr);
        stubNamespacedManager(jobMgr);
        stubNamespacedManager(cronJobMgr);
        stubNamespacedManager(ingressMgr);
        stubNamespacedManager(networkPolicyMgr);
        stubNamespacedManager(hpaMgr);
        stubNamespacedManager(vpaMgr);
        stubNamespacedManager(pdbMgr);
        stubNamespacedManager(leaseMgr);
        stubNamespacedManager(roleMgr);
        stubNamespacedManager(roleBindingMgr);

        // Phase 3: Stub all cluster managers with empty lists
        stubClusterManager(namespaceMgr);
        stubClusterManager(pvMgr);
        stubClusterManager(clusterRoleMgr);
        stubClusterManager(clusterRoleBindingMgr);
        stubClusterManager(crdMgr);
    }

    @SuppressWarnings("unchecked")
    private void stubNamespacedManager(Object manager) throws ResourceException {
        // Deep stubs return proxies that implement the correct interface
        // We can stub list(String) and list(String, ListOptions) via the proxy
        var rm = (io.elev8.resources.ResourceManager<? extends KubernetesResource>) manager;
        doReturn(Collections.emptyList()).when(rm).list(any(String.class));
        doReturn(Collections.emptyList()).when(rm).list(any(String.class), any(ListOptions.class));
    }

    @SuppressWarnings("unchecked")
    private void stubClusterManager(Object manager) throws ResourceException {
        var cm = (io.elev8.resources.ClusterResourceManager<? extends KubernetesResource>) manager;
        doReturn(Collections.emptyList()).when(cm).list();
        doReturn(Collections.emptyList()).when(cm).list(any(ListOptions.class));
    }

    @Nested
    class Construction {

        @Test
        void shouldRejectNullClient() {
            assertThatThrownBy(() -> new ResourceAggregator(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("client must not be null");
        }
    }

    @Nested
    class NamespacedQueries {

        @Nested
        class TypeSelection {

            @Test
            void shouldAcceptNamespacedTypes() {
                final var aggregator = new ResourceAggregator(client);

                assertThat(aggregator.inNamespace("default").types(ResourceType.POD, ResourceType.DEPLOYMENT))
                        .isNotNull();
            }

            @Test
            void shouldRejectClusterScopedTypeForNamespacedQuery() {
                final var aggregator = new ResourceAggregator(client);

                assertThatThrownBy(() -> aggregator.inNamespace("default").types(ResourceType.NAMESPACE))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Not a namespaced type");
            }

            @Test
            void allTypesShouldAddAllNamespaced() throws ResourceException {
                final var aggregator = new ResourceAggregator(client);
                final var result = aggregator.inNamespace("default").allTypes().list();

                assertThat(result.types()).containsExactlyInAnyOrderElementsOf(ResourceType.ALL_NAMESPACED);
            }

            @Test
            void commonShouldAddCommonTypes() throws ResourceException {
                final var aggregator = new ResourceAggregator(client);
                final var result = aggregator.inNamespace("default").common().list();

                assertThat(result.types()).containsExactlyInAnyOrderElementsOf(ResourceType.COMMON);
            }
        }

        @Nested
        class Listing {

            @Test
            @SuppressWarnings("unchecked")
            void shouldDispatchToCorrectManager() throws ResourceException {
                final KubernetesResource pod = mock(KubernetesResource.class);
                var rm = (io.elev8.resources.ResourceManager<KubernetesResource>) podMgr;
                doReturn(List.of(pod)).when(rm).list("default");
                final var aggregator = new ResourceAggregator(client);

                final var result = aggregator.inNamespace("default")
                        .types(ResourceType.POD)
                        .list();

                assertThat(result.get(ResourceType.POD)).hasSize(1).first().isEqualTo(pod);
                verify(rm).list("default");
            }

            @Test
            @SuppressWarnings("unchecked")
            void shouldDispatchMultipleTypes() throws ResourceException {
                final KubernetesResource pod = mock(KubernetesResource.class);
                final KubernetesResource svc = mock(KubernetesResource.class);
                var podRm = (io.elev8.resources.ResourceManager<KubernetesResource>) podMgr;
                var svcRm = (io.elev8.resources.ResourceManager<KubernetesResource>) serviceMgr;
                doReturn(List.of(pod)).when(podRm).list("ns1");
                doReturn(List.of(svc)).when(svcRm).list("ns1");
                final var aggregator = new ResourceAggregator(client);

                final var result = aggregator.inNamespace("ns1")
                        .types(ResourceType.POD, ResourceType.SERVICE)
                        .list();

                assertThat(result.size()).isEqualTo(2);
                assertThat(result.get(ResourceType.POD)).hasSize(1).first().isEqualTo(pod);
                assertThat(result.get(ResourceType.SERVICE)).hasSize(1).first().isEqualTo(svc);
            }

            @Test
            @SuppressWarnings("unchecked")
            void shouldForwardListOptions() throws ResourceException {
                final var options = ListOptions.withLabelSelector("app=test");
                var rm = (io.elev8.resources.ResourceManager<KubernetesResource>) podMgr;
                doReturn(Collections.emptyList()).when(rm).list("default", options);
                final var aggregator = new ResourceAggregator(client);

                aggregator.inNamespace("default")
                        .types(ResourceType.POD)
                        .withListOptions(options)
                        .list();

                verify(rm).list("default", options);
            }

            @Test
            @SuppressWarnings("unchecked")
            void shouldSupportStringLabelSelector() throws ResourceException {
                var rm = (io.elev8.resources.ResourceManager<KubernetesResource>) podMgr;
                doReturn(Collections.emptyList()).when(rm).list(eq("default"), any(ListOptions.class));
                final var aggregator = new ResourceAggregator(client);

                aggregator.inNamespace("default")
                        .types(ResourceType.POD)
                        .withLabelSelector("app=myapp")
                        .list();

                verify(rm).list(eq("default"), any(ListOptions.class));
            }

            @Test
            void shouldRejectEmptyTypes() {
                final var aggregator = new ResourceAggregator(client);

                assertThatThrownBy(() -> aggregator.inNamespace("default").list())
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("No resource types selected");
            }

            @Test
            @SuppressWarnings("unchecked")
            void shouldPropagateResourceException() throws ResourceException {
                var rm = (io.elev8.resources.ResourceManager<KubernetesResource>) daemonSetMgr;
                doThrow(new ResourceException("API error")).when(rm).list("default");
                final var aggregator = new ResourceAggregator(client);

                assertThatThrownBy(() -> aggregator.inNamespace("default")
                        .types(ResourceType.POD, ResourceType.DAEMON_SET)
                        .list())
                        .isInstanceOf(ResourceException.class)
                        .hasMessageContaining("API error");
            }

            @Test
            @SuppressWarnings("unchecked")
            void shouldDispatchAllNamespacedTypes() throws ResourceException {
                final var aggregator = new ResourceAggregator(client);
                final var result = aggregator.inNamespace("test").allTypes().list();

                assertThat(result.types()).hasSize(23);
                verify((io.elev8.resources.ResourceManager<?>) podMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) serviceMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) configMapMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) secretMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) eventMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) pvcMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) serviceAccountMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) limitRangeMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) resourceQuotaMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) deploymentMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) daemonSetMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) statefulSetMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) replicaSetMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) jobMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) cronJobMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) ingressMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) networkPolicyMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) hpaMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) vpaMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) pdbMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) leaseMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) roleMgr).list("test");
                verify((io.elev8.resources.ResourceManager<?>) roleBindingMgr).list("test");
            }
        }

        @Nested
        class Counting {

            @Test
            @SuppressWarnings("unchecked")
            void shouldReturnCorrectCounts() throws ResourceException {
                final KubernetesResource pod1 = mock(KubernetesResource.class);
                final KubernetesResource pod2 = mock(KubernetesResource.class);
                final KubernetesResource svc = mock(KubernetesResource.class);
                var podRm = (io.elev8.resources.ResourceManager<KubernetesResource>) podMgr;
                var svcRm = (io.elev8.resources.ResourceManager<KubernetesResource>) serviceMgr;
                doReturn(List.of(pod1, pod2)).when(podRm).list("default");
                doReturn(List.of(svc)).when(svcRm).list("default");
                final var aggregator = new ResourceAggregator(client);

                final var counts = aggregator.inNamespace("default")
                        .types(ResourceType.POD, ResourceType.SERVICE)
                        .count();

                assertThat(counts.get(ResourceType.POD)).isEqualTo(2);
                assertThat(counts.get(ResourceType.SERVICE)).isEqualTo(1);
                assertThat(counts.total()).isEqualTo(3);
            }

            @Test
            @SuppressWarnings("unchecked")
            void shouldPropagateErrorOnCount() throws ResourceException {
                var rm = (io.elev8.resources.ResourceManager<KubernetesResource>) podMgr;
                doThrow(new ResourceException("Forbidden", 403)).when(rm).list("default");
                final var aggregator = new ResourceAggregator(client);

                assertThatThrownBy(() -> aggregator.inNamespace("default")
                        .types(ResourceType.POD)
                        .count())
                        .isInstanceOf(ResourceException.class);
            }
        }
    }

    @Nested
    class ClusterQueries {

        @Nested
        class TypeSelection {

            @Test
            void shouldAcceptClusterScopedTypes() {
                final var aggregator = new ResourceAggregator(client);

                assertThat(aggregator.clusterScoped().types(ResourceType.NAMESPACE, ResourceType.PERSISTENT_VOLUME))
                        .isNotNull();
            }

            @Test
            void shouldRejectNamespacedTypeForClusterQuery() {
                final var aggregator = new ResourceAggregator(client);

                assertThatThrownBy(() -> aggregator.clusterScoped().types(ResourceType.POD))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Not a cluster-scoped type");
            }

            @Test
            void allTypesShouldAddAllCluster() throws ResourceException {
                final var aggregator = new ResourceAggregator(client);
                final var result = aggregator.clusterScoped().allTypes().list();

                assertThat(result.types()).containsExactlyInAnyOrderElementsOf(ResourceType.ALL_CLUSTER);
            }
        }

        @Nested
        class Listing {

            @Test
            @SuppressWarnings("unchecked")
            void shouldDispatchToCorrectManager() throws ResourceException {
                final KubernetesResource ns = mock(KubernetesResource.class);
                var cm = (io.elev8.resources.ClusterResourceManager<KubernetesResource>) namespaceMgr;
                doReturn(List.of(ns)).when(cm).list();
                final var aggregator = new ResourceAggregator(client);

                final var result = aggregator.clusterScoped()
                        .types(ResourceType.NAMESPACE)
                        .list();

                assertThat(result.get(ResourceType.NAMESPACE)).hasSize(1).first().isEqualTo(ns);
                verify(cm).list();
            }

            @Test
            @SuppressWarnings("unchecked")
            void shouldDispatchAllClusterTypes() throws ResourceException {
                final var aggregator = new ResourceAggregator(client);
                final var result = aggregator.clusterScoped().allTypes().list();

                assertThat(result.types()).hasSize(5);
                verify((io.elev8.resources.ClusterResourceManager<?>) namespaceMgr).list();
                verify((io.elev8.resources.ClusterResourceManager<?>) pvMgr).list();
                verify((io.elev8.resources.ClusterResourceManager<?>) clusterRoleMgr).list();
                verify((io.elev8.resources.ClusterResourceManager<?>) clusterRoleBindingMgr).list();
                verify((io.elev8.resources.ClusterResourceManager<?>) crdMgr).list();
            }

            @Test
            @SuppressWarnings("unchecked")
            void shouldForwardListOptions() throws ResourceException {
                final var options = ListOptions.withLabelSelector("env=prod");
                var cm = (io.elev8.resources.ClusterResourceManager<KubernetesResource>) namespaceMgr;
                doReturn(Collections.emptyList()).when(cm).list(options);
                final var aggregator = new ResourceAggregator(client);

                aggregator.clusterScoped()
                        .types(ResourceType.NAMESPACE)
                        .withListOptions(options)
                        .list();

                verify(cm).list(options);
            }

            @Test
            @SuppressWarnings("unchecked")
            void shouldPropagateResourceException() throws ResourceException {
                var cm = (io.elev8.resources.ClusterResourceManager<KubernetesResource>) namespaceMgr;
                doThrow(new ResourceException("Unauthorized", 401)).when(cm).list();
                final var aggregator = new ResourceAggregator(client);

                assertThatThrownBy(() -> aggregator.clusterScoped()
                        .types(ResourceType.NAMESPACE)
                        .list())
                        .isInstanceOf(ResourceException.class)
                        .hasMessageContaining("Unauthorized");
            }

            @Test
            void shouldRejectEmptyTypes() {
                final var aggregator = new ResourceAggregator(client);

                assertThatThrownBy(() -> aggregator.clusterScoped().list())
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("No resource types selected");
            }
        }

        @Nested
        class Counting {

            @Test
            @SuppressWarnings("unchecked")
            void shouldReturnCorrectCounts() throws ResourceException {
                final KubernetesResource ns1 = mock(KubernetesResource.class);
                final KubernetesResource ns2 = mock(KubernetesResource.class);
                final KubernetesResource pv = mock(KubernetesResource.class);
                var nsCm = (io.elev8.resources.ClusterResourceManager<KubernetesResource>) namespaceMgr;
                var pvCm = (io.elev8.resources.ClusterResourceManager<KubernetesResource>) pvMgr;
                doReturn(List.of(ns1, ns2)).when(nsCm).list();
                doReturn(List.of(pv)).when(pvCm).list();
                final var aggregator = new ResourceAggregator(client);

                final var counts = aggregator.clusterScoped()
                        .types(ResourceType.NAMESPACE, ResourceType.PERSISTENT_VOLUME)
                        .count();

                assertThat(counts.get(ResourceType.NAMESPACE)).isEqualTo(2);
                assertThat(counts.get(ResourceType.PERSISTENT_VOLUME)).isEqualTo(1);
                assertThat(counts.total()).isEqualTo(3);
            }
        }
    }
}
