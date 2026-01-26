package io.elev8.resources.crd;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.elev8.resources.crd.schema.ExternalDocumentation;
import io.elev8.resources.crd.schema.JSONSchemaProps;
import io.elev8.resources.crd.schema.ValidationRule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JSONSchemaPropsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildSimpleStringSchema() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("string")
                .description("A simple string field")
                .minLength(1L)
                .maxLength(255L)
                .pattern("^[a-z]+$")
                .build();

        assertThat(schema.getType()).isEqualTo("string");
        assertThat(schema.getDescription()).isEqualTo("A simple string field");
        assertThat(schema.getMinLength()).isEqualTo(1L);
        assertThat(schema.getMaxLength()).isEqualTo(255L);
        assertThat(schema.getPattern()).isEqualTo("^[a-z]+$");
    }

    @Test
    void shouldBuildIntegerSchemaWithConstraints() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("integer")
                .minimum(0)
                .maximum(100)
                .exclusiveMinimum(false)
                .exclusiveMaximum(true)
                .build();

        assertThat(schema.getType()).isEqualTo("integer");
        assertThat(schema.getMinimum()).isEqualTo(0);
        assertThat(schema.getMaximum()).isEqualTo(100);
        assertThat(schema.getExclusiveMinimum()).isFalse();
        assertThat(schema.getExclusiveMaximum()).isTrue();
    }

    @Test
    void shouldBuildObjectSchemaWithProperties() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .property("name", JSONSchemaProps.builder()
                        .type("string")
                        .build())
                .property("count", JSONSchemaProps.builder()
                        .type("integer")
                        .build())
                .requiredField("name")
                .build();

        assertThat(schema.getType()).isEqualTo("object");
        assertThat(schema.getProperties()).hasSize(2);
        assertThat(schema.getProperties()).containsKey("name");
        assertThat(schema.getProperties()).containsKey("count");
        assertThat(schema.getRequired()).containsExactly("name");
    }

    @Test
    void shouldBuildArraySchema() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("array")
                .items(JSONSchemaProps.builder()
                        .type("string")
                        .build())
                .minItems(1L)
                .maxItems(10L)
                .uniqueItems(true)
                .build();

        assertThat(schema.getType()).isEqualTo("array");
        assertThat(schema.getItems().getType()).isEqualTo("string");
        assertThat(schema.getMinItems()).isEqualTo(1L);
        assertThat(schema.getMaxItems()).isEqualTo(10L);
        assertThat(schema.getUniqueItems()).isTrue();
    }

    @Test
    void shouldBuildSchemaWithEnumValues() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("string")
                .enumValue("option1")
                .enumValue("option2")
                .enumValue("option3")
                .build();

        assertThat(schema.getEnumValues()).containsExactly("option1", "option2", "option3");
    }

    @Test
    void shouldBuildSchemaWithKubernetesExtensions() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .xKubernetesPreserveUnknownFields(true)
                .xKubernetesEmbeddedResource(true)
                .xKubernetesIntOrString(false)
                .xKubernetesListType("map")
                .xKubernetesListMapKey("name")
                .xKubernetesMapType("granular")
                .build();

        assertThat(schema.getXKubernetesPreserveUnknownFields()).isTrue();
        assertThat(schema.getXKubernetesEmbeddedResource()).isTrue();
        assertThat(schema.getXKubernetesIntOrString()).isFalse();
        assertThat(schema.getXKubernetesListType()).isEqualTo("map");
        assertThat(schema.getXKubernetesListMapKeys()).containsExactly("name");
        assertThat(schema.getXKubernetesMapType()).isEqualTo("granular");
    }

    @Test
    void shouldBuildSchemaWithCELValidation() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .xKubernetesValidation(ValidationRule.builder()
                        .rule("self.minReplicas <= self.maxReplicas")
                        .message("minReplicas must be less than or equal to maxReplicas")
                        .build())
                .xKubernetesValidation(ValidationRule.builder()
                        .rule("self.replicas >= self.minReplicas")
                        .message("replicas must be at least minReplicas")
                        .reason("FieldValueInvalid")
                        .build())
                .build();

        assertThat(schema.getXKubernetesValidations()).hasSize(2);
        assertThat(schema.getXKubernetesValidations().get(0).getRule())
                .isEqualTo("self.minReplicas <= self.maxReplicas");
        assertThat(schema.getXKubernetesValidations().get(1).getReason())
                .isEqualTo("FieldValueInvalid");
    }

    @Test
    void shouldBuildSchemaWithExternalDocs() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .description("A custom resource")
                .externalDocs(ExternalDocumentation.builder()
                        .description("API Documentation")
                        .url("https://example.com/docs")
                        .build())
                .build();

        assertThat(schema.getExternalDocs()).isNotNull();
        assertThat(schema.getExternalDocs().getDescription()).isEqualTo("API Documentation");
        assertThat(schema.getExternalDocs().getUrl()).isEqualTo("https://example.com/docs");
    }

    @Test
    void shouldBuildSchemaWithNullable() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("string")
                .nullable(true)
                .build();

        assertThat(schema.getNullable()).isTrue();
    }

    @Test
    void shouldSerializeWithJsonPropertyAnnotations() throws Exception {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("string")
                .enumValue("a")
                .enumValue("b")
                .defaultValue("a")
                .xKubernetesPreserveUnknownFields(true)
                .build();

        final String json = objectMapper.writeValueAsString(schema);

        assertThat(json).contains("\"enum\":[\"a\",\"b\"]");
        assertThat(json).contains("\"default\":\"a\"");
        assertThat(json).contains("\"x-kubernetes-preserve-unknown-fields\":true");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "type": "object",
                  "properties": {
                    "name": {
                      "type": "string",
                      "minLength": 1
                    },
                    "replicas": {
                      "type": "integer",
                      "minimum": 0,
                      "maximum": 100
                    }
                  },
                  "required": ["name"],
                  "x-kubernetes-preserve-unknown-fields": true
                }
                """;

        final JSONSchemaProps schema = objectMapper.readValue(json, JSONSchemaProps.class);

        assertThat(schema.getType()).isEqualTo("object");
        assertThat(schema.getProperties()).hasSize(2);
        assertThat(schema.getProperties().get("name").getType()).isEqualTo("string");
        assertThat(schema.getProperties().get("name").getMinLength()).isEqualTo(1L);
        assertThat(schema.getProperties().get("replicas").getType()).isEqualTo("integer");
        assertThat(schema.getRequired()).containsExactly("name");
        assertThat(schema.getXKubernetesPreserveUnknownFields()).isTrue();
    }

    @Test
    void shouldDeserializeSchemaWithRef() throws Exception {
        final String json = """
                {
                  "$ref": "#/definitions/MyType",
                  "$schema": "http://json-schema.org/draft-04/schema#"
                }
                """;

        final JSONSchemaProps schema = objectMapper.readValue(json, JSONSchemaProps.class);

        assertThat(schema.getRef()).isEqualTo("#/definitions/MyType");
        assertThat(schema.getSchema()).isEqualTo("http://json-schema.org/draft-04/schema#");
    }
}
