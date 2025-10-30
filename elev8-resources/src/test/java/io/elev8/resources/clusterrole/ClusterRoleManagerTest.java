package io.elev8.resources.clusterrole;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterRoleManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final ClusterRoleManager manager = new ClusterRoleManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final ClusterRoleManager manager = new ClusterRoleManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("clusterroles");
    }
}
