package io.elev8.resources.lease;

import io.elev8.resources.AbstractResource;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LeaseTest {

    @Test
    void shouldBuildLeaseWithBuilder() {
        final Instant now = Instant.now();

        final Lease lease = Lease.builder()
                .namespace("kube-system")
                .name("test-leader")
                .holderIdentity("pod-abc123")
                .leaseDurationSeconds(15)
                .acquireTime(now)
                .renewTime(now)
                .leaseTransitions(0)
                .build();

        assertThat(lease.getApiVersion()).isEqualTo("coordination.k8s.io/v1");
        assertThat(lease.getKind()).isEqualTo("Lease");
        assertThat(lease.getNamespace()).isEqualTo("kube-system");
        assertThat(lease.getName()).isEqualTo("test-leader");
        assertThat(lease.getSpec()).isNotNull();
        assertThat(lease.getSpec().getHolderIdentity()).isEqualTo("pod-abc123");
        assertThat(lease.getSpec().getLeaseDurationSeconds()).isEqualTo(15);
        assertThat(lease.getSpec().getAcquireTime()).isEqualTo(now);
        assertThat(lease.getSpec().getRenewTime()).isEqualTo(now);
        assertThat(lease.getSpec().getLeaseTransitions()).isEqualTo(0);
    }

    @Test
    void shouldBuildLeaseWithSpec() {
        final LeaseSpec spec = LeaseSpec.builder()
                .holderIdentity("controller-1")
                .leaseDurationSeconds(30)
                .build();

        final Lease lease = Lease.builder()
                .namespace("default")
                .name("my-lease")
                .spec(spec)
                .build();

        assertThat(lease.getSpec()).isSameAs(spec);
    }

    @Test
    void shouldSerializeToJson() {
        final Lease lease = Lease.builder()
                .namespace("test-ns")
                .name("json-test")
                .holderIdentity("test-holder")
                .leaseDurationSeconds(10)
                .build();

        final String json = lease.toJson();

        assertThat(json).contains("\"apiVersion\":\"coordination.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"Lease\"");
        assertThat(json).contains("\"holderIdentity\":\"test-holder\"");
        assertThat(json).contains("\"leaseDurationSeconds\":10");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                    "apiVersion": "coordination.k8s.io/v1",
                    "kind": "Lease",
                    "metadata": {
                        "name": "test-lease",
                        "namespace": "default"
                    },
                    "spec": {
                        "holderIdentity": "pod-xyz",
                        "leaseDurationSeconds": 20,
                        "leaseTransitions": 5
                    }
                }
                """;

        final Lease lease = AbstractResource.fromJson(json, Lease.class);

        assertThat(lease.getName()).isEqualTo("test-lease");
        assertThat(lease.getNamespace()).isEqualTo("default");
        assertThat(lease.getSpec().getHolderIdentity()).isEqualTo("pod-xyz");
        assertThat(lease.getSpec().getLeaseDurationSeconds()).isEqualTo(20);
        assertThat(lease.getSpec().getLeaseTransitions()).isEqualTo(5);
    }

    @Test
    void shouldCreateEmptyLease() {
        final Lease lease = new Lease();

        assertThat(lease.getApiVersion()).isEqualTo("coordination.k8s.io/v1");
        assertThat(lease.getKind()).isEqualTo("Lease");
        assertThat(lease.getSpec()).isNull();
    }
}
