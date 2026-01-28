package io.elev8.eks;

import io.elev8.auth.accessentries.AccessEntryManager;
import io.elev8.auth.iam.IamAuthProvider;
import io.elev8.auth.oidc.OidcAuthProvider;
import io.elev8.core.auth.AuthProvider;
import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientConfig;
import io.elev8.resources.configmap.ConfigMapManager;
import io.elev8.resources.crd.CustomResourceDefinitionManager;
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
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eks.model.Cluster;
import software.amazon.awssdk.services.eks.model.DescribeClusterRequest;
import software.amazon.awssdk.services.eks.model.DescribeClusterResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

import java.time.Duration;

/**
 * EKS-optimized Kubernetes client with native AWS IAM authentication.
 */
@Slf4j
public final class EksClient implements AutoCloseable {

    private final KubernetesClient kubernetesClient;

    @Getter
    private final String clusterName;

    @Getter
    private final String region;

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
    private final AccessEntryManager accessEntryManager;
    private final StsClient stsClient;

    private final boolean skipTlsVerify;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final String namespace;
    private final String apiServerUrl;
    private final String certificateAuthority;
    private final String roleArn;
    private final String sessionName;
    private final AwsCredentialsProvider baseCredentialsProvider;
    private final boolean useOidcAuth;
    private final String oidcRoleArn;
    private final String oidcWebIdentityTokenFile;
    private final String oidcRoleSessionName;

    private static final boolean DEFAULT_SKIP_TLS_VERIFY = false;
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);
    private static final String DEFAULT_NAMESPACE = "default";
    private static final String DEFAULT_SESSION_NAME = "elev8-eks-session";

    @lombok.Builder(toBuilder = true)
    private EksClient(final String clusterName,
                      final String region,
                      final String apiServerUrl,
                      final String certificateAuthority,
                      final Boolean skipTlsVerify,
                      final Duration connectTimeout,
                      final Duration readTimeout,
                      final String namespace,
                      final String roleArn,
                      final String sessionName,
                      final AwsCredentialsProvider baseCredentialsProvider,
                      final Boolean useOidcAuth,
                      final String oidcRoleArn,
                      final String oidcWebIdentityTokenFile,
                      final String oidcRoleSessionName) {

        if (clusterName == null || clusterName.isEmpty()) {
            throw new IllegalArgumentException("Cluster name is required");
        }
        if (region == null || region.isEmpty()) {
            throw new IllegalArgumentException("Region is required");
        }

        this.clusterName = clusterName;
        this.region = region;
        this.apiServerUrl = apiServerUrl;
        this.certificateAuthority = certificateAuthority;
        this.skipTlsVerify = skipTlsVerify != null ? skipTlsVerify : DEFAULT_SKIP_TLS_VERIFY;
        this.connectTimeout = connectTimeout != null ? connectTimeout : DEFAULT_CONNECT_TIMEOUT;
        this.readTimeout = readTimeout != null ? readTimeout : DEFAULT_READ_TIMEOUT;
        this.namespace = namespace != null ? namespace : DEFAULT_NAMESPACE;
        this.roleArn = roleArn;
        this.sessionName = sessionName != null ? sessionName : DEFAULT_SESSION_NAME;
        this.baseCredentialsProvider = baseCredentialsProvider;
        this.useOidcAuth = useOidcAuth != null && useOidcAuth;
        this.oidcRoleArn = oidcRoleArn;
        this.oidcWebIdentityTokenFile = oidcWebIdentityTokenFile;
        this.oidcRoleSessionName = oidcRoleSessionName;

        final String finalApiServerUrl;
        final String finalCertificateAuthority;

        if (apiServerUrl == null || certificateAuthority == null) {
            log.debug("Auto-discovering EKS cluster details for: {}", clusterName);
            final ClusterDetails details = discoverClusterDetails(Region.of(region), clusterName);
            finalApiServerUrl = details.endpoint();
            finalCertificateAuthority = details.certificateAuthority();
        } else {
            finalApiServerUrl = apiServerUrl;
            finalCertificateAuthority = certificateAuthority;
        }

        final AuthComponents authComponents = buildAuthProvider();

        final KubernetesClientConfig config = KubernetesClientConfig.builder()
                .apiServerUrl(finalApiServerUrl)
                .authProvider(authComponents.authProvider())
                .certificateAuthority(finalCertificateAuthority)
                .skipTlsVerify(this.skipTlsVerify)
                .connectTimeout(this.connectTimeout)
                .readTimeout(this.readTimeout)
                .namespace(this.namespace)
                .build();

        this.stsClient = authComponents.stsClient();
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
        this.accessEntryManager = AccessEntryManager.builder()
                .clusterName(clusterName)
                .region(region)
                .build();
    }

    private ClusterDetails discoverClusterDetails(final Region region, final String clusterName) {
        try (software.amazon.awssdk.services.eks.EksClient eksClient =
                software.amazon.awssdk.services.eks.EksClient.builder().region(region).build()) {
            final DescribeClusterResponse response = eksClient.describeCluster(
                    DescribeClusterRequest.builder().name(clusterName).build());

            final Cluster cluster = response.cluster();
            return new ClusterDetails(
                    cluster.endpoint(),
                    cluster.certificateAuthority().data()
            );
        }
    }

    private AuthComponents buildAuthProvider() {
        if (useOidcAuth) {
            return buildOidcAuthProvider();
        } else {
            return buildIamAuthProvider();
        }
    }

    private AuthComponents buildIamAuthProvider() {
        final IamAuthProvider.Builder builder = IamAuthProvider.builder()
                .clusterName(clusterName)
                .region(Region.of(region));

        final AwsCredentialsProvider finalCredentialsProvider;
        final StsClient createdStsClient;

        if (roleArn != null) {
            final var stsBuilder = StsClient.builder().region(Region.of(region));

            if (baseCredentialsProvider != null) {
                stsBuilder.credentialsProvider(baseCredentialsProvider);
            }

            createdStsClient = stsBuilder.build();
            final String effectiveSessionName = sessionName != null ? sessionName : "elev8-eks-session";

            finalCredentialsProvider = StsAssumeRoleCredentialsProvider.builder()
                    .stsClient(createdStsClient)
                    .refreshRequest(AssumeRoleRequest.builder()
                            .roleArn(roleArn)
                            .roleSessionName(effectiveSessionName)
                            .build())
                    .build();

            log.debug("Configured AssumeRole for {} with session name {}", roleArn, effectiveSessionName);
        } else {
            createdStsClient = null;
            finalCredentialsProvider = baseCredentialsProvider;
        }

        if (finalCredentialsProvider != null) {
            builder.credentialsProvider(finalCredentialsProvider);
        }

        return new AuthComponents(builder.build(), createdStsClient);
    }

    private AuthComponents buildOidcAuthProvider() {
        log.debug("Building OIDC auth provider for cluster: {}", clusterName);

        final OidcAuthProvider.Builder builder = OidcAuthProvider.builder()
                .clusterName(clusterName)
                .region(Region.of(region));

        if (oidcRoleArn != null) {
            builder.roleArn(oidcRoleArn);
        }

        if (oidcWebIdentityTokenFile != null) {
            builder.webIdentityTokenFile(oidcWebIdentityTokenFile);
        }

        if (oidcRoleSessionName != null) {
            builder.roleSessionName(oidcRoleSessionName);
        }

        return new AuthComponents(builder.build(), null);
    }

    private record AuthComponents(AuthProvider authProvider, StsClient stsClient) {}

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

    public AccessEntryManager accessEntries() {
        return accessEntryManager;
    }

    /**
     * Create a manager for namespace-scoped custom resources.
     *
     * @param group the API group (e.g., "stable.example.com")
     * @param version the API version (e.g., "v1")
     * @param kind the resource kind (e.g., "CronTab")
     * @param plural the plural name (e.g., "crontabs")
     * @return a GenericResourceManager for the custom resource
     */
    public GenericResourceManager genericResources(final String group,
                                                   final String version,
                                                   final String kind,
                                                   final String plural) {
        return new GenericResourceManager(kubernetesClient, group, version, kind, plural);
    }

    /**
     * Create a manager for namespace-scoped custom resources using a context.
     *
     * @param context the resource context
     * @return a GenericResourceManager for the custom resource
     */
    public GenericResourceManager genericResources(final GenericResourceContext context) {
        return new GenericResourceManager(kubernetesClient, context);
    }

    /**
     * Create a manager for cluster-scoped custom resources.
     *
     * @param group the API group (e.g., "example.com")
     * @param version the API version (e.g., "v1")
     * @param kind the resource kind (e.g., "ClusterPolicy")
     * @param plural the plural name (e.g., "clusterpolicies")
     * @return a GenericClusterResourceManager for the custom resource
     */
    public GenericClusterResourceManager genericClusterResources(final String group,
                                                                  final String version,
                                                                  final String kind,
                                                                  final String plural) {
        return new GenericClusterResourceManager(kubernetesClient, group, version, kind, plural);
    }

    /**
     * Create a manager for cluster-scoped custom resources using a context.
     *
     * @param context the resource context
     * @return a GenericClusterResourceManager for the custom resource
     */
    public GenericClusterResourceManager genericClusterResources(final GenericResourceContext context) {
        return new GenericClusterResourceManager(kubernetesClient, context);
    }

    @Override
    public void close() {
        if (kubernetesClient != null) {
            kubernetesClient.close();
        }
        if (accessEntryManager != null) {
            accessEntryManager.close();
        }
        if (stsClient != null) {
            stsClient.close();
        }
    }

    private record ClusterDetails(String endpoint, String certificateAuthority) {}
}
