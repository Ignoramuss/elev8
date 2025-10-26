package io.elev8.eks;

import io.elev8.auth.iam.IamAuthProvider;
import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientConfig;
import io.elev8.resources.deployment.DeploymentManager;
import io.elev8.resources.pod.PodManager;
import io.elev8.resources.service.ServiceManager;
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
                      final AwsCredentialsProvider baseCredentialsProvider) {

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

    private record AuthComponents(IamAuthProvider authProvider, StsClient stsClient) {}

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

    @Override
    public void close() {
        if (kubernetesClient != null) {
            kubernetesClient.close();
        }
        if (stsClient != null) {
            stsClient.close();
        }
    }

    private record ClusterDetails(String endpoint, String certificateAuthority) {}
}
