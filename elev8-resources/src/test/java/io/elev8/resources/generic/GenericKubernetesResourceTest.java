package io.elev8.resources.generic;

import io.elev8.resources.AbstractResource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GenericKubernetesResourceTest {

    @Test
    void shouldBuildResourceWithRequiredFields() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .namespace("default")
                .build();

        assertThat(resource.getApiVersion()).isEqualTo("stable.example.com/v1");
        assertThat(resource.getKind()).isEqualTo("CronTab");
        assertThat(resource.getName()).isEqualTo("my-cron");
        assertThat(resource.getNamespace()).isEqualTo("default");
    }

    @Test
    void shouldBuildResourceWithSpec() {
        final Map<String, Object> spec = new HashMap<>();
        spec.put("cronSpec", "* * * * */5");
        spec.put("replicas", 3);

        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(resource.getSpec()).containsEntry("cronSpec", "* * * * */5");
        assertThat(resource.getSpec()).containsEntry("replicas", 3);
    }

    @Test
    void shouldBuildResourceWithSpecField() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .namespace("default")
                .specField("cronSpec", "* * * * */5")
                .specField("replicas", 3)
                .build();

        assertThat(resource.getSpec()).containsEntry("cronSpec", "* * * * */5");
        assertThat(resource.getSpec()).containsEntry("replicas", 3);
    }

    @Test
    void shouldBuildResourceWithStatus() {
        final Map<String, Object> status = new HashMap<>();
        status.put("lastScheduleTime", "2025-01-01T00:00:00Z");
        status.put("active", 2);

        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .namespace("default")
                .status(status)
                .build();

        assertThat(resource.getStatus()).containsEntry("lastScheduleTime", "2025-01-01T00:00:00Z");
        assertThat(resource.getStatus()).containsEntry("active", 2);
    }

    @Test
    void shouldBuildResourceWithStatusField() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .namespace("default")
                .statusField("phase", "Running")
                .statusField("replicas", 3)
                .build();

        assertThat(resource.getStatus()).containsEntry("phase", "Running");
        assertThat(resource.getStatus()).containsEntry("replicas", 3);
    }

    @Test
    void shouldBuildResourceWithLabels() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .namespace("default")
                .label("app", "my-app")
                .label("env", "production")
                .build();

        assertThat(resource.getMetadata().getLabels()).containsEntry("app", "my-app");
        assertThat(resource.getMetadata().getLabels()).containsEntry("env", "production");
    }

    @Test
    void shouldBuildResourceWithAnnotations() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .namespace("default")
                .annotation("description", "My custom CronTab")
                .build();

        assertThat(resource.getMetadata().getAnnotations()).containsEntry("description", "My custom CronTab");
    }

    @Test
    void shouldBuildResourceWithAdditionalProperties() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .namespace("default")
                .additionalProperty("customField", "customValue")
                .build();

        assertThat(resource.getAdditionalProperties()).containsEntry("customField", "customValue");
    }

    @Test
    void shouldBuildFromContext() {
        final GenericResourceContext context = GenericResourceContext.forNamespacedResource(
                "stable.example.com", "v1", "CronTab", "crontabs");

        final GenericKubernetesResource resource = GenericKubernetesResource.fromContext(context)
                .name("my-cron")
                .namespace("default")
                .specField("cronSpec", "* * * * */5")
                .build();

        assertThat(resource.getApiVersion()).isEqualTo("stable.example.com/v1");
        assertThat(resource.getKind()).isEqualTo("CronTab");
        assertThat(resource.getName()).isEqualTo("my-cron");
    }

    @Test
    void shouldGetSpecValueByDotNotation() {
        final Map<String, Object> nested = new HashMap<>();
        nested.put("field", "value");

        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .specField("nested", nested)
                .specField("simple", "simpleValue")
                .build();

        assertThat(resource.getSpec("simple")).isEqualTo("simpleValue");
        assertThat(resource.getSpec("nested.field")).isEqualTo("value");
    }

    @Test
    void shouldReturnNullForMissingDotNotationPath() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .specField("simple", "simpleValue")
                .build();

        assertThat(resource.getSpec("nonexistent")).isNull();
        assertThat(resource.getSpec("nested.missing")).isNull();
    }

    @Test
    void shouldSetSpecValueByDotNotation() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .build();

        resource.setSpec("simple", "simpleValue");
        resource.setSpec("nested.field", "nestedValue");
        resource.setSpec("deep.nested.field", "deepValue");

        assertThat(resource.getSpec("simple")).isEqualTo("simpleValue");
        assertThat(resource.getSpec("nested.field")).isEqualTo("nestedValue");
        assertThat(resource.getSpec("deep.nested.field")).isEqualTo("deepValue");
    }

    @Test
    void shouldGetStatusValueByDotNotation() {
        final Map<String, Object> conditions = new HashMap<>();
        conditions.put("type", "Ready");
        conditions.put("status", "True");

        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .statusField("phase", "Running")
                .statusField("condition", conditions)
                .build();

        assertThat(resource.getStatus("phase")).isEqualTo("Running");
        assertThat(resource.getStatus("condition.type")).isEqualTo("Ready");
        assertThat(resource.getStatus("condition.status")).isEqualTo("True");
    }

    @Test
    void shouldSetStatusValueByDotNotation() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .build();

        resource.setStatus("phase", "Running");
        resource.setStatus("conditions.ready", true);

        assertThat(resource.getStatus("phase")).isEqualTo("Running");
        assertThat(resource.getStatus("conditions.ready")).isEqualTo(true);
    }

    @Test
    void shouldSerializeToJson() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .namespace("default")
                .specField("cronSpec", "* * * * */5")
                .specField("replicas", 3)
                .build();

        final String json = resource.toJson();

        assertThat(json).contains("\"apiVersion\":\"stable.example.com/v1\"");
        assertThat(json).contains("\"kind\":\"CronTab\"");
        assertThat(json).contains("\"name\":\"my-cron\"");
        assertThat(json).contains("\"namespace\":\"default\"");
        assertThat(json).contains("\"cronSpec\":\"* * * * */5\"");
        assertThat(json).contains("\"replicas\":3");
    }

    @Test
    void shouldDeserializeFromJson() {
        final String json = """
                {
                    "apiVersion": "stable.example.com/v1",
                    "kind": "CronTab",
                    "metadata": {
                        "name": "my-cron",
                        "namespace": "default"
                    },
                    "spec": {
                        "cronSpec": "* * * * */5",
                        "replicas": 3
                    }
                }
                """;

        final GenericKubernetesResource resource = AbstractResource.fromJson(json, GenericKubernetesResource.class);

        assertThat(resource.getApiVersion()).isEqualTo("stable.example.com/v1");
        assertThat(resource.getKind()).isEqualTo("CronTab");
        assertThat(resource.getName()).isEqualTo("my-cron");
        assertThat(resource.getNamespace()).isEqualTo("default");
        assertThat(resource.getSpec()).containsEntry("cronSpec", "* * * * */5");
        assertThat(resource.getSpec()).containsEntry("replicas", 3);
    }

    @Test
    void shouldDeserializeWithUnknownFields() {
        final String json = """
                {
                    "apiVersion": "stable.example.com/v1",
                    "kind": "CronTab",
                    "metadata": {
                        "name": "my-cron",
                        "namespace": "default"
                    },
                    "spec": {
                        "cronSpec": "* * * * */5"
                    },
                    "customField": "customValue",
                    "anotherField": 42
                }
                """;

        final GenericKubernetesResource resource = AbstractResource.fromJson(json, GenericKubernetesResource.class);

        assertThat(resource.getAdditionalProperties()).containsEntry("customField", "customValue");
        assertThat(resource.getAdditionalProperties()).containsEntry("anotherField", 42);
    }

    @Test
    void shouldDeserializeNestedSpec() {
        final String json = """
                {
                    "apiVersion": "stable.example.com/v1",
                    "kind": "CronTab",
                    "metadata": {
                        "name": "my-cron"
                    },
                    "spec": {
                        "config": {
                            "database": {
                                "host": "localhost",
                                "port": 5432
                            }
                        }
                    }
                }
                """;

        final GenericKubernetesResource resource = AbstractResource.fromJson(json, GenericKubernetesResource.class);

        assertThat(resource.getSpec("config.database.host")).isEqualTo("localhost");
        assertThat(resource.getSpec("config.database.port")).isEqualTo(5432);
    }

    @Test
    void shouldBuildResourceWithUidAndResourceVersion() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .namespace("default")
                .uid("abc123")
                .resourceVersion("12345")
                .build();

        assertThat(resource.getMetadata().getUid()).isEqualTo("abc123");
        assertThat(resource.getMetadata().getResourceVersion()).isEqualTo("12345");
    }

    @Test
    void shouldHandleNullSpec() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .build();

        assertThat(resource.getSpec()).isNull();
        assertThat(resource.getSpec("any.path")).isNull();
    }

    @Test
    void shouldHandleNullStatus() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .build();

        assertThat(resource.getStatus()).isNull();
        assertThat(resource.getStatus("any.path")).isNull();
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .build();

        final String json = resource.toJson();

        assertThat(json).doesNotContain("\"spec\"");
        assertThat(json).doesNotContain("\"status\"");
    }

    @Test
    void shouldHandleSpecWithListValues() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .specField("items", List.of("item1", "item2", "item3"))
                .build();

        assertThat(resource.getSpec()).containsEntry("items", List.of("item1", "item2", "item3"));
    }

    @Test
    void shouldChainSetSpecCalls() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .build();

        resource.setSpec("field1", "value1")
                .setSpec("field2", "value2")
                .setSpec("field3", "value3");

        assertThat(resource.getSpec("field1")).isEqualTo("value1");
        assertThat(resource.getSpec("field2")).isEqualTo("value2");
        assertThat(resource.getSpec("field3")).isEqualTo("value3");
    }

    @Test
    void shouldChainSetStatusCalls() {
        final GenericKubernetesResource resource = GenericKubernetesResource.builder()
                .apiVersion("stable.example.com/v1")
                .kind("CronTab")
                .name("my-cron")
                .build();

        resource.setStatus("phase", "Running")
                .setStatus("replicas", 3)
                .setStatus("ready", true);

        assertThat(resource.getStatus("phase")).isEqualTo("Running");
        assertThat(resource.getStatus("replicas")).isEqualTo(3);
        assertThat(resource.getStatus("ready")).isEqualTo(true);
    }
}
