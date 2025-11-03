package io.elev8.resources.volumesnapshotclass;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class VolumeSnapshotClassManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final VolumeSnapshotClassManager manager = new VolumeSnapshotClassManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final VolumeSnapshotClassManager manager = new VolumeSnapshotClassManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("volumesnapshotclasses");
    }
}
