package io.elev8.reactor.gke;

import io.elev8.gke.GkeClient;
import io.elev8.reactor.ReactiveCloudKubernetesClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveGkeClientTest {

    @Mock
    private GkeClient mockGkeClient;

    @Test
    void shouldThrowExceptionWhenProjectIdIsNull() {
        assertThatThrownBy(() -> ReactiveGkeClient.builder()
                .location("us-central1")
                .clusterName("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Project ID is required");
    }

    @Test
    void shouldThrowExceptionWhenLocationIsNull() {
        assertThatThrownBy(() -> ReactiveGkeClient.builder()
                .projectId("my-project")
                .clusterName("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Location is required");
    }

    @Test
    void shouldThrowExceptionWhenClusterNameIsNull() {
        assertThatThrownBy(() -> ReactiveGkeClient.builder()
                .projectId("my-project")
                .location("us-central1")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cluster name is required");
    }

    @Test
    void shouldWrapExistingGkeClient() {
        when(mockGkeClient.getProjectId()).thenReturn("my-project");
        when(mockGkeClient.getLocation()).thenReturn("us-central1");
        when(mockGkeClient.getClusterName()).thenReturn("test-cluster");

        final ReactiveGkeClient reactiveClient = ReactiveGkeClient.from(mockGkeClient);

        assertThat(reactiveClient).isNotNull();
        assertThat(reactiveClient).isInstanceOf(ReactiveCloudKubernetesClient.class);
        assertThat(reactiveClient.getGkeDelegate()).isSameAs(mockGkeClient);
        assertThat(reactiveClient.getDelegate()).isSameAs(mockGkeClient);
        assertThat(reactiveClient.getProjectId()).isEqualTo("my-project");
        assertThat(reactiveClient.getLocation()).isEqualTo("us-central1");
        assertThat(reactiveClient.getClusterName()).isEqualTo("test-cluster");
    }
}
