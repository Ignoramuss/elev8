package io.elev8.resources.serviceaccount;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceAccountManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final ServiceAccountManager manager = new ServiceAccountManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final ServiceAccountManager manager = new ServiceAccountManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("serviceaccounts");
    }
}
