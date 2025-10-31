package io.elev8.resources.networkpolicy;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkPolicyManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final NetworkPolicyManager manager = new NetworkPolicyManager(client);

        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final NetworkPolicyManager manager = new NetworkPolicyManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("networkpolicies");
    }
}
