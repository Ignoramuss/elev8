package io.elev8.resources.cloud;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.clusterrole.ClusterRoleManager;
import io.elev8.resources.clusterrolebinding.ClusterRoleBindingManager;
import io.elev8.resources.configmap.ConfigMapManager;
import io.elev8.resources.crd.CustomResourceDefinitionManager;
import io.elev8.resources.cronjob.CronJobManager;
import io.elev8.resources.daemonset.DaemonSetManager;
import io.elev8.resources.deployment.DeploymentManager;
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

/**
 * Common interface for cloud-specific Kubernetes clients.
 * Provides unified access to Kubernetes resource managers across EKS, GKE, and AKS.
 */
public interface CloudKubernetesClient extends AutoCloseable {

    KubernetesClient getKubernetesClient();

    PodManager pods();

    ServiceManager services();

    DeploymentManager deployments();

    DaemonSetManager daemonSets();

    EventManager events();

    JobManager jobs();

    LeaseManager leases();

    CronJobManager cronJobs();

    StatefulSetManager statefulSets();

    ReplicaSetManager replicaSets();

    IngressManager ingresses();

    NetworkPolicyManager networkPolicies();

    HorizontalPodAutoscalerManager horizontalPodAutoscalers();

    VerticalPodAutoscalerManager verticalPodAutoscalers();

    LimitRangeManager limitRanges();

    PodDisruptionBudgetManager podDisruptionBudgets();

    ServiceAccountManager serviceAccounts();

    RoleManager roles();

    RoleBindingManager roleBindings();

    ClusterRoleManager clusterRoles();

    ClusterRoleBindingManager clusterRoleBindings();

    PersistentVolumeManager persistentVolumes();

    PersistentVolumeClaimManager persistentVolumeClaims();

    ConfigMapManager configMaps();

    SecretManager secrets();

    ResourceQuotaManager resourceQuotas();

    NamespaceManager namespaces();

    CustomResourceDefinitionManager customResourceDefinitions();

    GenericResourceManager genericResources(String group, String version, String kind, String plural);

    GenericResourceManager genericResources(GenericResourceContext context);

    GenericClusterResourceManager genericClusterResources(String group, String version, String kind, String plural);

    GenericClusterResourceManager genericClusterResources(GenericResourceContext context);

    DynamicClient dynamic();

    @Override
    void close();
}
