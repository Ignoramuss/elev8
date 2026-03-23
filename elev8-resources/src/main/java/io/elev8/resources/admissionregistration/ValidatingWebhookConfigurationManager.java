package io.elev8.resources.admissionregistration;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractClusterResourceManager;

public final class ValidatingWebhookConfigurationManager
        extends AbstractClusterResourceManager<ValidatingWebhookConfiguration> {

    public ValidatingWebhookConfigurationManager(final KubernetesClient client) {
        super(client, ValidatingWebhookConfiguration.class, "/apis/admissionregistration.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "validatingwebhookconfigurations";
    }
}
