package io.elev8.codegen;

import com.squareup.javapoet.JavaFile;
import io.elev8.resources.crd.CRDNames;
import io.elev8.resources.crd.CRDVersion;
import io.elev8.resources.crd.CustomResourceDefinition;
import io.elev8.resources.crd.CustomResourceDefinitionSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceClassGeneratorTest {

    private ResourceClassGenerator generator;
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
        generator = new ResourceClassGenerator(config);
    }

    @Test
    void generateNamespacedResourceClass() {
        final CustomResourceDefinition crd = createCrd("CronTab", "stable.example.com", "Namespaced");
        final ResourceClassGenerator.ResourceContext context = new ResourceClassGenerator.ResourceContext(
                "CronTab",
                "stable.example.com/v1",
                "CronTabSpec",
                "CronTabStatus",
                true,
                true,
                true
        );

        final JavaFile result = generator.generate(crd, "v1", "com.example.generated", context);

        assertThat(result).isNotNull();
        assertThat(result.typeSpec.name).isEqualTo("CronTab");
        assertThat(result.packageName).isEqualTo("com.example.generated");

        final String code = result.toString();
        assertThat(code).contains("extends AbstractResource");
        assertThat(code).contains("@Getter");
        assertThat(code).contains("@Setter");
        assertThat(code).contains("private CronTabSpec spec;");
        assertThat(code).contains("private CronTabStatus status;");
        assertThat(code).contains("super(\"stable.example.com/v1\", \"CronTab\", null)");
        assertThat(code).contains("public static Builder builder()");
        assertThat(code).contains("public static final class Builder");
    }

    @Test
    void generateClusterScopedResourceClass() {
        final CustomResourceDefinition crd = createCrd("ClusterPolicy", "policy.example.com", "Cluster");
        final ResourceClassGenerator.ResourceContext context = new ResourceClassGenerator.ResourceContext(
                "ClusterPolicy",
                "policy.example.com/v1",
                "ClusterPolicySpec",
                "ClusterPolicyStatus",
                true,
                false,
                false
        );

        final JavaFile result = generator.generate(crd, "v1", "com.example.generated", context);

        final String code = result.toString();
        assertThat(code).contains("private ClusterPolicySpec spec;");
        assertThat(code).doesNotContain("private ClusterPolicyStatus status;");
        assertThat(code).doesNotContain("public Builder namespace");
    }

    @Test
    void generateResourceClassWithOnlySpec() {
        final CustomResourceDefinition crd = createCrd("SimpleResource", "example.com", "Namespaced");
        final ResourceClassGenerator.ResourceContext context = new ResourceClassGenerator.ResourceContext(
                "SimpleResource",
                "example.com/v1",
                "SimpleResourceSpec",
                "SimpleResourceStatus",
                true,
                false,
                true
        );

        final JavaFile result = generator.generate(crd, "v1", "com.example.generated", context);

        final String code = result.toString();
        assertThat(code).contains("private SimpleResourceSpec spec;");
        assertThat(code).doesNotContain("private SimpleResourceStatus status;");
        assertThat(code).doesNotContain("this.status = builder.status");
    }

    @Test
    void builderContainsMetadataMethods() {
        final CustomResourceDefinition crd = createCrd("Test", "example.com", "Namespaced");
        final ResourceClassGenerator.ResourceContext context = new ResourceClassGenerator.ResourceContext(
                "Test",
                "example.com/v1",
                "TestSpec",
                "TestStatus",
                true,
                true,
                true
        );

        final JavaFile result = generator.generate(crd, "v1", "com.example.generated", context);

        final String code = result.toString();
        assertThat(code).contains("public Builder metadata(final Metadata metadata)");
        assertThat(code).contains("public Builder name(final String name)");
        assertThat(code).contains("public Builder namespace(final String namespace)");
        assertThat(code).contains("public Builder label(final String key, final String value)");
        assertThat(code).contains("public Builder spec(final TestSpec spec)");
        assertThat(code).contains("public Builder status(final TestStatus status)");
    }

    @Test
    void builderContainsValidation() {
        final CustomResourceDefinition crd = createCrd("Test", "example.com", "Namespaced");
        final ResourceClassGenerator.ResourceContext context = new ResourceClassGenerator.ResourceContext(
                "Test",
                "example.com/v1",
                "TestSpec",
                "TestStatus",
                true,
                false,
                true
        );

        final JavaFile result = generator.generate(crd, "v1", "com.example.generated", context);

        final String code = result.toString();
        assertThat(code).contains("if (metadata == null || metadata.getName() == null)");
        assertThat(code).contains("throw new IllegalArgumentException");
        assertThat(code).contains("if (spec == null)");
    }

    @Test
    void writeToFile() throws Exception {
        final CustomResourceDefinition crd = createCrd("Test", "example.com", "Namespaced");
        final ResourceClassGenerator.ResourceContext context = new ResourceClassGenerator.ResourceContext(
                "Test",
                "example.com/v1",
                "TestSpec",
                "TestStatus",
                true,
                true,
                true
        );

        final JavaFile result = generator.generate(crd, "v1", "com.example.generated", context);
        generator.writeToFile(result, tempDir);

        final File generatedFile = new File(tempDir, "com/example/generated/Test.java");
        assertThat(generatedFile).exists();
    }

    private CustomResourceDefinition createCrd(final String kind, final String group, final String scope) {
        return CustomResourceDefinition.builder()
                .name(kind.toLowerCase() + "s." + group)
                .spec(CustomResourceDefinitionSpec.builder()
                        .group(group)
                        .scope(scope)
                        .names(CRDNames.builder()
                                .kind(kind)
                                .plural(kind.toLowerCase() + "s")
                                .build())
                        .version(CRDVersion.builder()
                                .name("v1")
                                .served(true)
                                .storage(true)
                                .build())
                        .build())
                .build();
    }
}
