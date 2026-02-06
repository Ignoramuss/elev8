package io.elev8.eks;

import io.elev8.auth.accessentries.AccessEntryManager;
import io.elev8.auth.iam.IamAuthProvider;
import io.elev8.auth.oidc.OidcAuthProvider;
import io.elev8.cloud.AbstractCloudKubernetesClient;
import io.elev8.core.auth.AuthProvider;
import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientConfig;
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
public class EksClient extends AbstractCloudKubernetesClient {

    @Getter
    private final String clusterName;

    @Getter
    private final String region;

    private final AccessEntryManager accessEntryManager;
    private final StsClient stsClient;

    private static final boolean DEFAULT_SKIP_TLS_VERIFY = false;
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);
    private static final String DEFAULT_NAMESPACE = "default";
    private static final String DEFAULT_SESSION_NAME = "elev8-eks-session";

    @lombok.Builder
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
        this(init(clusterName, region, apiServerUrl, certificateAuthority, skipTlsVerify, connectTimeout, readTimeout,
                namespace, roleArn, sessionName, baseCredentialsProvider, useOidcAuth, oidcRoleArn,
                oidcWebIdentityTokenFile, oidcRoleSessionName), clusterName, region);
    }

    private EksClient(final InitResult init, final String clusterName, final String region) {
        super(init.kubernetesClient());
        this.clusterName = clusterName;
        this.region = region;
        this.stsClient = init.stsClient();
        this.accessEntryManager = AccessEntryManager.builder()
                .clusterName(clusterName)
                .region(region)
                .build();
    }

    private static InitResult init(final String clusterName,
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

        final boolean effectiveUseOidc = useOidcAuth != null && useOidcAuth;
        final AuthComponents authComponents = buildAuthProvider(clusterName, region, roleArn, sessionName,
                baseCredentialsProvider, effectiveUseOidc, oidcRoleArn, oidcWebIdentityTokenFile, oidcRoleSessionName);

        final boolean effectiveSkipTlsVerify = skipTlsVerify != null ? skipTlsVerify : DEFAULT_SKIP_TLS_VERIFY;
        final Duration effectiveConnectTimeout = connectTimeout != null ? connectTimeout : DEFAULT_CONNECT_TIMEOUT;
        final Duration effectiveReadTimeout = readTimeout != null ? readTimeout : DEFAULT_READ_TIMEOUT;
        final String effectiveNamespace = namespace != null ? namespace : DEFAULT_NAMESPACE;

        final KubernetesClientConfig config = KubernetesClientConfig.builder()
                .apiServerUrl(finalApiServerUrl)
                .authProvider(authComponents.authProvider())
                .certificateAuthority(finalCertificateAuthority)
                .skipTlsVerify(effectiveSkipTlsVerify)
                .connectTimeout(effectiveConnectTimeout)
                .readTimeout(effectiveReadTimeout)
                .namespace(effectiveNamespace)
                .build();

        return new InitResult(new KubernetesClient(config), authComponents.stsClient());
    }

    private static ClusterDetails discoverClusterDetails(final Region region, final String clusterName) {
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

    private static AuthComponents buildAuthProvider(final String clusterName,
                                                    final String region,
                                                    final String roleArn,
                                                    final String sessionName,
                                                    final AwsCredentialsProvider baseCredentialsProvider,
                                                    final boolean useOidcAuth,
                                                    final String oidcRoleArn,
                                                    final String oidcWebIdentityTokenFile,
                                                    final String oidcRoleSessionName) {
        if (useOidcAuth) {
            return buildOidcAuthProvider(clusterName, region, oidcRoleArn, oidcWebIdentityTokenFile, oidcRoleSessionName);
        } else {
            return buildIamAuthProvider(clusterName, region, roleArn, sessionName, baseCredentialsProvider);
        }
    }

    private static AuthComponents buildIamAuthProvider(final String clusterName,
                                                       final String region,
                                                       final String roleArn,
                                                       final String sessionName,
                                                       final AwsCredentialsProvider baseCredentialsProvider) {
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
            final String effectiveSessionName = sessionName != null ? sessionName : DEFAULT_SESSION_NAME;

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

    private static AuthComponents buildOidcAuthProvider(final String clusterName,
                                                        final String region,
                                                        final String oidcRoleArn,
                                                        final String oidcWebIdentityTokenFile,
                                                        final String oidcRoleSessionName) {
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

    public AccessEntryManager accessEntries() {
        return accessEntryManager;
    }

    @Override
    public void close() {
        super.close();
        if (accessEntryManager != null) {
            accessEntryManager.close();
        }
        if (stsClient != null) {
            stsClient.close();
        }
    }

    private record InitResult(KubernetesClient kubernetesClient, StsClient stsClient) {}
    private record AuthComponents(AuthProvider authProvider, StsClient stsClient) {}
    private record ClusterDetails(String endpoint, String certificateAuthority) {}
}
