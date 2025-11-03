package io.elev8.resources.volumesnapshotcontent;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class VolumeSnapshotContentManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final VolumeSnapshotContentManager manager = new VolumeSnapshotContentManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final VolumeSnapshotContentManager manager = new VolumeSnapshotContentManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("volumesnapshotcontents");
    }
}
