package io.elev8.gke;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.container.v1.ClusterManagerClient;
import io.elev8.auth.gcp.GcpAuthProvider;
import io.elev8.cloud.AbstractCloudKubernetesClient;
import io.elev8.core.auth.AuthProvider;
import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * GKE-optimized Kubernetes client with native GCP authentication.
 */
@Slf4j
public class GkeClient extends AbstractCloudKubernetesClient {

    @Getter
    private final String projectId;

    @Getter
    private final String location;

    @Getter
    private final String clusterName;

    private static final boolean DEFAULT_SKIP_TLS_VERIFY = false;
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);
    private static final String DEFAULT_NAMESPACE = "default";

    @lombok.Builder
    private GkeClient(final String projectId,
                      final String location,
                      final String clusterName,
                      final String apiServerUrl,
                      final String certificateAuthority,
                      final Boolean skipTlsVerify,
                      final Duration connectTimeout,
                      final Duration readTimeout,
                      final String namespace,
                      final AuthProvider authProvider,
                      final GoogleCredentials credentials,
                      final String serviceAccountKeyPath) {
        super(buildKubernetesClient(projectId, location, clusterName, apiServerUrl, certificateAuthority,
                skipTlsVerify, connectTimeout, readTimeout, namespace, authProvider, credentials, serviceAccountKeyPath));
        this.projectId = projectId;
        this.location = location;
        this.clusterName = clusterName;
    }

    private static KubernetesClient buildKubernetesClient(final String projectId,
                                                          final String location,
                                                          final String clusterName,
                                                          final String apiServerUrl,
                                                          final String certificateAuthority,
                                                          final Boolean skipTlsVerify,
                                                          final Duration connectTimeout,
                                                          final Duration readTimeout,
                                                          final String namespace,
                                                          final AuthProvider authProvider,
                                                          final GoogleCredentials credentials,
                                                          final String serviceAccountKeyPath) {
        if (projectId == null || projectId.isEmpty()) {
            throw new IllegalArgumentException("Project ID is required");
        }
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }
        if (clusterName == null || clusterName.isEmpty()) {
            throw new IllegalArgumentException("Cluster name is required");
        }

        final String finalApiServerUrl;
        final String finalCertificateAuthority;

        if (apiServerUrl == null || certificateAuthority == null) {
            log.debug("Auto-discovering GKE cluster details for: {}", clusterName);
            final ClusterDetails details = discoverClusterDetails(projectId, location, clusterName);
            finalApiServerUrl = details.endpoint();
            finalCertificateAuthority = details.certificateAuthority();
        } else {
            finalApiServerUrl = apiServerUrl;
            finalCertificateAuthority = certificateAuthority;
        }

        final AuthProvider finalAuthProvider = buildAuthProvider(authProvider, credentials, serviceAccountKeyPath);

        final boolean effectiveSkipTlsVerify = skipTlsVerify != null ? skipTlsVerify : DEFAULT_SKIP_TLS_VERIFY;
        final Duration effectiveConnectTimeout = connectTimeout != null ? connectTimeout : DEFAULT_CONNECT_TIMEOUT;
        final Duration effectiveReadTimeout = readTimeout != null ? readTimeout : DEFAULT_READ_TIMEOUT;
        final String effectiveNamespace = namespace != null ? namespace : DEFAULT_NAMESPACE;

        final KubernetesClientConfig config = KubernetesClientConfig.builder()
                .apiServerUrl(finalApiServerUrl)
                .authProvider(finalAuthProvider)
                .certificateAuthority(finalCertificateAuthority)
                .skipTlsVerify(effectiveSkipTlsVerify)
                .connectTimeout(effectiveConnectTimeout)
                .readTimeout(effectiveReadTimeout)
                .namespace(effectiveNamespace)
                .build();

        return new KubernetesClient(config);
    }

    private static ClusterDetails discoverClusterDetails(final String projectId,
                                                         final String location,
                                                         final String clusterName) {
        final String clusterPath = String.format("projects/%s/locations/%s/clusters/%s",
                projectId, location, clusterName);
        try (final ClusterManagerClient clusterManagerClient = ClusterManagerClient.create()) {
            final com.google.container.v1.Cluster cluster =
                    clusterManagerClient.getCluster(clusterPath);
            return new ClusterDetails(
                    "https://" + cluster.getEndpoint(),
                    cluster.getMasterAuth().getClusterCaCertificate()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to discover GKE cluster: " + clusterPath, e);
        }
    }

    private static AuthProvider buildAuthProvider(final AuthProvider authProvider,
                                                  final GoogleCredentials credentials,
                                                  final String serviceAccountKeyPath) {
        if (authProvider != null) {
            return authProvider;
        }
        final GcpAuthProvider.Builder builder = GcpAuthProvider.builder();
        if (credentials != null) {
            builder.credentials(credentials);
        }
        if (serviceAccountKeyPath != null) {
            builder.serviceAccountKeyPath(serviceAccountKeyPath);
        }
        return builder.build();
    }

    private record ClusterDetails(String endpoint, String certificateAuthority) {}
}
