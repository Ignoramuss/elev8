package io.elev8.cloud;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.cloud.CloudKubernetesClient;
import io.elev8.resources.clusterrole.ClusterRoleManager;
import io.elev8.resources.clusterrolebinding.ClusterRoleBindingManager;
import io.elev8.resources.configmap.ConfigMapManager;
import io.elev8.resources.crd.CustomResourceDefinitionManager;
import io.elev8.resources.cronjob.CronJobManager;
import io.elev8.resources.daemonset.DaemonSetManager;
import io.elev8.resources.deployment.DeploymentManager;
import io.elev8.resources.dynamic.DefaultDynamicClient;
import io.elev8.resources.dynamic.DynamicClient;
import io.elev8.resources.event.EventManager;
import io.elev8.resources.generic.GenericClusterResourceManager;
import io.elev8.resources.generic.GenericResourceContext;
import io.elev8.resources.generic.GenericResourceManager;
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
import lombok.Getter;

/**
 * Abstract base class for cloud-specific Kubernetes clients.
 * Provides common resource manager initialization and accessors for EKS, GKE, and AKS clients.
 */
public abstract class AbstractCloudKubernetesClient implements CloudKubernetesClient {

    @Getter
    private final KubernetesClient kubernetesClient;

    private final PodManager podManager;
    private final ServiceManager serviceManager;
    private final DeploymentManager deploymentManager;
    private final DaemonSetManager daemonSetManager;
    private final EventManager eventManager;
    private final JobManager jobManager;
    private final LeaseManager leaseManager;
    private final CronJobManager cronJobManager;
    private final StatefulSetManager statefulSetManager;
    private final ReplicaSetManager replicaSetManager;
    private final IngressManager ingressManager;
    private final NetworkPolicyManager networkPolicyManager;
    private final HorizontalPodAutoscalerManager horizontalPodAutoscalerManager;
    private final VerticalPodAutoscalerManager verticalPodAutoscalerManager;
    private final LimitRangeManager limitRangeManager;
    private final PodDisruptionBudgetManager podDisruptionBudgetManager;
    private final ServiceAccountManager serviceAccountManager;
    private final RoleManager roleManager;
    private final RoleBindingManager roleBindingManager;
    private final ClusterRoleManager clusterRoleManager;
    private final ClusterRoleBindingManager clusterRoleBindingManager;
    private final PersistentVolumeManager persistentVolumeManager;
    private final PersistentVolumeClaimManager persistentVolumeClaimManager;
    private final ConfigMapManager configMapManager;
    private final SecretManager secretManager;
    private final ResourceQuotaManager resourceQuotaManager;
    private final NamespaceManager namespaceManager;
    private final CustomResourceDefinitionManager customResourceDefinitionManager;

    protected AbstractCloudKubernetesClient(final KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
        this.podManager = new PodManager(kubernetesClient);
        this.serviceManager = new ServiceManager(kubernetesClient);
        this.deploymentManager = new DeploymentManager(kubernetesClient);
        this.daemonSetManager = new DaemonSetManager(kubernetesClient);
        this.eventManager = new EventManager(kubernetesClient);
        this.jobManager = new JobManager(kubernetesClient);
        this.leaseManager = new LeaseManager(kubernetesClient);
        this.cronJobManager = new CronJobManager(kubernetesClient);
        this.statefulSetManager = new StatefulSetManager(kubernetesClient);
        this.replicaSetManager = new ReplicaSetManager(kubernetesClient);
        this.ingressManager = new IngressManager(kubernetesClient);
        this.networkPolicyManager = new NetworkPolicyManager(kubernetesClient);
        this.horizontalPodAutoscalerManager = new HorizontalPodAutoscalerManager(kubernetesClient);
        this.verticalPodAutoscalerManager = new VerticalPodAutoscalerManager(kubernetesClient);
        this.limitRangeManager = new LimitRangeManager(kubernetesClient);
        this.podDisruptionBudgetManager = new PodDisruptionBudgetManager(kubernetesClient);
        this.serviceAccountManager = new ServiceAccountManager(kubernetesClient);
        this.roleManager = new RoleManager(kubernetesClient);
        this.roleBindingManager = new RoleBindingManager(kubernetesClient);
        this.clusterRoleManager = new ClusterRoleManager(kubernetesClient);
        this.clusterRoleBindingManager = new ClusterRoleBindingManager(kubernetesClient);
        this.persistentVolumeManager = new PersistentVolumeManager(kubernetesClient);
        this.persistentVolumeClaimManager = new PersistentVolumeClaimManager(kubernetesClient);
        this.configMapManager = new ConfigMapManager(kubernetesClient);
        this.secretManager = new SecretManager(kubernetesClient);
        this.resourceQuotaManager = new ResourceQuotaManager(kubernetesClient);
        this.namespaceManager = new NamespaceManager(kubernetesClient);
        this.customResourceDefinitionManager = new CustomResourceDefinitionManager(kubernetesClient);
    }

    @Override
    public PodManager pods() {
        return podManager;
    }

    @Override
    public ServiceManager services() {
        return serviceManager;
    }

    @Override
    public DeploymentManager deployments() {
        return deploymentManager;
    }

    @Override
    public DaemonSetManager daemonSets() {
        return daemonSetManager;
    }

    @Override
    public EventManager events() {
        return eventManager;
    }

    @Override
    public JobManager jobs() {
        return jobManager;
    }

    @Override
    public LeaseManager leases() {
        return leaseManager;
    }

    @Override
    public CronJobManager cronJobs() {
        return cronJobManager;
    }

    @Override
    public StatefulSetManager statefulSets() {
        return statefulSetManager;
    }

    @Override
    public ReplicaSetManager replicaSets() {
        return replicaSetManager;
    }

    @Override
    public IngressManager ingresses() {
        return ingressManager;
    }

    @Override
    public NetworkPolicyManager networkPolicies() {
        return networkPolicyManager;
    }

    @Override
    public HorizontalPodAutoscalerManager horizontalPodAutoscalers() {
        return horizontalPodAutoscalerManager;
    }

    @Override
    public VerticalPodAutoscalerManager verticalPodAutoscalers() {
        return verticalPodAutoscalerManager;
    }

    @Override
    public LimitRangeManager limitRanges() {
        return limitRangeManager;
    }

    @Override
    public PodDisruptionBudgetManager podDisruptionBudgets() {
        return podDisruptionBudgetManager;
    }

    @Override
    public ServiceAccountManager serviceAccounts() {
        return serviceAccountManager;
    }

    @Override
    public RoleManager roles() {
        return roleManager;
    }

    @Override
    public RoleBindingManager roleBindings() {
        return roleBindingManager;
    }

    @Override
    public ClusterRoleManager clusterRoles() {
        return clusterRoleManager;
    }

    @Override
    public ClusterRoleBindingManager clusterRoleBindings() {
        return clusterRoleBindingManager;
    }

    @Override
    public PersistentVolumeManager persistentVolumes() {
        return persistentVolumeManager;
    }

    @Override
    public PersistentVolumeClaimManager persistentVolumeClaims() {
        return persistentVolumeClaimManager;
    }

    @Override
    public ConfigMapManager configMaps() {
        return configMapManager;
    }

    @Override
    public SecretManager secrets() {
        return secretManager;
    }

    @Override
    public ResourceQuotaManager resourceQuotas() {
        return resourceQuotaManager;
    }

    @Override
    public NamespaceManager namespaces() {
        return namespaceManager;
    }

    @Override
    public CustomResourceDefinitionManager customResourceDefinitions() {
        return customResourceDefinitionManager;
    }

    @Override
    public GenericResourceManager genericResources(final String group,
                                                   final String version,
                                                   final String kind,
                                                   final String plural) {
        return new GenericResourceManager(kubernetesClient, group, version, kind, plural);
    }

    @Override
    public GenericResourceManager genericResources(final GenericResourceContext context) {
        return new GenericResourceManager(kubernetesClient, context);
    }

    @Override
    public GenericClusterResourceManager genericClusterResources(final String group,
                                                                  final String version,
                                                                  final String kind,
                                                                  final String plural) {
        return new GenericClusterResourceManager(kubernetesClient, group, version, kind, plural);
    }

    @Override
    public GenericClusterResourceManager genericClusterResources(final GenericResourceContext context) {
        return new GenericClusterResourceManager(kubernetesClient, context);
    }

    @Override
    public DynamicClient dynamic() {
        return new DefaultDynamicClient(kubernetesClient);
    }

    @Override
    public void close() {
        if (kubernetesClient != null) {
            kubernetesClient.close();
        }
    }
}
