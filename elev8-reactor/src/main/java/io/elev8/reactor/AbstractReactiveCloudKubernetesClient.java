package io.elev8.reactor;

import io.elev8.resources.clusterrole.ClusterRole;
import io.elev8.resources.clusterrolebinding.ClusterRoleBinding;
import io.elev8.resources.cloud.CloudKubernetesClient;
import io.elev8.resources.configmap.ConfigMap;
import io.elev8.resources.crd.CustomResourceDefinition;
import io.elev8.resources.cronjob.CronJob;
import io.elev8.resources.daemonset.DaemonSet;
import io.elev8.resources.deployment.Deployment;
import io.elev8.resources.event.Event;
import io.elev8.resources.horizontalpodautoscaler.HorizontalPodAutoscaler;
import io.elev8.resources.ingress.Ingress;
import io.elev8.resources.job.Job;
import io.elev8.resources.lease.Lease;
import io.elev8.resources.limitrange.LimitRange;
import io.elev8.resources.namespace.Namespace;
import io.elev8.resources.networkpolicy.NetworkPolicy;
import io.elev8.resources.persistentvolume.PersistentVolume;
import io.elev8.resources.persistentvolumeclaim.PersistentVolumeClaim;
import io.elev8.resources.pod.Pod;
import io.elev8.resources.poddisruptionbudget.PodDisruptionBudget;
import io.elev8.resources.replicaset.ReplicaSet;
import io.elev8.resources.resourcequota.ResourceQuota;
import io.elev8.resources.role.Role;
import io.elev8.resources.rolebinding.RoleBinding;
import io.elev8.resources.secret.Secret;
import io.elev8.resources.service.Service;
import io.elev8.resources.serviceaccount.ServiceAccount;
import io.elev8.resources.statefulset.StatefulSet;
import io.elev8.resources.verticalpodautoscaler.VerticalPodAutoscaler;
import lombok.Getter;

/**
 * Abstract base class for reactive cloud-specific Kubernetes clients.
 * Wraps a synchronous CloudKubernetesClient and provides reactive resource managers.
 */
public abstract class AbstractReactiveCloudKubernetesClient implements ReactiveCloudKubernetesClient {

    @Getter
    private final CloudKubernetesClient delegate;

    private final ReactiveResourceManager<Pod> podManager;
    private final ReactiveResourceManager<Service> serviceManager;
    private final ReactiveResourceManager<Deployment> deploymentManager;
    private final ReactiveResourceManager<DaemonSet> daemonSetManager;
    private final ReactiveResourceManager<Event> eventManager;
    private final ReactiveResourceManager<Job> jobManager;
    private final ReactiveResourceManager<Lease> leaseManager;
    private final ReactiveResourceManager<CronJob> cronJobManager;
    private final ReactiveResourceManager<StatefulSet> statefulSetManager;
    private final ReactiveResourceManager<ReplicaSet> replicaSetManager;
    private final ReactiveResourceManager<Ingress> ingressManager;
    private final ReactiveResourceManager<NetworkPolicy> networkPolicyManager;
    private final ReactiveResourceManager<HorizontalPodAutoscaler> horizontalPodAutoscalerManager;
    private final ReactiveResourceManager<VerticalPodAutoscaler> verticalPodAutoscalerManager;
    private final ReactiveResourceManager<LimitRange> limitRangeManager;
    private final ReactiveResourceManager<PodDisruptionBudget> podDisruptionBudgetManager;
    private final ReactiveResourceManager<ServiceAccount> serviceAccountManager;
    private final ReactiveResourceManager<Role> roleManager;
    private final ReactiveResourceManager<RoleBinding> roleBindingManager;
    private final ReactiveClusterResourceManager<ClusterRole> clusterRoleManager;
    private final ReactiveClusterResourceManager<ClusterRoleBinding> clusterRoleBindingManager;
    private final ReactiveClusterResourceManager<PersistentVolume> persistentVolumeManager;
    private final ReactiveResourceManager<PersistentVolumeClaim> persistentVolumeClaimManager;
    private final ReactiveResourceManager<ConfigMap> configMapManager;
    private final ReactiveResourceManager<Secret> secretManager;
    private final ReactiveResourceManager<ResourceQuota> resourceQuotaManager;
    private final ReactiveClusterResourceManager<Namespace> namespaceManager;
    private final ReactiveClusterResourceManager<CustomResourceDefinition> customResourceDefinitionManager;

    protected AbstractReactiveCloudKubernetesClient(final CloudKubernetesClient delegate) {
        this.delegate = delegate;
        this.podManager = new AbstractReactiveResourceManager<>(delegate.pods());
        this.serviceManager = new AbstractReactiveResourceManager<>(delegate.services());
        this.deploymentManager = new AbstractReactiveResourceManager<>(delegate.deployments());
        this.daemonSetManager = new AbstractReactiveResourceManager<>(delegate.daemonSets());
        this.eventManager = new AbstractReactiveResourceManager<>(delegate.events());
        this.jobManager = new AbstractReactiveResourceManager<>(delegate.jobs());
        this.leaseManager = new AbstractReactiveResourceManager<>(delegate.leases());
        this.cronJobManager = new AbstractReactiveResourceManager<>(delegate.cronJobs());
        this.statefulSetManager = new AbstractReactiveResourceManager<>(delegate.statefulSets());
        this.replicaSetManager = new AbstractReactiveResourceManager<>(delegate.replicaSets());
        this.ingressManager = new AbstractReactiveResourceManager<>(delegate.ingresses());
        this.networkPolicyManager = new AbstractReactiveResourceManager<>(delegate.networkPolicies());
        this.horizontalPodAutoscalerManager = new AbstractReactiveResourceManager<>(delegate.horizontalPodAutoscalers());
        this.verticalPodAutoscalerManager = new AbstractReactiveResourceManager<>(delegate.verticalPodAutoscalers());
        this.limitRangeManager = new AbstractReactiveResourceManager<>(delegate.limitRanges());
        this.podDisruptionBudgetManager = new AbstractReactiveResourceManager<>(delegate.podDisruptionBudgets());
        this.serviceAccountManager = new AbstractReactiveResourceManager<>(delegate.serviceAccounts());
        this.roleManager = new AbstractReactiveResourceManager<>(delegate.roles());
        this.roleBindingManager = new AbstractReactiveResourceManager<>(delegate.roleBindings());
        this.clusterRoleManager = new AbstractReactiveClusterResourceManager<>(delegate.clusterRoles());
        this.clusterRoleBindingManager = new AbstractReactiveClusterResourceManager<>(delegate.clusterRoleBindings());
        this.persistentVolumeManager = new AbstractReactiveClusterResourceManager<>(delegate.persistentVolumes());
        this.persistentVolumeClaimManager = new AbstractReactiveResourceManager<>(delegate.persistentVolumeClaims());
        this.configMapManager = new AbstractReactiveResourceManager<>(delegate.configMaps());
        this.secretManager = new AbstractReactiveResourceManager<>(delegate.secrets());
        this.resourceQuotaManager = new AbstractReactiveResourceManager<>(delegate.resourceQuotas());
        this.namespaceManager = new AbstractReactiveClusterResourceManager<>(delegate.namespaces());
        this.customResourceDefinitionManager = new AbstractReactiveClusterResourceManager<>(delegate.customResourceDefinitions());
    }

    @Override
    public ReactiveResourceManager<Pod> pods() {
        return podManager;
    }

    @Override
    public ReactiveResourceManager<Service> services() {
        return serviceManager;
    }

    @Override
    public ReactiveResourceManager<Deployment> deployments() {
        return deploymentManager;
    }

    @Override
    public ReactiveResourceManager<DaemonSet> daemonSets() {
        return daemonSetManager;
    }

    @Override
    public ReactiveResourceManager<Event> events() {
        return eventManager;
    }

    @Override
    public ReactiveResourceManager<Job> jobs() {
        return jobManager;
    }

    @Override
    public ReactiveResourceManager<Lease> leases() {
        return leaseManager;
    }

    @Override
    public ReactiveResourceManager<CronJob> cronJobs() {
        return cronJobManager;
    }

    @Override
    public ReactiveResourceManager<StatefulSet> statefulSets() {
        return statefulSetManager;
    }

    @Override
    public ReactiveResourceManager<ReplicaSet> replicaSets() {
        return replicaSetManager;
    }

    @Override
    public ReactiveResourceManager<Ingress> ingresses() {
        return ingressManager;
    }

    @Override
    public ReactiveResourceManager<NetworkPolicy> networkPolicies() {
        return networkPolicyManager;
    }

    @Override
    public ReactiveResourceManager<HorizontalPodAutoscaler> horizontalPodAutoscalers() {
        return horizontalPodAutoscalerManager;
    }

    @Override
    public ReactiveResourceManager<VerticalPodAutoscaler> verticalPodAutoscalers() {
        return verticalPodAutoscalerManager;
    }

    @Override
    public ReactiveResourceManager<LimitRange> limitRanges() {
        return limitRangeManager;
    }

    @Override
    public ReactiveResourceManager<PodDisruptionBudget> podDisruptionBudgets() {
        return podDisruptionBudgetManager;
    }

    @Override
    public ReactiveResourceManager<ServiceAccount> serviceAccounts() {
        return serviceAccountManager;
    }

    @Override
    public ReactiveResourceManager<Role> roles() {
        return roleManager;
    }

    @Override
    public ReactiveResourceManager<RoleBinding> roleBindings() {
        return roleBindingManager;
    }

    @Override
    public ReactiveClusterResourceManager<ClusterRole> clusterRoles() {
        return clusterRoleManager;
    }

    @Override
    public ReactiveClusterResourceManager<ClusterRoleBinding> clusterRoleBindings() {
        return clusterRoleBindingManager;
    }

    @Override
    public ReactiveClusterResourceManager<PersistentVolume> persistentVolumes() {
        return persistentVolumeManager;
    }

    @Override
    public ReactiveResourceManager<PersistentVolumeClaim> persistentVolumeClaims() {
        return persistentVolumeClaimManager;
    }

    @Override
    public ReactiveResourceManager<ConfigMap> configMaps() {
        return configMapManager;
    }

    @Override
    public ReactiveResourceManager<Secret> secrets() {
        return secretManager;
    }

    @Override
    public ReactiveResourceManager<ResourceQuota> resourceQuotas() {
        return resourceQuotaManager;
    }

    @Override
    public ReactiveClusterResourceManager<Namespace> namespaces() {
        return namespaceManager;
    }

    @Override
    public ReactiveClusterResourceManager<CustomResourceDefinition> customResourceDefinitions() {
        return customResourceDefinitionManager;
    }

    @Override
    public void close() {
        if (delegate != null) {
            delegate.close();
        }
    }
}
