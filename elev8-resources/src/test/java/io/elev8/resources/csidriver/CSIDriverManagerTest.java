package io.elev8.resources.csidriver;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class CSIDriverManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final CSIDriverManager manager = new CSIDriverManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final CSIDriverManager manager = new CSIDriverManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("csidrivers");
    }
}
