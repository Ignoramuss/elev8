package io.elev8.resources.resourcequota;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceQuotaManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final ResourceQuotaManager manager = new ResourceQuotaManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final ResourceQuotaManager manager = new ResourceQuotaManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("resourcequotas");
    }
}
