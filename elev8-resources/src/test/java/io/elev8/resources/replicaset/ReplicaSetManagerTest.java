package io.elev8.resources.replicaset;

import io.elev8.core.client.KubernetesClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class ReplicaSetManagerTest {

    @Mock
    private KubernetesClient kubernetesClient;

    private ReplicaSetManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new ReplicaSetManager(kubernetesClient);
    }

    @Test
    void shouldCreateManagerWithKubernetesClient() {
        assertThat(manager).isNotNull();
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() throws Exception {
        final Method method = manager.getClass().getSuperclass()
                .getDeclaredMethod("getResourceTypePlural");
        method.setAccessible(true);
        final String resourceType = (String) method.invoke(manager);

        assertThat(resourceType).isEqualTo("replicasets");
    }
}
