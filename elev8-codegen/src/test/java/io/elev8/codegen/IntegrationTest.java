package io.elev8.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationTest {

    @TempDir
    File outputDir;

    private SchemaToJavaGenerator generator;

    @BeforeEach
    void setUp() {
        final File crdDir = new File("src/test/resources/crds");
        final GeneratorConfig config = GeneratorConfig.builder()
                .crdDirectory(crdDir)
                .outputDirectory(outputDir)
                .targetPackage("com.example.generated")
                .generateManagers(true)
                .useLombok(true)
                .build();
        generator = new SchemaToJavaGenerator(config);
    }

    @Test
    void generateFromCrontabCrd() throws Exception {
        final File crdFile = new File("src/test/resources/crds/crontab.yaml");
        final CrdParser parser = new CrdParser();
        final var crd = parser.parse(crdFile);

        final SchemaToJavaGenerator.GenerationResult result = generator.generateFromCrd(crd);

        assertThat(result.success()).isTrue();
        assertThat(result.resourceName()).isEqualTo("CronTab");
        assertThat(result.generatedFiles()).isNotEmpty();

        assertFileExists("CronTab.java");
        assertFileExists("CronTabSpec.java");
        assertFileExists("CronTabStatus.java");
        assertFileExists("CronTabManager.java");
        assertFileExists("CronTabSpecResources.java");
        assertFileExists("CronTabSpecEnv.java");
        assertFileExists("CronTabStatusCondition.java");

        final String cronTabCode = readGeneratedFile("CronTab.java");
        assertThat(cronTabCode).contains("extends AbstractResource");
        assertThat(cronTabCode).contains("private CronTabSpec spec");
        assertThat(cronTabCode).contains("private CronTabStatus status");
        assertThat(cronTabCode).contains("super(\"stable.example.com/v1\", \"CronTab\"");
        assertThat(cronTabCode).contains("public Builder namespace");

        final String specCode = readGeneratedFile("CronTabSpec.java");
        assertThat(specCode).contains("private String cronSpec");
        assertThat(specCode).contains("private String image");
        assertThat(specCode).contains("private Integer replicas");
        assertThat(specCode).contains("private Boolean suspend");
        assertThat(specCode).contains("private CronTabSpecResources resources");
        assertThat(specCode).contains("private List<CronTabSpecEnv> env");
        assertThat(specCode).contains("private Map<String, String> labels");

        final String statusCode = readGeneratedFile("CronTabStatus.java");
        assertThat(statusCode).contains("private Integer replicas");
        assertThat(statusCode).contains("private Instant lastScheduleTime");
        assertThat(statusCode).contains("private List<CronTabStatusCondition> conditions");

        final String managerCode = readGeneratedFile("CronTabManager.java");
        assertThat(managerCode).contains("extends AbstractResourceManager<CronTab>");
        assertThat(managerCode).contains("/apis/stable.example.com/v1");
        assertThat(managerCode).contains("return \"crontabs\"");
    }

    @Test
    void generateFromClusterPolicyCrd() throws Exception {
        final File crdFile = new File("src/test/resources/crds/clusterpolicy.yaml");
        final CrdParser parser = new CrdParser();
        final var crd = parser.parse(crdFile);

        final SchemaToJavaGenerator.GenerationResult result = generator.generateFromCrd(crd);

        assertThat(result.success()).isTrue();
        assertThat(result.resourceName()).isEqualTo("ClusterPolicy");

        assertFileExists("ClusterPolicy.java");
        assertFileExists("ClusterPolicySpec.java");
        assertFileExists("ClusterPolicyManager.java");

        final String resourceCode = readGeneratedFile("ClusterPolicy.java");
        assertThat(resourceCode).doesNotContain("public Builder namespace");

        final String managerCode = readGeneratedFile("ClusterPolicyManager.java");
        assertThat(managerCode).contains("extends AbstractClusterResourceManager<ClusterPolicy>");
        assertThat(managerCode).contains("return \"clusterpolicies\"");
    }

    @Test
    void generateAll() throws Exception {
        final List<SchemaToJavaGenerator.GenerationResult> results = generator.generateAll();

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(SchemaToJavaGenerator.GenerationResult::success);

        assertFileExists("CronTab.java");
        assertFileExists("ClusterPolicy.java");
    }

    @Test
    void generatedCodeCompiles() throws Exception {
        generator.generateAll();

        final String specCode = readGeneratedFile("CronTabSpec.java");
        assertThat(specCode).contains("@Data");
        assertThat(specCode).contains("@Builder");
        assertThat(specCode).contains("@Jacksonized");
        assertThat(specCode).contains("@JsonInclude(JsonInclude.Include.NON_NULL)");

        assertThat(specCode).contains("import com.fasterxml.jackson.annotation.JsonInclude;");
        assertThat(specCode).contains("import lombok.Builder;");
        assertThat(specCode).contains("import lombok.Data;");
    }

    private void assertFileExists(final String filename) {
        final File file = new File(outputDir, "com/example/generated/" + filename);
        assertThat(file).withFailMessage("Expected file to exist: " + filename).exists();
    }

    private String readGeneratedFile(final String filename) throws Exception {
        final File file = new File(outputDir, "com/example/generated/" + filename);
        return Files.readString(file.toPath());
    }
}
