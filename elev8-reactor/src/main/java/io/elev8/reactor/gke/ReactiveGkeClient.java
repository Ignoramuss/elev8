package io.elev8.reactor.gke;

import com.google.auth.oauth2.GoogleCredentials;
import io.elev8.gke.GkeClient;
import io.elev8.reactor.AbstractReactiveCloudKubernetesClient;
import lombok.Getter;

/**
 * Reactive GKE client providing non-blocking access to Kubernetes resources on Google Cloud GKE.
 * Wraps a GkeClient and exposes reactive resource managers using Project Reactor.
 */
public final class ReactiveGkeClient extends AbstractReactiveCloudKubernetesClient {

    @Getter
    private final GkeClient gkeDelegate;

    private ReactiveGkeClient(final GkeClient delegate) {
        super(delegate);
        this.gkeDelegate = delegate;
    }

    /**
     * Create a ReactiveGkeClient from an existing GkeClient.
     *
     * @param delegate the GkeClient to wrap
     * @return a new ReactiveGkeClient
     */
    public static ReactiveGkeClient from(final GkeClient delegate) {
        return new ReactiveGkeClient(delegate);
    }

    /**
     * Get a builder for creating a new ReactiveGkeClient.
     *
     * @return a new ReactiveGkeClientBuilder
     */
    public static ReactiveGkeClientBuilder builder() {
        return new ReactiveGkeClientBuilder();
    }

    /**
     * Get the GCP project ID.
     *
     * @return the project ID
     */
    public String getProjectId() {
        return gkeDelegate.getProjectId();
    }

    /**
     * Get the GKE cluster location (region or zone).
     *
     * @return the location
     */
    public String getLocation() {
        return gkeDelegate.getLocation();
    }

    /**
     * Get the GKE cluster name.
     *
     * @return the cluster name
     */
    public String getClusterName() {
        return gkeDelegate.getClusterName();
    }

    /**
     * Builder for ReactiveGkeClient that delegates to GkeClient.Builder.
     */
    public static final class ReactiveGkeClientBuilder {
        private final GkeClient.GkeClientBuilder delegateBuilder = GkeClient.builder();

        public ReactiveGkeClientBuilder projectId(final String projectId) {
            delegateBuilder.projectId(projectId);
            return this;
        }

        public ReactiveGkeClientBuilder location(final String location) {
            delegateBuilder.location(location);
            return this;
        }

        public ReactiveGkeClientBuilder clusterName(final String clusterName) {
            delegateBuilder.clusterName(clusterName);
            return this;
        }

        public ReactiveGkeClientBuilder apiServerUrl(final String apiServerUrl) {
            delegateBuilder.apiServerUrl(apiServerUrl);
            return this;
        }

        public ReactiveGkeClientBuilder certificateAuthority(final String certificateAuthority) {
            delegateBuilder.certificateAuthority(certificateAuthority);
            return this;
        }

        public ReactiveGkeClientBuilder skipTlsVerify(final Boolean skipTlsVerify) {
            delegateBuilder.skipTlsVerify(skipTlsVerify);
            return this;
        }

        public ReactiveGkeClientBuilder namespace(final String namespace) {
            delegateBuilder.namespace(namespace);
            return this;
        }

        public ReactiveGkeClientBuilder credentials(final GoogleCredentials credentials) {
            delegateBuilder.credentials(credentials);
            return this;
        }

        public ReactiveGkeClientBuilder serviceAccountKeyPath(final String serviceAccountKeyPath) {
            delegateBuilder.serviceAccountKeyPath(serviceAccountKeyPath);
            return this;
        }

        public ReactiveGkeClient build() {
            return new ReactiveGkeClient(delegateBuilder.build());
        }
    }
}
