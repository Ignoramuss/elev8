package io.elev8.resources.clusterrolebinding;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterRoleBindingManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final ClusterRoleBindingManager manager = new ClusterRoleBindingManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final ClusterRoleBindingManager manager = new ClusterRoleBindingManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("clusterrolebindings");
    }
}
