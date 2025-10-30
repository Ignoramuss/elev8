package io.elev8.resources.persistentvolumeclaim;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class PersistentVolumeClaimManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final PersistentVolumeClaimManager manager = new PersistentVolumeClaimManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final PersistentVolumeClaimManager manager = new PersistentVolumeClaimManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("persistentvolumeclaims");
    }
}
