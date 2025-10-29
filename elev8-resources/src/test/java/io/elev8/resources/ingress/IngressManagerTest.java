package io.elev8.resources.ingress;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class IngressManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final IngressManager manager = new IngressManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final IngressManager manager = new IngressManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("ingresses");
    }
}
