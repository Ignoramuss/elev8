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

/**
 * Reactive interface for cloud-specific Kubernetes clients.
 * Provides non-blocking access to Kubernetes resources using Project Reactor.
 */
public interface ReactiveCloudKubernetesClient extends AutoCloseable {

    ReactiveResourceManager<Pod> pods();

    ReactiveResourceManager<Service> services();

    ReactiveResourceManager<Deployment> deployments();

    ReactiveResourceManager<DaemonSet> daemonSets();

    ReactiveResourceManager<Event> events();

    ReactiveResourceManager<Job> jobs();

    ReactiveResourceManager<Lease> leases();

    ReactiveResourceManager<CronJob> cronJobs();

    ReactiveResourceManager<StatefulSet> statefulSets();

    ReactiveResourceManager<ReplicaSet> replicaSets();

    ReactiveResourceManager<Ingress> ingresses();

    ReactiveResourceManager<NetworkPolicy> networkPolicies();

    ReactiveResourceManager<HorizontalPodAutoscaler> horizontalPodAutoscalers();

    ReactiveResourceManager<VerticalPodAutoscaler> verticalPodAutoscalers();

    ReactiveResourceManager<LimitRange> limitRanges();

    ReactiveResourceManager<PodDisruptionBudget> podDisruptionBudgets();

    ReactiveResourceManager<ServiceAccount> serviceAccounts();

    ReactiveResourceManager<Role> roles();

    ReactiveResourceManager<RoleBinding> roleBindings();

    ReactiveClusterResourceManager<ClusterRole> clusterRoles();

    ReactiveClusterResourceManager<ClusterRoleBinding> clusterRoleBindings();

    ReactiveClusterResourceManager<PersistentVolume> persistentVolumes();

    ReactiveResourceManager<PersistentVolumeClaim> persistentVolumeClaims();

    ReactiveResourceManager<ConfigMap> configMaps();

    ReactiveResourceManager<Secret> secrets();

    ReactiveResourceManager<ResourceQuota> resourceQuotas();

    ReactiveClusterResourceManager<Namespace> namespaces();

    ReactiveClusterResourceManager<CustomResourceDefinition> customResourceDefinitions();

    /**
     * Get the underlying synchronous CloudKubernetesClient.
     *
     * @return the delegate client
     */
    CloudKubernetesClient getDelegate();

    @Override
    void close();
}
