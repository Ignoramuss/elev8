package io.elev8.reactor;

import io.elev8.resources.cloud.CloudKubernetesClient;
import io.elev8.resources.clusterrole.ClusterRoleManager;
import io.elev8.resources.clusterrolebinding.ClusterRoleBindingManager;
import io.elev8.resources.configmap.ConfigMapManager;
import io.elev8.resources.crd.CustomResourceDefinitionManager;
import io.elev8.resources.cronjob.CronJobManager;
import io.elev8.resources.daemonset.DaemonSetManager;
import io.elev8.resources.deployment.DeploymentManager;
import io.elev8.resources.event.EventManager;
import io.elev8.resources.horizontalpodautoscaler.HorizontalPodAutoscalerManager;
import io.elev8.resources.ingress.IngressManager;
import io.elev8.resources.job.JobManager;
import io.elev8.resources.lease.LeaseManager;
import io.elev8.resources.limitrange.LimitRangeManager;
import io.elev8.resources.namespace.NamespaceManager;
import io.elev8.resources.networkpolicy.NetworkPolicyManager;
import io.elev8.resources.persistentvolume.PersistentVolumeManager;
import io.elev8.resources.persistentvolumeclaim.PersistentVolumeClaimManager;
import io.elev8.resources.pod.PodManager;
import io.elev8.resources.poddisruptionbudget.PodDisruptionBudgetManager;
import io.elev8.resources.replicaset.ReplicaSetManager;
import io.elev8.resources.resourcequota.ResourceQuotaManager;
import io.elev8.resources.role.RoleManager;
import io.elev8.resources.rolebinding.RoleBindingManager;
import io.elev8.resources.secret.SecretManager;
import io.elev8.resources.service.ServiceManager;
import io.elev8.resources.serviceaccount.ServiceAccountManager;
import io.elev8.resources.statefulset.StatefulSetManager;
import io.elev8.resources.verticalpodautoscaler.VerticalPodAutoscalerManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractReactiveCloudKubernetesClientTest {

    @Mock
    private CloudKubernetesClient delegate;

    @Mock
    private PodManager podManager;
    @Mock
    private ServiceManager serviceManager;
    @Mock
    private DeploymentManager deploymentManager;
    @Mock
    private DaemonSetManager daemonSetManager;
    @Mock
    private EventManager eventManager;
    @Mock
    private JobManager jobManager;
    @Mock
    private LeaseManager leaseManager;
    @Mock
    private CronJobManager cronJobManager;
    @Mock
    private StatefulSetManager statefulSetManager;
    @Mock
    private ReplicaSetManager replicaSetManager;
    @Mock
    private IngressManager ingressManager;
    @Mock
    private NetworkPolicyManager networkPolicyManager;
    @Mock
    private HorizontalPodAutoscalerManager horizontalPodAutoscalerManager;
    @Mock
    private VerticalPodAutoscalerManager verticalPodAutoscalerManager;
    @Mock
    private LimitRangeManager limitRangeManager;
    @Mock
    private PodDisruptionBudgetManager podDisruptionBudgetManager;
    @Mock
    private ServiceAccountManager serviceAccountManager;
    @Mock
    private RoleManager roleManager;
    @Mock
    private RoleBindingManager roleBindingManager;
    @Mock
    private ClusterRoleManager clusterRoleManager;
    @Mock
    private ClusterRoleBindingManager clusterRoleBindingManager;
    @Mock
    private PersistentVolumeManager persistentVolumeManager;
    @Mock
    private PersistentVolumeClaimManager persistentVolumeClaimManager;
    @Mock
    private ConfigMapManager configMapManager;
    @Mock
    private SecretManager secretManager;
    @Mock
    private ResourceQuotaManager resourceQuotaManager;
    @Mock
    private NamespaceManager namespaceManager;
    @Mock
    private CustomResourceDefinitionManager customResourceDefinitionManager;

    private TestReactiveClient client;

    @BeforeEach
    void setUp() {
        when(delegate.pods()).thenReturn(podManager);
        when(delegate.services()).thenReturn(serviceManager);
        when(delegate.deployments()).thenReturn(deploymentManager);
        when(delegate.daemonSets()).thenReturn(daemonSetManager);
        when(delegate.events()).thenReturn(eventManager);
        when(delegate.jobs()).thenReturn(jobManager);
        when(delegate.leases()).thenReturn(leaseManager);
        when(delegate.cronJobs()).thenReturn(cronJobManager);
        when(delegate.statefulSets()).thenReturn(statefulSetManager);
        when(delegate.replicaSets()).thenReturn(replicaSetManager);
        when(delegate.ingresses()).thenReturn(ingressManager);
        when(delegate.networkPolicies()).thenReturn(networkPolicyManager);
        when(delegate.horizontalPodAutoscalers()).thenReturn(horizontalPodAutoscalerManager);
        when(delegate.verticalPodAutoscalers()).thenReturn(verticalPodAutoscalerManager);
        when(delegate.limitRanges()).thenReturn(limitRangeManager);
        when(delegate.podDisruptionBudgets()).thenReturn(podDisruptionBudgetManager);
        when(delegate.serviceAccounts()).thenReturn(serviceAccountManager);
        when(delegate.roles()).thenReturn(roleManager);
        when(delegate.roleBindings()).thenReturn(roleBindingManager);
        when(delegate.clusterRoles()).thenReturn(clusterRoleManager);
        when(delegate.clusterRoleBindings()).thenReturn(clusterRoleBindingManager);
        when(delegate.persistentVolumes()).thenReturn(persistentVolumeManager);
        when(delegate.persistentVolumeClaims()).thenReturn(persistentVolumeClaimManager);
        when(delegate.configMaps()).thenReturn(configMapManager);
        when(delegate.secrets()).thenReturn(secretManager);
        when(delegate.resourceQuotas()).thenReturn(resourceQuotaManager);
        when(delegate.namespaces()).thenReturn(namespaceManager);
        when(delegate.customResourceDefinitions()).thenReturn(customResourceDefinitionManager);

        client = new TestReactiveClient(delegate);
    }

    @Test
    void shouldImplementReactiveCloudKubernetesClient() {
        assertThat(client).isInstanceOf(ReactiveCloudKubernetesClient.class);
    }

    @Test
    void shouldReturnDelegate() {
        assertThat(client.getDelegate()).isSameAs(delegate);
    }

    @Test
    void shouldInitializeAllReactiveResourceManagers() {
        assertThat(client.pods()).isNotNull();
        assertThat(client.services()).isNotNull();
        assertThat(client.deployments()).isNotNull();
        assertThat(client.daemonSets()).isNotNull();
        assertThat(client.events()).isNotNull();
        assertThat(client.jobs()).isNotNull();
        assertThat(client.leases()).isNotNull();
        assertThat(client.cronJobs()).isNotNull();
        assertThat(client.statefulSets()).isNotNull();
        assertThat(client.replicaSets()).isNotNull();
        assertThat(client.ingresses()).isNotNull();
        assertThat(client.networkPolicies()).isNotNull();
        assertThat(client.horizontalPodAutoscalers()).isNotNull();
        assertThat(client.verticalPodAutoscalers()).isNotNull();
        assertThat(client.limitRanges()).isNotNull();
        assertThat(client.podDisruptionBudgets()).isNotNull();
        assertThat(client.serviceAccounts()).isNotNull();
        assertThat(client.roles()).isNotNull();
        assertThat(client.roleBindings()).isNotNull();
        assertThat(client.clusterRoles()).isNotNull();
        assertThat(client.clusterRoleBindings()).isNotNull();
        assertThat(client.persistentVolumes()).isNotNull();
        assertThat(client.persistentVolumeClaims()).isNotNull();
        assertThat(client.configMaps()).isNotNull();
        assertThat(client.secrets()).isNotNull();
        assertThat(client.resourceQuotas()).isNotNull();
        assertThat(client.namespaces()).isNotNull();
        assertThat(client.customResourceDefinitions()).isNotNull();
    }

    @Test
    void shouldReturnSameManagerInstanceOnMultipleCalls() {
        assertThat(client.pods()).isSameAs(client.pods());
        assertThat(client.services()).isSameAs(client.services());
        assertThat(client.deployments()).isSameAs(client.deployments());
        assertThat(client.namespaces()).isSameAs(client.namespaces());
    }

    @Test
    void shouldReturnReactiveResourceManagerInstances() {
        assertThat(client.pods()).isInstanceOf(ReactiveResourceManager.class);
        assertThat(client.services()).isInstanceOf(ReactiveResourceManager.class);
        assertThat(client.deployments()).isInstanceOf(ReactiveResourceManager.class);
    }

    @Test
    void shouldReturnReactiveClusterResourceManagerInstances() {
        assertThat(client.namespaces()).isInstanceOf(ReactiveClusterResourceManager.class);
        assertThat(client.clusterRoles()).isInstanceOf(ReactiveClusterResourceManager.class);
        assertThat(client.persistentVolumes()).isInstanceOf(ReactiveClusterResourceManager.class);
    }

    @Test
    void shouldDelegateCloseToDelegate() {
        client.close();

        verify(delegate).close();
    }

    @Test
    void shouldHandleDoubleCloseWithoutError() {
        client.close();
        client.close();

        verify(delegate, times(2)).close();
    }

    private static class TestReactiveClient extends AbstractReactiveCloudKubernetesClient {
        TestReactiveClient(final CloudKubernetesClient delegate) {
            super(delegate);
        }
    }
}
