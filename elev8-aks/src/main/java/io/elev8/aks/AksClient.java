package io.elev8.aks;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.containerservice.ContainerServiceManager;
import com.azure.resourcemanager.containerservice.models.CredentialResult;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import io.elev8.auth.azure.AzureAuthProvider;
import io.elev8.cloud.AbstractCloudKubernetesClient;
import io.elev8.core.auth.AuthProvider;
import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientConfig;
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
public class AksClient extends AbstractCloudKubernetesClient {

    @Getter
    private final String subscriptionId;

    @Getter
    private final String resourceGroupName;

    @Getter
    private final String clusterName;

    private static final boolean DEFAULT_SKIP_TLS_VERIFY = false;
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);
    private static final String DEFAULT_NAMESPACE = "default";

    @lombok.Builder
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
        super(buildKubernetesClient(subscriptionId, resourceGroupName, clusterName, apiServerUrl, certificateAuthority,
                skipTlsVerify, connectTimeout, readTimeout, namespace, authProvider, credential,
                tenantId, clientId, clientSecret, managedIdentityClientId));
        this.subscriptionId = subscriptionId;
        this.resourceGroupName = resourceGroupName;
        this.clusterName = clusterName;
    }

    private static KubernetesClient buildKubernetesClient(final String subscriptionId,
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

        final String finalApiServerUrl;
        final String finalCertificateAuthority;

        if (apiServerUrl == null || certificateAuthority == null) {
            log.debug("Auto-discovering AKS cluster details for: {}", clusterName);
            final ClusterDetails details = discoverClusterDetails(subscriptionId, resourceGroupName, clusterName,
                    credential, tenantId, clientId, clientSecret, managedIdentityClientId);
            finalApiServerUrl = details.endpoint();
            finalCertificateAuthority = details.certificateAuthority();
        } else {
            finalApiServerUrl = apiServerUrl;
            finalCertificateAuthority = certificateAuthority;
        }

        final AuthProvider finalAuthProvider = buildAuthProvider(authProvider, credential,
                tenantId, clientId, clientSecret, managedIdentityClientId);

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

    private static ClusterDetails discoverClusterDetails(final String subscriptionId,
                                                         final String resourceGroupName,
                                                         final String clusterName,
                                                         final TokenCredential credential,
                                                         final String tenantId,
                                                         final String clientId,
                                                         final String clientSecret,
                                                         final String managedIdentityClientId) {
        final TokenCredential discoveryCredential = resolveTokenCredential(credential,
                tenantId, clientId, clientSecret, managedIdentityClientId);
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

    private static TokenCredential resolveTokenCredential(final TokenCredential credential,
                                                          final String tenantId,
                                                          final String clientId,
                                                          final String clientSecret,
                                                          final String managedIdentityClientId) {
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

    private static AuthProvider buildAuthProvider(final AuthProvider authProvider,
                                                  final TokenCredential credential,
                                                  final String tenantId,
                                                  final String clientId,
                                                  final String clientSecret,
                                                  final String managedIdentityClientId) {
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

    private record ClusterDetails(String endpoint, String certificateAuthority) {}
}
