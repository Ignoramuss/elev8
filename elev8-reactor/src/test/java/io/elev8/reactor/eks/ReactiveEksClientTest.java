package io.elev8.reactor.eks;

import io.elev8.eks.EksClient;
import io.elev8.reactor.ReactiveCloudKubernetesClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveEksClientTest {

    @Mock
    private EksClient mockEksClient;

    @Test
    void shouldThrowExceptionWhenClusterNameIsNull() {
        assertThatThrownBy(() -> ReactiveEksClient.builder()
                .region("us-east-1")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cluster name is required");
    }

    @Test
    void shouldThrowExceptionWhenRegionIsNull() {
        assertThatThrownBy(() -> ReactiveEksClient.builder()
                .clusterName("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Region is required");
    }

    @Test
    void shouldWrapExistingEksClient() {
        when(mockEksClient.getClusterName()).thenReturn("test-cluster");
        when(mockEksClient.getRegion()).thenReturn("us-east-1");

        final ReactiveEksClient reactiveClient = ReactiveEksClient.from(mockEksClient);

        assertThat(reactiveClient).isNotNull();
        assertThat(reactiveClient).isInstanceOf(ReactiveCloudKubernetesClient.class);
        assertThat(reactiveClient.getEksDelegate()).isSameAs(mockEksClient);
        assertThat(reactiveClient.getDelegate()).isSameAs(mockEksClient);
        assertThat(reactiveClient.getClusterName()).isEqualTo("test-cluster");
        assertThat(reactiveClient.getRegion()).isEqualTo("us-east-1");
    }

    @Test
    void shouldExposeAccessEntriesFromDelegate() {
        when(mockEksClient.accessEntries()).thenReturn(null);

        final ReactiveEksClient reactiveClient = ReactiveEksClient.from(mockEksClient);

        assertThat(reactiveClient.accessEntries()).isNull();
    }
}
