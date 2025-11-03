package io.elev8.resources.volumesnapshot;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class VolumeSnapshotManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final VolumeSnapshotManager manager = new VolumeSnapshotManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final VolumeSnapshotManager manager = new VolumeSnapshotManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("volumesnapshots");
    }
}
