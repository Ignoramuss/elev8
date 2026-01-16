package io.elev8.resources.lease;

import io.elev8.core.client.KubernetesClient;
import io.elev8.core.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaseManagerTest {

    @Mock
    private KubernetesClient client;

    @Mock
    private HttpResponse response;

    private LeaseManager leaseManager;

    @BeforeEach
    void setUp() {
        leaseManager = new LeaseManager(client);
    }

    @Test
    void shouldHaveCorrectApiPath() {
        assertThat(leaseManager.getApiPath()).isEqualTo("/apis/coordination.k8s.io/v1");
    }

    @Test
    void shouldGetLease() throws Exception {
        final String leaseJson = """
                {
                    "apiVersion": "coordination.k8s.io/v1",
                    "kind": "Lease",
                    "metadata": {
                        "name": "test-lease",
                        "namespace": "kube-system"
                    },
                    "spec": {
                        "holderIdentity": "controller-1",
                        "leaseDurationSeconds": 15
                    }
                }
                """;

        when(client.get(anyString())).thenReturn(response);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(leaseJson);

        final Lease lease = leaseManager.get("kube-system", "test-lease");

        assertThat(lease.getName()).isEqualTo("test-lease");
        assertThat(lease.getSpec().getHolderIdentity()).isEqualTo("controller-1");
        verify(client).get("/apis/coordination.k8s.io/v1/namespaces/kube-system/leases/test-lease");
    }

    @Test
    void shouldCreateLease() throws Exception {
        final Lease lease = Lease.builder()
                .namespace("default")
                .name("new-lease")
                .holderIdentity("pod-123")
                .leaseDurationSeconds(10)
                .build();

        final String responseJson = """
                {
                    "apiVersion": "coordination.k8s.io/v1",
                    "kind": "Lease",
                    "metadata": {
                        "name": "new-lease",
                        "namespace": "default",
                        "uid": "abc-123"
                    },
                    "spec": {
                        "holderIdentity": "pod-123",
                        "leaseDurationSeconds": 10
                    }
                }
                """;

        when(client.post(anyString(), anyString())).thenReturn(response);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseJson);

        final Lease created = leaseManager.create(lease);

        assertThat(created.getName()).isEqualTo("new-lease");
        assertThat(created.getMetadata().getUid()).isEqualTo("abc-123");
        verify(client).post(
                org.mockito.ArgumentMatchers.eq("/apis/coordination.k8s.io/v1/namespaces/default/leases"),
                anyString()
        );
    }

    @Test
    void shouldUpdateLease() throws Exception {
        final Lease lease = Lease.builder()
                .namespace("default")
                .name("existing-lease")
                .holderIdentity("new-holder")
                .leaseDurationSeconds(20)
                .build();

        final String responseJson = """
                {
                    "apiVersion": "coordination.k8s.io/v1",
                    "kind": "Lease",
                    "metadata": {
                        "name": "existing-lease",
                        "namespace": "default"
                    },
                    "spec": {
                        "holderIdentity": "new-holder",
                        "leaseDurationSeconds": 20
                    }
                }
                """;

        when(client.put(anyString(), anyString())).thenReturn(response);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(responseJson);

        final Lease updated = leaseManager.update(lease);

        assertThat(updated.getSpec().getHolderIdentity()).isEqualTo("new-holder");
        verify(client).put(
                org.mockito.ArgumentMatchers.eq("/apis/coordination.k8s.io/v1/namespaces/default/leases/existing-lease"),
                anyString()
        );
    }

    @Test
    void shouldDeleteLease() throws Exception {
        when(client.delete(anyString())).thenReturn(response);
        when(response.isSuccessful()).thenReturn(true);

        leaseManager.delete("kube-system", "old-lease");

        verify(client).delete("/apis/coordination.k8s.io/v1/namespaces/kube-system/leases/old-lease");
    }
}
