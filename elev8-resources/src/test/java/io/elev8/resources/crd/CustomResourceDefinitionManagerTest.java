package io.elev8.resources.crd;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class CustomResourceDefinitionManagerTest {

    @Test
    void shouldInitializeWithCorrectApiPath() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final CustomResourceDefinitionManager manager = new CustomResourceDefinitionManager(client);

        assertThat(manager).isNotNull();
        assertThat(manager.getApiPath()).isEqualTo("/apis/apiextensions.k8s.io/v1");
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        final KubernetesClient client = Mockito.mock(KubernetesClient.class);
        final CustomResourceDefinitionManager manager = new CustomResourceDefinitionManager(client);

        assertThat(manager.getResourceTypePlural()).isEqualTo("customresourcedefinitions");
    }
}
