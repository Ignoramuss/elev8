package io.elev8.resources.serviceaccount;

import io.elev8.resources.ObjectReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceAccountStatusTest {

    @Test
    void shouldBuildStatusWithSecrets() {
        final ServiceAccountStatus status = ServiceAccountStatus.builder()
                .secret(ObjectReference.builder()
                        .name("token-abc")
                        .namespace("default")
                        .build())
                .build();

        assertThat(status.getSecrets()).hasSize(1);
        assertThat(status.getSecrets().get(0).getName()).isEqualTo("token-abc");
    }

    @Test
    void shouldBuildStatusWithMultipleSecrets() {
        final ServiceAccountStatus status = ServiceAccountStatus.builder()
                .secret(ObjectReference.builder()
                        .name("token-abc")
                        .namespace("default")
                        .build())
                .secret(ObjectReference.builder()
                        .name("token-xyz")
                        .namespace("default")
                        .build())
                .build();

        assertThat(status.getSecrets()).hasSize(2);
        assertThat(status.getSecrets().get(0).getName()).isEqualTo("token-abc");
        assertThat(status.getSecrets().get(1).getName()).isEqualTo("token-xyz");
    }

    @Test
    void shouldBuildEmptyStatus() {
        final ServiceAccountStatus status = ServiceAccountStatus.builder().build();

        assertThat(status.getSecrets()).isEmpty();
    }
}
