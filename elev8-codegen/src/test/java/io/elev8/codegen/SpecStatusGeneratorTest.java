package io.elev8.codegen;

import io.elev8.resources.crd.schema.JSONSchemaProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class SpecStatusGeneratorTest {

    private SpecStatusGenerator generator;
    private GeneratorConfig config;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        config = GeneratorConfig.builder()
                .crdDirectory(tempDir)
                .outputDirectory(tempDir)
                .targetPackage("com.example.generated")
                .useLombok(true)
                .build();
        generator = new SpecStatusGenerator(new TypeMapper(), config);
    }

    @Test
    void generateSimpleSpec() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .property("name", JSONSchemaProps.builder()
                        .type("string")
                        .description("The resource name")
                        .build())
                .property("replicas", JSONSchemaProps.builder()
                        .type("integer")
                        .build())
                .build();

        final SpecStatusGenerator.GenerationResult result = generator.generate(
                schema, "TestSpec", "com.example.generated");

        assertThat(result.mainClass()).isNotNull();
        assertThat(result.mainClass().typeSpec.name).isEqualTo("TestSpec");
        assertThat(result.mainClass().packageName).isEqualTo("com.example.generated");

        final String code = result.mainClass().toString();
        assertThat(code).contains("private String name;");
        assertThat(code).contains("private Integer replicas;");
        assertThat(code).contains("@Data");
        assertThat(code).contains("@Builder");
        assertThat(code).contains("@Jacksonized");
    }

    @Test
    void generateSpecWithNestedObject() {
        final JSONSchemaProps nestedSchema = JSONSchemaProps.builder()
                .type("object")
                .property("cpu", JSONSchemaProps.builder().type("string").build())
                .property("memory", JSONSchemaProps.builder().type("string").build())
                .build();

        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .property("resources", nestedSchema)
                .build();

        final SpecStatusGenerator.GenerationResult result = generator.generate(
                schema, "TestSpec", "com.example.generated");

        assertThat(result.nestedClasses()).hasSize(1);
        assertThat(result.nestedClasses().get(0).typeSpec.name).isEqualTo("TestSpecResources");
    }

    @Test
    void generateSpecWithArray() {
        final JSONSchemaProps itemSchema = JSONSchemaProps.builder()
                .type("object")
                .property("name", JSONSchemaProps.builder().type("string").build())
                .property("value", JSONSchemaProps.builder().type("string").build())
                .build();

        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .property("env", JSONSchemaProps.builder()
                        .type("array")
                        .items(itemSchema)
                        .build())
                .build();

        final SpecStatusGenerator.GenerationResult result = generator.generate(
                schema, "TestSpec", "com.example.generated");

        final String code = result.mainClass().toString();
        assertThat(code).contains("List<TestSpecEnv>");
        assertThat(code).contains("@Singular");

        assertThat(result.nestedClasses()).hasSize(1);
        assertThat(result.nestedClasses().get(0).typeSpec.name).isEqualTo("TestSpecEnv");
    }

    @Test
    void generateSpecWithMap() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .property("labels", JSONSchemaProps.builder()
                        .type("object")
                        .additionalProperties(JSONSchemaProps.builder().type("string").build())
                        .build())
                .build();

        final SpecStatusGenerator.GenerationResult result = generator.generate(
                schema, "TestSpec", "com.example.generated");

        final String code = result.mainClass().toString();
        assertThat(code).contains("Map<String, String> labels");
        assertThat(code).contains("@Singular");
    }

    @Test
    void generateWithJsonPropertyAnnotation() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .property("my-field", JSONSchemaProps.builder().type("string").build())
                .build();

        final SpecStatusGenerator.GenerationResult result = generator.generate(
                schema, "TestSpec", "com.example.generated");

        final String code = result.mainClass().toString();
        assertThat(code).contains("private String myField;");
        assertThat(code).contains("@JsonProperty(\"my-field\")");
    }

    @Test
    void generateWithDescription() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .property("name", JSONSchemaProps.builder()
                        .type("string")
                        .description("The resource name")
                        .build())
                .build();

        final SpecStatusGenerator.GenerationResult result = generator.generate(
                schema, "TestSpec", "com.example.generated");

        final String code = result.mainClass().toString();
        assertThat(code).contains("The resource name");
    }

    @Test
    void generateWithDateTimeFormat() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .property("createdAt", JSONSchemaProps.builder()
                        .type("string")
                        .format("date-time")
                        .build())
                .build();

        final SpecStatusGenerator.GenerationResult result = generator.generate(
                schema, "TestStatus", "com.example.generated");

        final String code = result.mainClass().toString();
        assertThat(code).contains("Instant createdAt");
    }

    @Test
    void writeToFile() throws Exception {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .property("name", JSONSchemaProps.builder().type("string").build())
                .build();

        final SpecStatusGenerator.GenerationResult result = generator.generate(
                schema, "TestSpec", "com.example.generated");
        generator.writeToFile(result, tempDir);

        final File generatedFile = new File(tempDir, "com/example/generated/TestSpec.java");
        assertThat(generatedFile).exists();
    }
}
