package io.elev8.resources.poddisruptionbudget;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class PodDisruptionBudgetManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final PodDisruptionBudgetManager manager = new PodDisruptionBudgetManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final PodDisruptionBudgetManager manager = new PodDisruptionBudgetManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("poddisruptionbudgets");
    }
}
