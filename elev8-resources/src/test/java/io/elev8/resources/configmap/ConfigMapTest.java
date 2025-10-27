package io.elev8.resources.configmap;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigMapTest {

    @Test
    void shouldBuildConfigMapWithRequiredFields() {
        final ConfigMap configMap = ConfigMap.builder()
                .name("test-config")
                .namespace("default")
                .build();

        assertThat(configMap.getApiVersion()).isEqualTo("v1");
        assertThat(configMap.getKind()).isEqualTo("ConfigMap");
        assertThat(configMap.getName()).isEqualTo("test-config");
        assertThat(configMap.getNamespace()).isEqualTo("default");
    }

    @Test
    void shouldBuildConfigMapWithData() {
        final Map<String, String> data = new HashMap<>();
        data.put("database.url", "jdbc:postgresql://localhost:5432/mydb");
        data.put("database.user", "admin");

        final ConfigMap configMap = ConfigMap.builder()
                .name("database-config")
                .namespace("default")
                .data(data)
                .build();

        assertThat(configMap.getData()).hasSize(2);
        assertThat(configMap.getData()).containsEntry("database.url", "jdbc:postgresql://localhost:5432/mydb");
        assertThat(configMap.getData()).containsEntry("database.user", "admin");
    }

    @Test
    void shouldBuildConfigMapWithAddDataMethod() {
        final ConfigMap configMap = ConfigMap.builder()
                .name("app-config")
                .namespace("default")
                .addData("app.name", "myapp")
                .addData("app.version", "1.0.0")
                .addData("app.environment", "production")
                .build();

        assertThat(configMap.getData()).hasSize(3);
        assertThat(configMap.getData()).containsEntry("app.name", "myapp");
        assertThat(configMap.getData()).containsEntry("app.version", "1.0.0");
        assertThat(configMap.getData()).containsEntry("app.environment", "production");
    }

    @Test
    void shouldBuildConfigMapWithBinaryData() {
        final Map<String, String> binaryData = new HashMap<>();
        binaryData.put("image.png", "iVBORw0KGgoAAAANSUhEUgAAAAUA");

        final ConfigMap configMap = ConfigMap.builder()
                .name("binary-config")
                .namespace("default")
                .binaryData(binaryData)
                .build();

        assertThat(configMap.getBinaryData()).hasSize(1);
        assertThat(configMap.getBinaryData()).containsEntry("image.png", "iVBORw0KGgoAAAANSUhEUgAAAAUA");
    }

    @Test
    void shouldBuildConfigMapWithAddBinaryDataMethod() {
        final ConfigMap configMap = ConfigMap.builder()
                .name("binary-config")
                .namespace("default")
                .addBinaryData("cert.pem", "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0t")
                .build();

        assertThat(configMap.getBinaryData()).hasSize(1);
        assertThat(configMap.getBinaryData()).containsEntry("cert.pem", "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0t");
    }

    @Test
    void shouldBuildConfigMapWithLabels() {
        final ConfigMap configMap = ConfigMap.builder()
                .name("labeled-config")
                .namespace("default")
                .label("app", "myapp")
                .label("env", "production")
                .addData("key", "value")
                .build();

        assertThat(configMap.getMetadata().getLabels()).containsEntry("app", "myapp");
        assertThat(configMap.getMetadata().getLabels()).containsEntry("env", "production");
    }

    @Test
    void shouldBuildImmutableConfigMap() {
        final ConfigMap configMap = ConfigMap.builder()
                .name("immutable-config")
                .namespace("default")
                .addData("constant", "value")
                .immutable(true)
                .build();

        assertThat(configMap.getImmutable()).isTrue();
    }

    @Test
    void shouldBuildConfigMapWithDataAndBinaryData() {
        final ConfigMap configMap = ConfigMap.builder()
                .name("mixed-config")
                .namespace("default")
                .addData("text.key", "text value")
                .addBinaryData("binary.key", "YmluYXJ5IHZhbHVl")
                .build();

        assertThat(configMap.getData()).hasSize(1);
        assertThat(configMap.getBinaryData()).hasSize(1);
        assertThat(configMap.getData()).containsEntry("text.key", "text value");
        assertThat(configMap.getBinaryData()).containsEntry("binary.key", "YmluYXJ5IHZhbHVl");
    }

    @Test
    void shouldSerializeToJson() {
        final ConfigMap configMap = ConfigMap.builder()
                .name("test-config")
                .namespace("default")
                .addData("key1", "value1")
                .addData("key2", "value2")
                .build();

        final String json = configMap.toJson();

        assertThat(json).contains("\"apiVersion\":\"v1\"");
        assertThat(json).contains("\"kind\":\"ConfigMap\"");
        assertThat(json).contains("\"name\":\"test-config\"");
        assertThat(json).contains("\"namespace\":\"default\"");
        assertThat(json).contains("\"key1\":\"value1\"");
        assertThat(json).contains("\"key2\":\"value2\"");
    }

    @Test
    void shouldSerializeToJsonWithBinaryData() {
        final ConfigMap configMap = ConfigMap.builder()
                .name("binary-config")
                .namespace("default")
                .addBinaryData("file.dat", "AQIDBA==")
                .build();

        final String json = configMap.toJson();

        assertThat(json).contains("\"binaryData\"");
        assertThat(json).contains("\"file.dat\":\"AQIDBA==\"");
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() {
        final ConfigMap configMap = ConfigMap.builder()
                .name("minimal-config")
                .namespace("default")
                .build();

        final String json = configMap.toJson();

        assertThat(json).doesNotContain("\"data\"");
        assertThat(json).doesNotContain("\"binaryData\"");
        assertThat(json).doesNotContain("\"immutable\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> ConfigMap.builder()
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ConfigMap name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> ConfigMap.builder()
                .name("")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ConfigMap name is required");
    }

    @Test
    void shouldAllowConfigMapWithoutNamespace() {
        final ConfigMap configMap = ConfigMap.builder()
                .name("cluster-config")
                .addData("key", "value")
                .build();

        assertThat(configMap.getName()).isEqualTo("cluster-config");
        assertThat(configMap.getNamespace()).isNull();
    }

    @Test
    void shouldAllowEmptyConfigMap() {
        final ConfigMap configMap = ConfigMap.builder()
                .name("empty-config")
                .namespace("default")
                .build();

        assertThat(configMap.getData()).isNull();
        assertThat(configMap.getBinaryData()).isNull();
    }
}
