package io.elev8.resources.serviceaccount;

import io.elev8.resources.LocalObjectReference;
import io.elev8.resources.ObjectReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceAccountSpecTest {

    @Test
    void shouldBuildSpecWithAutomountTrue() {
        final ServiceAccountSpec spec = ServiceAccountSpec.builder()
                .automountServiceAccountToken(true)
                .build();

        assertThat(spec.getAutomountServiceAccountToken()).isTrue();
    }

    @Test
    void shouldBuildSpecWithAutomountFalse() {
        final ServiceAccountSpec spec = ServiceAccountSpec.builder()
                .automountServiceAccountToken(false)
                .build();

        assertThat(spec.getAutomountServiceAccountToken()).isFalse();
    }

    @Test
    void shouldBuildSpecWithImagePullSecrets() {
        final ServiceAccountSpec spec = ServiceAccountSpec.builder()
                .imagePullSecret(LocalObjectReference.builder()
                        .name("docker-secret")
                        .build())
                .imagePullSecret(LocalObjectReference.builder()
                        .name("ghcr-secret")
                        .build())
                .build();

        assertThat(spec.getImagePullSecrets()).hasSize(2);
        assertThat(spec.getImagePullSecrets().get(0).getName()).isEqualTo("docker-secret");
        assertThat(spec.getImagePullSecrets().get(1).getName()).isEqualTo("ghcr-secret");
    }

    @Test
    void shouldBuildSpecWithSecrets() {
        final ServiceAccountSpec spec = ServiceAccountSpec.builder()
                .secret(ObjectReference.builder()
                        .name("token-abc")
                        .namespace("default")
                        .build())
                .secret(ObjectReference.builder()
                        .name("token-xyz")
                        .namespace("default")
                        .build())
                .build();

        assertThat(spec.getSecrets()).hasSize(2);
        assertThat(spec.getSecrets().get(0).getName()).isEqualTo("token-abc");
        assertThat(spec.getSecrets().get(1).getName()).isEqualTo("token-xyz");
    }

    @Test
    void shouldBuildCompleteSpec() {
        final ServiceAccountSpec spec = ServiceAccountSpec.builder()
                .automountServiceAccountToken(true)
                .imagePullSecret(LocalObjectReference.builder()
                        .name("docker-secret")
                        .build())
                .secret(ObjectReference.builder()
                        .name("token-abc")
                        .namespace("default")
                        .build())
                .build();

        assertThat(spec.getAutomountServiceAccountToken()).isTrue();
        assertThat(spec.getImagePullSecrets()).hasSize(1);
        assertThat(spec.getSecrets()).hasSize(1);
    }

    @Test
    void shouldBuildEmptySpec() {
        final ServiceAccountSpec spec = ServiceAccountSpec.builder().build();

        assertThat(spec.getAutomountServiceAccountToken()).isNull();
        assertThat(spec.getImagePullSecrets()).isEmpty();
        assertThat(spec.getSecrets()).isEmpty();
    }
}
