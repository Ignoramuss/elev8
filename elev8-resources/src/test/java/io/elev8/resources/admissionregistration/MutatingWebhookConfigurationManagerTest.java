package io.elev8.resources.admissionregistration;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class MutatingWebhookConfigurationManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final MutatingWebhookConfigurationManager manager = new MutatingWebhookConfigurationManager(client);

        assertThat(manager).isNotNull();
        assertThat(manager.getApiPath()).isEqualTo("/apis/admissionregistration.k8s.io/v1");
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final MutatingWebhookConfigurationManager manager = new MutatingWebhookConfigurationManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("mutatingwebhookconfigurations");
    }
}
