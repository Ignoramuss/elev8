package io.elev8.resources.secret;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecretTest {

    @Test
    void shouldBuildSecretWithRequiredFields() {
        final Secret secret = Secret.builder()
                .name("test-secret")
                .namespace("default")
                .build();

        assertThat(secret.getApiVersion()).isEqualTo("v1");
        assertThat(secret.getKind()).isEqualTo("Secret");
        assertThat(secret.getName()).isEqualTo("test-secret");
        assertThat(secret.getNamespace()).isEqualTo("default");
        assertThat(secret.getType()).isEqualTo(Secret.TYPE_OPAQUE);
    }

    @Test
    void shouldBuildSecretWithData() {
        final Map<String, String> data = new HashMap<>();
        data.put("username", "YWRtaW4=");
        data.put("password", "cGFzc3dvcmQxMjM=");

        final Secret secret = Secret.builder()
                .name("credentials")
                .namespace("default")
                .data(data)
                .build();

        assertThat(secret.getData()).hasSize(2);
        assertThat(secret.getData()).containsEntry("username", "YWRtaW4=");
        assertThat(secret.getData()).containsEntry("password", "cGFzc3dvcmQxMjM=");
    }

    @Test
    void shouldBuildSecretWithAddDataMethod() {
        final Secret secret = Secret.builder()
                .name("api-secret")
                .namespace("default")
                .addData("api-key", "YXBpLWtleS0xMjM0NTY=")
                .addData("api-secret", "c2VjcmV0LWFiY2RlZg==")
                .build();

        assertThat(secret.getData()).hasSize(2);
        assertThat(secret.getData()).containsEntry("api-key", "YXBpLWtleS0xMjM0NTY=");
        assertThat(secret.getData()).containsEntry("api-secret", "c2VjcmV0LWFiY2RlZg==");
    }

    @Test
    void shouldBuildSecretWithStringData() {
        final Map<String, String> stringData = new HashMap<>();
        stringData.put("username", "admin");
        stringData.put("password", "password123");

        final Secret secret = Secret.builder()
                .name("credentials")
                .namespace("default")
                .stringData(stringData)
                .build();

        assertThat(secret.getStringData()).hasSize(2);
        assertThat(secret.getStringData()).containsEntry("username", "admin");
        assertThat(secret.getStringData()).containsEntry("password", "password123");
    }

    @Test
    void shouldBuildSecretWithAddStringDataMethod() {
        final Secret secret = Secret.builder()
                .name("app-secret")
                .namespace("default")
                .addStringData("db-password", "mysecretpassword")
                .addStringData("api-token", "mytoken123")
                .build();

        assertThat(secret.getStringData()).hasSize(2);
        assertThat(secret.getStringData()).containsEntry("db-password", "mysecretpassword");
        assertThat(secret.getStringData()).containsEntry("api-token", "mytoken123");
    }

    @Test
    void shouldBuildSecretWithCustomType() {
        final Secret secret = Secret.builder()
                .name("custom-secret")
                .namespace("default")
                .type("my-custom-type")
                .addStringData("key", "value")
                .build();

        assertThat(secret.getType()).isEqualTo("my-custom-type");
    }

    @Test
    void shouldBuildOpaqueSecret() {
        final Secret secret = Secret.builder()
                .name("opaque-secret")
                .namespace("default")
                .opaque()
                .addStringData("key", "value")
                .build();

        assertThat(secret.getType()).isEqualTo(Secret.TYPE_OPAQUE);
    }

    @Test
    void shouldBuildTlsSecret() {
        final String cert = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0t";
        final String key = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0t";

        final Secret secret = Secret.builder()
                .name("tls-secret")
                .namespace("default")
                .tls(cert, key)
                .build();

        assertThat(secret.getType()).isEqualTo(Secret.TYPE_TLS);
        assertThat(secret.getData()).containsEntry("tls.crt", cert);
        assertThat(secret.getData()).containsEntry("tls.key", key);
    }

    @Test
    void shouldBuildBasicAuthSecret() {
        final Secret secret = Secret.builder()
                .name("basic-auth")
                .namespace("default")
                .basicAuth("admin", "password123")
                .build();

        assertThat(secret.getType()).isEqualTo(Secret.TYPE_BASIC_AUTH);
        assertThat(secret.getStringData()).containsEntry("username", "admin");
        assertThat(secret.getStringData()).containsEntry("password", "password123");
    }

    @Test
    void shouldBuildDockerConfigJsonSecret() {
        final String dockerConfig = "eyJhdXRocyI6eyJodHRwczovL2luZGV4LmRvY2tlci5pby92MS8iOnt9fX0=";

        final Secret secret = Secret.builder()
                .name("docker-secret")
                .namespace("default")
                .dockerConfigJson(dockerConfig)
                .build();

        assertThat(secret.getType()).isEqualTo(Secret.TYPE_DOCKERCONFIGJSON);
        assertThat(secret.getData()).containsEntry(".dockerconfigjson", dockerConfig);
    }

    @Test
    void shouldBuildSshAuthSecret() {
        final String privateKey = "-----BEGIN RSA PRIVATE KEY-----\nMIIEpAIBAAKCAQEA...";

        final Secret secret = Secret.builder()
                .name("ssh-secret")
                .namespace("default")
                .sshAuth(privateKey)
                .build();

        assertThat(secret.getType()).isEqualTo(Secret.TYPE_SSH_AUTH);
        assertThat(secret.getStringData()).containsEntry("ssh-privatekey", privateKey);
    }

    @Test
    void shouldBuildSecretWithLabels() {
        final Secret secret = Secret.builder()
                .name("labeled-secret")
                .namespace("default")
                .label("app", "myapp")
                .label("env", "production")
                .addStringData("key", "value")
                .build();

        assertThat(secret.getMetadata().getLabels()).containsEntry("app", "myapp");
        assertThat(secret.getMetadata().getLabels()).containsEntry("env", "production");
    }

    @Test
    void shouldBuildImmutableSecret() {
        final Secret secret = Secret.builder()
                .name("immutable-secret")
                .namespace("default")
                .addStringData("constant", "value")
                .immutable(true)
                .build();

        assertThat(secret.getImmutable()).isTrue();
    }

    @Test
    void shouldBuildSecretWithDataAndStringData() {
        final Secret secret = Secret.builder()
                .name("mixed-secret")
                .namespace("default")
                .addData("encoded-key", "ZW5jb2RlZA==")
                .addStringData("plain-key", "plain value")
                .build();

        assertThat(secret.getData()).hasSize(1);
        assertThat(secret.getStringData()).hasSize(1);
        assertThat(secret.getData()).containsEntry("encoded-key", "ZW5jb2RlZA==");
        assertThat(secret.getStringData()).containsEntry("plain-key", "plain value");
    }

    @Test
    void shouldSerializeToJson() {
        final Secret secret = Secret.builder()
                .name("test-secret")
                .namespace("default")
                .addStringData("key1", "value1")
                .addStringData("key2", "value2")
                .build();

        final String json = secret.toJson();

        assertThat(json).contains("\"apiVersion\":\"v1\"");
        assertThat(json).contains("\"kind\":\"Secret\"");
        assertThat(json).contains("\"name\":\"test-secret\"");
        assertThat(json).contains("\"namespace\":\"default\"");
        assertThat(json).contains("\"type\":\"Opaque\"");
        assertThat(json).contains("\"key1\":\"value1\"");
        assertThat(json).contains("\"key2\":\"value2\"");
    }

    @Test
    void shouldSerializeToJsonWithType() {
        final Secret secret = Secret.builder()
                .name("tls-secret")
                .namespace("default")
                .type(Secret.TYPE_TLS)
                .addData("tls.crt", "Y2VydA==")
                .addData("tls.key", "a2V5")
                .build();

        final String json = secret.toJson();

        assertThat(json).contains("\"type\":\"kubernetes.io/tls\"");
        assertThat(json).contains("\"tls.crt\":\"Y2VydA==\"");
        assertThat(json).contains("\"tls.key\":\"a2V5\"");
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() {
        final Secret secret = Secret.builder()
                .name("minimal-secret")
                .namespace("default")
                .build();

        final String json = secret.toJson();

        assertThat(json).doesNotContain("\"data\"");
        assertThat(json).doesNotContain("\"stringData\"");
        assertThat(json).doesNotContain("\"immutable\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> Secret.builder()
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Secret name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> Secret.builder()
                .name("")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Secret name is required");
    }

    @Test
    void shouldAllowSecretWithoutNamespace() {
        final Secret secret = Secret.builder()
                .name("cluster-secret")
                .addStringData("key", "value")
                .build();

        assertThat(secret.getName()).isEqualTo("cluster-secret");
        assertThat(secret.getNamespace()).isNull();
    }

    @Test
    void shouldAllowEmptySecret() {
        final Secret secret = Secret.builder()
                .name("empty-secret")
                .namespace("default")
                .build();

        assertThat(secret.getData()).isNull();
        assertThat(secret.getStringData()).isNull();
    }

    @Test
    void shouldDefaultToOpaqueTypeWhenNotSpecified() {
        final Secret secret = Secret.builder()
                .name("default-type-secret")
                .namespace("default")
                .addStringData("key", "value")
                .build();

        assertThat(secret.getType()).isEqualTo(Secret.TYPE_OPAQUE);
    }
}
