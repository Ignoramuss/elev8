package io.elev8.reactor.aks;

import com.azure.core.credential.TokenCredential;
import io.elev8.aks.AksClient;
import io.elev8.reactor.AbstractReactiveCloudKubernetesClient;
import lombok.Getter;

/**
 * Reactive AKS client providing non-blocking access to Kubernetes resources on Azure AKS.
 * Wraps an AksClient and exposes reactive resource managers using Project Reactor.
 */
public final class ReactiveAksClient extends AbstractReactiveCloudKubernetesClient {

    @Getter
    private final AksClient aksDelegate;

    private ReactiveAksClient(final AksClient delegate) {
        super(delegate);
        this.aksDelegate = delegate;
    }

    /**
     * Create a ReactiveAksClient from an existing AksClient.
     *
     * @param delegate the AksClient to wrap
     * @return a new ReactiveAksClient
     */
    public static ReactiveAksClient from(final AksClient delegate) {
        return new ReactiveAksClient(delegate);
    }

    /**
     * Get a builder for creating a new ReactiveAksClient.
     *
     * @return a new ReactiveAksClientBuilder
     */
    public static ReactiveAksClientBuilder builder() {
        return new ReactiveAksClientBuilder();
    }

    /**
     * Get the Azure subscription ID.
     *
     * @return the subscription ID
     */
    public String getSubscriptionId() {
        return aksDelegate.getSubscriptionId();
    }

    /**
     * Get the Azure resource group name.
     *
     * @return the resource group name
     */
    public String getResourceGroupName() {
        return aksDelegate.getResourceGroupName();
    }

    /**
     * Get the AKS cluster name.
     *
     * @return the cluster name
     */
    public String getClusterName() {
        return aksDelegate.getClusterName();
    }

    /**
     * Builder for ReactiveAksClient that delegates to AksClient.Builder.
     */
    public static final class ReactiveAksClientBuilder {
        private final AksClient.AksClientBuilder delegateBuilder = AksClient.builder();

        public ReactiveAksClientBuilder subscriptionId(final String subscriptionId) {
            delegateBuilder.subscriptionId(subscriptionId);
            return this;
        }

        public ReactiveAksClientBuilder resourceGroupName(final String resourceGroupName) {
            delegateBuilder.resourceGroupName(resourceGroupName);
            return this;
        }

        public ReactiveAksClientBuilder clusterName(final String clusterName) {
            delegateBuilder.clusterName(clusterName);
            return this;
        }

        public ReactiveAksClientBuilder apiServerUrl(final String apiServerUrl) {
            delegateBuilder.apiServerUrl(apiServerUrl);
            return this;
        }

        public ReactiveAksClientBuilder certificateAuthority(final String certificateAuthority) {
            delegateBuilder.certificateAuthority(certificateAuthority);
            return this;
        }

        public ReactiveAksClientBuilder skipTlsVerify(final Boolean skipTlsVerify) {
            delegateBuilder.skipTlsVerify(skipTlsVerify);
            return this;
        }

        public ReactiveAksClientBuilder namespace(final String namespace) {
            delegateBuilder.namespace(namespace);
            return this;
        }

        public ReactiveAksClientBuilder credential(final TokenCredential credential) {
            delegateBuilder.credential(credential);
            return this;
        }

        public ReactiveAksClientBuilder tenantId(final String tenantId) {
            delegateBuilder.tenantId(tenantId);
            return this;
        }

        public ReactiveAksClientBuilder clientId(final String clientId) {
            delegateBuilder.clientId(clientId);
            return this;
        }

        public ReactiveAksClientBuilder clientSecret(final String clientSecret) {
            delegateBuilder.clientSecret(clientSecret);
            return this;
        }

        public ReactiveAksClientBuilder managedIdentityClientId(final String managedIdentityClientId) {
            delegateBuilder.managedIdentityClientId(managedIdentityClientId);
            return this;
        }

        public ReactiveAksClient build() {
            return new ReactiveAksClient(delegateBuilder.build());
        }
    }
}
