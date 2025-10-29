package io.elev8.resources.serviceaccount;

import io.elev8.resources.LocalObjectReference;
import io.elev8.resources.ObjectReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceAccountTest {

    @Test
    void shouldBuildServiceAccountWithRequiredFields() {
        final ServiceAccount serviceAccount = ServiceAccount.builder()
                .name("my-service-account")
                .namespace("default")
                .build();

        assertThat(serviceAccount.getApiVersion()).isEqualTo("v1");
        assertThat(serviceAccount.getKind()).isEqualTo("ServiceAccount");
        assertThat(serviceAccount.getName()).isEqualTo("my-service-account");
        assertThat(serviceAccount.getNamespace()).isEqualTo("default");
    }

    @Test
    void shouldBuildServiceAccountWithLabels() {
        final ServiceAccount serviceAccount = ServiceAccount.builder()
                .name("my-service-account")
                .namespace("default")
                .label("app", "backend")
                .label("env", "prod")
                .build();

        assertThat(serviceAccount.getMetadata().getLabels()).containsEntry("app", "backend");
        assertThat(serviceAccount.getMetadata().getLabels()).containsEntry("env", "prod");
    }

    @Test
    void shouldBuildServiceAccountWithSpec() {
        final ServiceAccountSpec spec = ServiceAccountSpec.builder()
                .automountServiceAccountToken(true)
                .build();

        final ServiceAccount serviceAccount = ServiceAccount.builder()
                .name("my-service-account")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(serviceAccount.getSpec()).isEqualTo(spec);
        assertThat(serviceAccount.getSpec().getAutomountServiceAccountToken()).isTrue();
    }

    @Test
    void shouldBuildServiceAccountWithImagePullSecrets() {
        final ServiceAccountSpec spec = ServiceAccountSpec.builder()
                .imagePullSecret(LocalObjectReference.builder()
                        .name("my-docker-secret")
                        .build())
                .imagePullSecret(LocalObjectReference.builder()
                        .name("another-secret")
                        .build())
                .build();

        final ServiceAccount serviceAccount = ServiceAccount.builder()
                .name("my-service-account")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(serviceAccount.getSpec().getImagePullSecrets()).hasSize(2);
        assertThat(serviceAccount.getSpec().getImagePullSecrets().get(0).getName())
                .isEqualTo("my-docker-secret");
        assertThat(serviceAccount.getSpec().getImagePullSecrets().get(1).getName())
                .isEqualTo("another-secret");
    }

    @Test
    void shouldBuildServiceAccountWithSecretReferences() {
        final ServiceAccountSpec spec = ServiceAccountSpec.builder()
                .secret(ObjectReference.builder()
                        .name("my-secret-token")
                        .namespace("default")
                        .build())
                .build();

        final ServiceAccount serviceAccount = ServiceAccount.builder()
                .name("my-service-account")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(serviceAccount.getSpec().getSecrets()).hasSize(1);
        assertThat(serviceAccount.getSpec().getSecrets().get(0).getName()).isEqualTo("my-secret-token");
    }

    @Test
    void shouldBuildServiceAccountWithStatus() {
        final ServiceAccountStatus status = ServiceAccountStatus.builder()
                .secret(ObjectReference.builder()
                        .name("token-xyz")
                        .build())
                .build();

        final ServiceAccount serviceAccount = ServiceAccount.builder()
                .name("my-service-account")
                .namespace("default")
                .status(status)
                .build();

        assertThat(serviceAccount.getStatus()).isNotNull();
        assertThat(serviceAccount.getStatus().getSecrets()).hasSize(1);
    }

    @Test
    void shouldSerializeToJson() {
        final ServiceAccount serviceAccount = ServiceAccount.builder()
                .name("my-service-account")
                .namespace("default")
                .spec(ServiceAccountSpec.builder()
                        .automountServiceAccountToken(true)
                        .build())
                .build();

        final String json = serviceAccount.toJson();

        assertThat(json).contains("\"apiVersion\":\"v1\"");
        assertThat(json).contains("\"kind\":\"ServiceAccount\"");
        assertThat(json).contains("\"name\":\"my-service-account\"");
        assertThat(json).contains("\"automountServiceAccountToken\":true");
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() {
        final ServiceAccount serviceAccount = ServiceAccount.builder()
                .name("my-service-account")
                .namespace("default")
                .build();

        final String json = serviceAccount.toJson();

        assertThat(json).doesNotContain("\"spec\"");
        assertThat(json).doesNotContain("\"status\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> ServiceAccount.builder()
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ServiceAccount name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> ServiceAccount.builder()
                .name("")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ServiceAccount name is required");
    }

    @Test
    void shouldAllowServiceAccountWithoutNamespace() {
        final ServiceAccount serviceAccount = ServiceAccount.builder()
                .name("my-service-account")
                .build();

        assertThat(serviceAccount.getName()).isEqualTo("my-service-account");
        assertThat(serviceAccount.getNamespace()).isNull();
    }

    @Test
    void shouldAllowServiceAccountWithoutSpec() {
        final ServiceAccount serviceAccount = ServiceAccount.builder()
                .name("my-service-account")
                .namespace("default")
                .build();

        assertThat(serviceAccount.getSpec()).isNull();
    }
}
