package io.elev8.eks;

import io.elev8.auth.iam.IamAuthProvider;
import io.elev8.core.auth.AuthProvider;
import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientConfig;
import io.elev8.resources.deployment.DeploymentManager;
import io.elev8.resources.pod.PodManager;
import io.elev8.resources.service.ServiceManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eks.model.Cluster;
import software.amazon.awssdk.services.eks.model.DescribeClusterRequest;
import software.amazon.awssdk.services.eks.model.DescribeClusterResponse;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * EKS-optimized Kubernetes client with native AWS IAM authentication.
 */
@Slf4j
public final class EksClient implements AutoCloseable {

    private final KubernetesClient kubernetesClient;

    @Getter
    private final String clusterName;

    @Getter
    private final Region region;

    private final PodManager podManager;
    private final ServiceManager serviceManager;
    private final DeploymentManager deploymentManager;

    private EksClient(final Builder builder) {
        this.clusterName = builder.clusterName;
        this.region = builder.region;

        String apiServerUrl = builder.apiServerUrl;
        String certificateAuthority = builder.certificateAuthority;

        if (apiServerUrl == null || certificateAuthority == null) {
            log.debug("Auto-discovering EKS cluster details for: {}", clusterName);
            final ClusterDetails details = discoverClusterDetails(builder.region, builder.clusterName);
            apiServerUrl = details.endpoint();
            certificateAuthority = details.certificateAuthority();
        }

        final KubernetesClientConfig config = KubernetesClientConfig.builder()
                .apiServerUrl(apiServerUrl)
                .authProvider(builder.authProvider)
                .certificateAuthority(certificateAuthority)
                .skipTlsVerify(builder.skipTlsVerify)
                .connectTimeout(builder.connectTimeout)
                .readTimeout(builder.readTimeout)
                .namespace(builder.namespace)
                .build();

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
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String clusterName;
        private Region region;
        private String apiServerUrl;
        private String certificateAuthority;
        private AuthProvider authProvider;
        private boolean skipTlsVerify = false;
        private Duration connectTimeout = Duration.ofSeconds(30);
        private Duration readTimeout = Duration.ofSeconds(30);
        private String namespace = "default";

        public Builder cluster(String clusterName) {
            this.clusterName = clusterName;
            return this;
        }

        public Builder region(String region) {
            this.region = Region.of(region);
            return this;
        }

        public Builder region(Region region) {
            this.region = region;
            return this;
        }

        public Builder apiServerUrl(String apiServerUrl) {
            this.apiServerUrl = apiServerUrl;
            return this;
        }

        public Builder certificateAuthority(String certificateAuthority) {
            this.certificateAuthority = certificateAuthority;
            return this;
        }

        public Builder skipTlsVerify(boolean skipTlsVerify) {
            this.skipTlsVerify = skipTlsVerify;
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        /**
         * Use IAM authentication with default credentials provider.
         *
         * @return this builder
         */
        public Builder iamAuth() {
            if (clusterName == null) {
                throw new IllegalStateException("Cluster name must be set before configuring IAM auth");
            }
            if (region == null) {
                throw new IllegalStateException("Region must be set before configuring IAM auth");
            }

            this.authProvider = IamAuthProvider.builder()
                    .clusterName(clusterName)
                    .region(region)
                    .build();
            return this;
        }

        /**
         * Use IAM authentication with custom configuration.
         *
         * @param configurator consumer to configure IAM auth
         * @return this builder
         */
        public Builder iamAuth(Consumer<IamAuthBuilder> configurator) {
            if (clusterName == null) {
                throw new IllegalStateException("Cluster name must be set before configuring IAM auth");
            }
            if (region == null) {
                throw new IllegalStateException("Region must be set before configuring IAM auth");
            }

            IamAuthBuilder iamBuilder = new IamAuthBuilder();
            configurator.accept(iamBuilder);
            this.authProvider = iamBuilder.build(clusterName, region);
            return this;
        }

        /**
         * Use a custom authentication provider.
         *
         * @param authProvider the authentication provider
         * @return this builder
         */
        public Builder authProvider(AuthProvider authProvider) {
            this.authProvider = authProvider;
            return this;
        }

        public EksClient build() {
            if (clusterName == null || clusterName.isEmpty()) {
                throw new IllegalArgumentException("Cluster name is required");
            }
            if (region == null) {
                throw new IllegalArgumentException("Region is required");
            }
            if (authProvider == null) {
                throw new IllegalStateException("Authentication provider must be configured. Call iamAuth() or authProvider()");
            }
            return new EksClient(this);
        }
    }

    public static class IamAuthBuilder {
        private AwsCredentialsProvider credentialsProvider;
        private String roleArn;
        private String sessionName;

        public IamAuthBuilder credentialsProvider(AwsCredentialsProvider credentialsProvider) {
            this.credentialsProvider = credentialsProvider;
            return this;
        }

        public IamAuthBuilder assumeRole(String roleArn) {
            this.roleArn = roleArn;
            return this;
        }

        public IamAuthBuilder sessionName(String sessionName) {
            this.sessionName = sessionName;
            return this;
        }

        private IamAuthProvider build(String clusterName, Region region) {
            IamAuthProvider.Builder builder = IamAuthProvider.builder()
                    .clusterName(clusterName)
                    .region(region);

            if (credentialsProvider != null) {
                builder.credentialsProvider(credentialsProvider);
            }

            // TODO: Add assume role support
            if (roleArn != null) {
                log.warn("Assume role support not yet implemented, using provided credentials provider");
            }

            return builder.build();
        }
    }

    private record ClusterDetails(String endpoint, String certificateAuthority) {}
}
