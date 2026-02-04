package io.elev8.aks;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.containerservice.ContainerServiceManager;
import com.azure.resourcemanager.containerservice.models.CredentialResult;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import io.elev8.auth.azure.AzureAuthProvider;
import io.elev8.core.auth.AuthProvider;
import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientConfig;
import io.elev8.resources.configmap.ConfigMapManager;
import io.elev8.resources.crd.CustomResourceDefinitionManager;
import io.elev8.resources.dynamic.DefaultDynamicClient;
import io.elev8.resources.dynamic.DynamicClient;
import io.elev8.resources.generic.GenericClusterResourceManager;
import io.elev8.resources.generic.GenericResourceContext;
import io.elev8.resources.generic.GenericResourceManager;
import io.elev8.resources.cronjob.CronJobManager;
import io.elev8.resources.daemonset.DaemonSetManager;
import io.elev8.resources.deployment.DeploymentManager;
import io.elev8.resources.event.EventManager;
import io.elev8.resources.horizontalpodautoscaler.HorizontalPodAutoscalerManager;
import io.elev8.resources.ingress.IngressManager;
import io.elev8.resources.job.JobManager;
import io.elev8.resources.lease.LeaseManager;
import io.elev8.resources.limitrange.LimitRangeManager;
import io.elev8.resources.verticalpodautoscaler.VerticalPodAutoscalerManager;
import io.elev8.resources.namespace.NamespaceManager;
import io.elev8.resources.networkpolicy.NetworkPolicyManager;
import io.elev8.resources.persistentvolume.PersistentVolumeManager;
import io.elev8.resources.persistentvolumeclaim.PersistentVolumeClaimManager;
import io.elev8.resources.pod.PodManager;
import io.elev8.resources.poddisruptionbudget.PodDisruptionBudgetManager;
import io.elev8.resources.replicaset.ReplicaSetManager;
import io.elev8.resources.resourcequota.ResourceQuotaManager;
import io.elev8.resources.clusterrole.ClusterRoleManager;
import io.elev8.resources.clusterrolebinding.ClusterRoleBindingManager;
import io.elev8.resources.role.RoleManager;
import io.elev8.resources.rolebinding.RoleBindingManager;
import io.elev8.resources.secret.SecretManager;
import io.elev8.resources.service.ServiceManager;
import io.elev8.resources.serviceaccount.ServiceAccountManager;
import io.elev8.resources.statefulset.StatefulSetManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

/**
 * AKS-optimized Kubernetes client with native Azure authentication.
 */
@Slf4j
public final class AksClient implements AutoCloseable {

    private final KubernetesClient kubernetesClient;

    @Getter
    private final String subscriptionId;

    @Getter
    private final String resourceGroupName;

    @Getter
    private final String clusterName;

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

    private final boolean skipTlsVerify;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final String namespace;
    private final String apiServerUrl;
    private final String certificateAuthority;
    private final AuthProvider authProvider;
    private final TokenCredential credential;
    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private final String managedIdentityClientId;

    private static final boolean DEFAULT_SKIP_TLS_VERIFY = false;
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);
    private static final String DEFAULT_NAMESPACE = "default";

    @lombok.Builder(toBuilder = true)
    private AksClient(final String subscriptionId,
                      final String resourceGroupName,
                      final String clusterName,
                      final String apiServerUrl,
                      final String certificateAuthority,
                      final Boolean skipTlsVerify,
                      final Duration connectTimeout,
                      final Duration readTimeout,
                      final String namespace,
                      final AuthProvider authProvider,
                      final TokenCredential credential,
                      final String tenantId,
                      final String clientId,
                      final String clientSecret,
                      final String managedIdentityClientId) {

        if (subscriptionId == null || subscriptionId.isEmpty()) {
            throw new IllegalArgumentException("Subscription ID is required");
        }
        if (resourceGroupName == null || resourceGroupName.isEmpty()) {
            throw new IllegalArgumentException("Resource group name is required");
        }
        if (clusterName == null || clusterName.isEmpty()) {
            throw new IllegalArgumentException("Cluster name is required");
        }

        this.subscriptionId = subscriptionId;
        this.resourceGroupName = resourceGroupName;
        this.clusterName = clusterName;
        this.apiServerUrl = apiServerUrl;
        this.certificateAuthority = certificateAuthority;
        this.skipTlsVerify = skipTlsVerify != null ? skipTlsVerify : DEFAULT_SKIP_TLS_VERIFY;
        this.connectTimeout = connectTimeout != null ? connectTimeout : DEFAULT_CONNECT_TIMEOUT;
        this.readTimeout = readTimeout != null ? readTimeout : DEFAULT_READ_TIMEOUT;
        this.namespace = namespace != null ? namespace : DEFAULT_NAMESPACE;
        this.authProvider = authProvider;
        this.credential = credential;
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.managedIdentityClientId = managedIdentityClientId;

        final String finalApiServerUrl;
        final String finalCertificateAuthority;

        if (apiServerUrl == null || certificateAuthority == null) {
            log.debug("Auto-discovering AKS cluster details for: {}", clusterName);
            final ClusterDetails details = discoverClusterDetails();
            finalApiServerUrl = details.endpoint();
            finalCertificateAuthority = details.certificateAuthority();
        } else {
            finalApiServerUrl = apiServerUrl;
            finalCertificateAuthority = certificateAuthority;
        }

        final AuthProvider finalAuthProvider = buildAuthProvider();

        final KubernetesClientConfig config = KubernetesClientConfig.builder()
                .apiServerUrl(finalApiServerUrl)
                .authProvider(finalAuthProvider)
                .certificateAuthority(finalCertificateAuthority)
                .skipTlsVerify(this.skipTlsVerify)
                .connectTimeout(this.connectTimeout)
                .readTimeout(this.readTimeout)
                .namespace(this.namespace)
                .build();

        this.kubernetesClient = new KubernetesClient(config);
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

    private ClusterDetails discoverClusterDetails() {
        final TokenCredential discoveryCredential = resolveTokenCredential();
        final AzureProfile profile = new AzureProfile(null, subscriptionId, AzureEnvironment.AZURE);
        try {
            final ContainerServiceManager manager =
                    ContainerServiceManager.authenticate(discoveryCredential, profile);
            final KubernetesCluster cluster = manager.kubernetesClusters()
                    .getByResourceGroup(resourceGroupName, clusterName);
            final String endpoint = "https://" + cluster.fqdn();
            final List<CredentialResult> kubeConfigs = cluster.userKubeConfigs();
            final List<byte[]> kubeConfigBytes = kubeConfigs.stream()
                    .map(CredentialResult::value)
                    .toList();
            final String caCert = extractCaCertificate(kubeConfigBytes);
            return new ClusterDetails(endpoint, caCert);
        } catch (Exception e) {
            throw new IllegalStateException(
                    String.format("Failed to discover AKS cluster: %s/%s", resourceGroupName, clusterName), e);
        }
    }

    private TokenCredential resolveTokenCredential() {
        if (credential != null) {
            return credential;
        }
        if (tenantId != null && clientId != null && clientSecret != null) {
            return new com.azure.identity.ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();
        }
        if (managedIdentityClientId != null) {
            return new com.azure.identity.ManagedIdentityCredentialBuilder()
                    .clientId(managedIdentityClientId)
                    .build();
        }
        return new DefaultAzureCredentialBuilder().build();
    }

    static String extractCaCertificate(final List<byte[]> kubeConfigs) {
        if (kubeConfigs == null || kubeConfigs.isEmpty()) {
            throw new IllegalStateException("No kubeconfig data returned from AKS cluster");
        }
        final String kubeconfig = new String(kubeConfigs.get(0), StandardCharsets.UTF_8);
        final String marker = "certificate-authority-data:";
        final int startIdx = kubeconfig.indexOf(marker);
        if (startIdx < 0) {
            throw new IllegalStateException("certificate-authority-data not found in kubeconfig");
        }
        final int valueStart = startIdx + marker.length();
        int valueEnd = kubeconfig.indexOf('\n', valueStart);
        if (valueEnd < 0) {
            valueEnd = kubeconfig.length();
        }
        final String base64Ca = kubeconfig.substring(valueStart, valueEnd).trim();
        return new String(Base64.getDecoder().decode(base64Ca), StandardCharsets.UTF_8);
    }

    private AuthProvider buildAuthProvider() {
        if (authProvider != null) {
            return authProvider;
        }
        final AzureAuthProvider.Builder builder = AzureAuthProvider.builder();
        if (credential != null) {
            builder.credential(credential);
        }
        if (tenantId != null) {
            builder.tenantId(tenantId);
        }
        if (clientId != null) {
            builder.clientId(clientId);
        }
        if (clientSecret != null) {
            builder.clientSecret(clientSecret);
        }
        if (managedIdentityClientId != null) {
            builder.managedIdentityClientId(managedIdentityClientId);
        }
        return builder.build();
    }

    public KubernetesClient getKubernetesClient() {
        return kubernetesClient;
    }

    public PodManager pods() {
        return podManager;
    }

    public ServiceManager services() {
        return serviceManager;
    }

    public DeploymentManager deployments() {
        return deploymentManager;
    }

    public DaemonSetManager daemonSets() {
        return daemonSetManager;
    }

    public EventManager events() {
        return eventManager;
    }

    public JobManager jobs() {
        return jobManager;
    }

    public LeaseManager leases() {
        return leaseManager;
    }

    public CronJobManager cronJobs() {
        return cronJobManager;
    }

    public StatefulSetManager statefulSets() {
        return statefulSetManager;
    }

    public ReplicaSetManager replicaSets() {
        return replicaSetManager;
    }

    public IngressManager ingresses() {
        return ingressManager;
    }

    public NetworkPolicyManager networkPolicies() {
        return networkPolicyManager;
    }

    public HorizontalPodAutoscalerManager horizontalPodAutoscalers() {
        return horizontalPodAutoscalerManager;
    }

    public VerticalPodAutoscalerManager verticalPodAutoscalers() {
        return verticalPodAutoscalerManager;
    }

    public LimitRangeManager limitRanges() {
        return limitRangeManager;
    }

    public PodDisruptionBudgetManager podDisruptionBudgets() {
        return podDisruptionBudgetManager;
    }

    public ServiceAccountManager serviceAccounts() {
        return serviceAccountManager;
    }

    public RoleManager roles() {
        return roleManager;
    }

    public RoleBindingManager roleBindings() {
        return roleBindingManager;
    }

    public ClusterRoleManager clusterRoles() {
        return clusterRoleManager;
    }

    public ClusterRoleBindingManager clusterRoleBindings() {
        return clusterRoleBindingManager;
    }

    public PersistentVolumeManager persistentVolumes() {
        return persistentVolumeManager;
    }

    public PersistentVolumeClaimManager persistentVolumeClaims() {
        return persistentVolumeClaimManager;
    }

    public ConfigMapManager configMaps() {
        return configMapManager;
    }

    public SecretManager secrets() {
        return secretManager;
    }

    public ResourceQuotaManager resourceQuotas() {
        return resourceQuotaManager;
    }

    public NamespaceManager namespaces() {
        return namespaceManager;
    }

    public CustomResourceDefinitionManager customResourceDefinitions() {
        return customResourceDefinitionManager;
    }

    public GenericResourceManager genericResources(final String group,
                                                    final String version,
                                                    final String kind,
                                                    final String plural) {
        return new GenericResourceManager(kubernetesClient, group, version, kind, plural);
    }

    public GenericResourceManager genericResources(final GenericResourceContext context) {
        return new GenericResourceManager(kubernetesClient, context);
    }

    public GenericClusterResourceManager genericClusterResources(final String group,
                                                                  final String version,
                                                                  final String kind,
                                                                  final String plural) {
        return new GenericClusterResourceManager(kubernetesClient, group, version, kind, plural);
    }

    public GenericClusterResourceManager genericClusterResources(final GenericResourceContext context) {
        return new GenericClusterResourceManager(kubernetesClient, context);
    }

    public DynamicClient dynamic() {
        return new DefaultDynamicClient(kubernetesClient);
    }

    @Override
    public void close() {
        if (kubernetesClient != null) {
            kubernetesClient.close();
        }
    }

    private record ClusterDetails(String endpoint, String certificateAuthority) {}
}
