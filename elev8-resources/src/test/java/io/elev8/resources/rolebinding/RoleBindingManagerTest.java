package io.elev8.resources.rolebinding;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class RoleBindingManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final RoleBindingManager manager = new RoleBindingManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final RoleBindingManager manager = new RoleBindingManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("rolebindings");
    }
}
