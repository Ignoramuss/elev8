package io.elev8.resources.admissionregistration;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractClusterResourceManager;

public final class MutatingWebhookConfigurationManager
        extends AbstractClusterResourceManager<MutatingWebhookConfiguration> {

    public MutatingWebhookConfigurationManager(final KubernetesClient client) {
        super(client, MutatingWebhookConfiguration.class, "/apis/admissionregistration.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "mutatingwebhookconfigurations";
    }
}
