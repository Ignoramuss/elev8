package io.elev8.reactor.eks;

import io.elev8.auth.accessentries.AccessEntryManager;
import io.elev8.eks.EksClient;
import io.elev8.reactor.AbstractReactiveCloudKubernetesClient;
import lombok.Getter;

/**
 * Reactive EKS client providing non-blocking access to Kubernetes resources on AWS EKS.
 * Wraps an EksClient and exposes reactive resource managers using Project Reactor.
 */
public final class ReactiveEksClient extends AbstractReactiveCloudKubernetesClient {

    @Getter
    private final EksClient eksDelegate;

    private ReactiveEksClient(final EksClient delegate) {
        super(delegate);
        this.eksDelegate = delegate;
    }

    /**
     * Create a ReactiveEksClient from an existing EksClient.
     *
     * @param delegate the EksClient to wrap
     * @return a new ReactiveEksClient
     */
    public static ReactiveEksClient from(final EksClient delegate) {
        return new ReactiveEksClient(delegate);
    }

    /**
     * Get a builder for creating a new ReactiveEksClient.
     *
     * @return a new ReactiveEksClientBuilder
     */
    public static ReactiveEksClientBuilder builder() {
        return new ReactiveEksClientBuilder();
    }

    /**
     * Get the EKS cluster name.
     *
     * @return the cluster name
     */
    public String getClusterName() {
        return eksDelegate.getClusterName();
    }

    /**
     * Get the AWS region.
     *
     * @return the region
     */
    public String getRegion() {
        return eksDelegate.getRegion();
    }

    /**
     * Get the EKS Access Entries manager for IAM access management.
     *
     * @return the access entry manager
     */
    public AccessEntryManager accessEntries() {
        return eksDelegate.accessEntries();
    }

    /**
     * Builder for ReactiveEksClient that delegates to EksClient.Builder.
     */
    public static final class ReactiveEksClientBuilder {
        private final EksClient.EksClientBuilder delegateBuilder = EksClient.builder();

        public ReactiveEksClientBuilder clusterName(final String clusterName) {
            delegateBuilder.clusterName(clusterName);
            return this;
        }

        public ReactiveEksClientBuilder region(final String region) {
            delegateBuilder.region(region);
            return this;
        }

        public ReactiveEksClientBuilder apiServerUrl(final String apiServerUrl) {
            delegateBuilder.apiServerUrl(apiServerUrl);
            return this;
        }

        public ReactiveEksClientBuilder certificateAuthority(final String certificateAuthority) {
            delegateBuilder.certificateAuthority(certificateAuthority);
            return this;
        }

        public ReactiveEksClientBuilder skipTlsVerify(final Boolean skipTlsVerify) {
            delegateBuilder.skipTlsVerify(skipTlsVerify);
            return this;
        }

        public ReactiveEksClientBuilder namespace(final String namespace) {
            delegateBuilder.namespace(namespace);
            return this;
        }

        public ReactiveEksClientBuilder roleArn(final String roleArn) {
            delegateBuilder.roleArn(roleArn);
            return this;
        }

        public ReactiveEksClientBuilder sessionName(final String sessionName) {
            delegateBuilder.sessionName(sessionName);
            return this;
        }

        public ReactiveEksClientBuilder useOidcAuth(final Boolean useOidcAuth) {
            delegateBuilder.useOidcAuth(useOidcAuth);
            return this;
        }

        public ReactiveEksClientBuilder oidcRoleArn(final String oidcRoleArn) {
            delegateBuilder.oidcRoleArn(oidcRoleArn);
            return this;
        }

        public ReactiveEksClientBuilder oidcWebIdentityTokenFile(final String oidcWebIdentityTokenFile) {
            delegateBuilder.oidcWebIdentityTokenFile(oidcWebIdentityTokenFile);
            return this;
        }

        public ReactiveEksClientBuilder oidcRoleSessionName(final String oidcRoleSessionName) {
            delegateBuilder.oidcRoleSessionName(oidcRoleSessionName);
            return this;
        }

        public ReactiveEksClient build() {
            return new ReactiveEksClient(delegateBuilder.build());
        }
    }
}
