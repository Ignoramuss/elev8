package io.elev8.resources.horizontalpodautoscaler;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class HorizontalPodAutoscalerManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final HorizontalPodAutoscalerManager manager = new HorizontalPodAutoscalerManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final HorizontalPodAutoscalerManager manager = new HorizontalPodAutoscalerManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("horizontalpodautoscalers");
    }
}
