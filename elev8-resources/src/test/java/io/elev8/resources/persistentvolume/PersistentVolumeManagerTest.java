package io.elev8.resources.persistentvolume;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class PersistentVolumeManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final PersistentVolumeManager manager = new PersistentVolumeManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final PersistentVolumeManager manager = new PersistentVolumeManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("persistentvolumes");
    }
}
