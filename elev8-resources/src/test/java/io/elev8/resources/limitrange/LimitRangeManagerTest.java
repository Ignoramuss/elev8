package io.elev8.resources.limitrange;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class LimitRangeManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final LimitRangeManager manager = new LimitRangeManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final LimitRangeManager manager = new LimitRangeManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("limitranges");
    }
}
