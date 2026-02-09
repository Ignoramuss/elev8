package io.elev8.reactor.aks;

import io.elev8.aks.AksClient;
import io.elev8.reactor.ReactiveCloudKubernetesClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveAksClientTest {

    @Mock
    private AksClient mockAksClient;

    @Test
    void shouldThrowExceptionWhenSubscriptionIdIsNull() {
        assertThatThrownBy(() -> ReactiveAksClient.builder()
                .resourceGroupName("my-rg")
                .clusterName("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Subscription ID is required");
    }

    @Test
    void shouldThrowExceptionWhenResourceGroupNameIsNull() {
        assertThatThrownBy(() -> ReactiveAksClient.builder()
                .subscriptionId("sub-123")
                .clusterName("test-cluster")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Resource group name is required");
    }

    @Test
    void shouldThrowExceptionWhenClusterNameIsNull() {
        assertThatThrownBy(() -> ReactiveAksClient.builder()
                .subscriptionId("sub-123")
                .resourceGroupName("my-rg")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cluster name is required");
    }

    @Test
    void shouldWrapExistingAksClient() {
        when(mockAksClient.getSubscriptionId()).thenReturn("sub-123");
        when(mockAksClient.getResourceGroupName()).thenReturn("my-rg");
        when(mockAksClient.getClusterName()).thenReturn("test-cluster");

        final ReactiveAksClient reactiveClient = ReactiveAksClient.from(mockAksClient);

        assertThat(reactiveClient).isNotNull();
        assertThat(reactiveClient).isInstanceOf(ReactiveCloudKubernetesClient.class);
        assertThat(reactiveClient.getAksDelegate()).isSameAs(mockAksClient);
        assertThat(reactiveClient.getDelegate()).isSameAs(mockAksClient);
        assertThat(reactiveClient.getSubscriptionId()).isEqualTo("sub-123");
        assertThat(reactiveClient.getResourceGroupName()).isEqualTo("my-rg");
        assertThat(reactiveClient.getClusterName()).isEqualTo("test-cluster");
    }
}
