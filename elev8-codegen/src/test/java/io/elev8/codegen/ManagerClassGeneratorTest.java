package io.elev8.codegen;

import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class ManagerClassGeneratorTest {

    private ManagerClassGenerator generator;
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
        generator = new ManagerClassGenerator(config);
    }

    @Test
    void generateNamespacedManager() {
        final ManagerClassGenerator.ManagerContext context = new ManagerClassGenerator.ManagerContext(
                "CronTab",
                "stable.example.com",
                "v1",
                "crontabs",
                true
        );

        final JavaFile result = generator.generate("com.example.generated", context);

        assertThat(result).isNotNull();
        assertThat(result.typeSpec.name).isEqualTo("CronTabManager");
        assertThat(result.packageName).isEqualTo("com.example.generated");

        final String code = result.toString();
        assertThat(code).contains("extends AbstractResourceManager<CronTab>");
        assertThat(code).contains("@Slf4j");
        assertThat(code).contains("public CronTabManager(final KubernetesClient client)");
        assertThat(code).contains("super(client, CronTab.class, \"/apis/stable.example.com/v1\")");
        assertThat(code).contains("protected String getResourceTypePlural()");
        assertThat(code).contains("return \"crontabs\"");
    }

    @Test
    void generateClusterScopedManager() {
        final ManagerClassGenerator.ManagerContext context = new ManagerClassGenerator.ManagerContext(
                "ClusterPolicy",
                "policy.example.com",
                "v1",
                "clusterpolicies",
                false
        );

        final JavaFile result = generator.generate("com.example.generated", context);

        final String code = result.toString();
        assertThat(code).contains("extends AbstractClusterResourceManager<ClusterPolicy>");
        assertThat(code).contains("super(client, ClusterPolicy.class, \"/apis/policy.example.com/v1\")");
        assertThat(code).contains("return \"clusterpolicies\"");
    }

    @Test
    void generateCoreResourceManager() {
        final ManagerClassGenerator.ManagerContext context = new ManagerClassGenerator.ManagerContext(
                "Pod",
                "",
                "v1",
                "pods",
                true
        );

        final JavaFile result = generator.generate("com.example.generated", context);

        final String code = result.toString();
        assertThat(code).contains("super(client, Pod.class, \"/api/v1\")");
    }

    @Test
    void managerContextApiPath() {
        final ManagerClassGenerator.ManagerContext coreContext = new ManagerClassGenerator.ManagerContext(
                "Pod", "", "v1", "pods", true
        );
        assertThat(coreContext.apiPath()).isEqualTo("/api/v1");

        final ManagerClassGenerator.ManagerContext customContext = new ManagerClassGenerator.ManagerContext(
                "CronTab", "stable.example.com", "v1", "crontabs", true
        );
        assertThat(customContext.apiPath()).isEqualTo("/apis/stable.example.com/v1");
    }

    @Test
    void managerContextManagerClassName() {
        final ManagerClassGenerator.ManagerContext context = new ManagerClassGenerator.ManagerContext(
                "CronTab", "stable.example.com", "v1", "crontabs", true
        );

        assertThat(context.managerClassName()).isEqualTo("CronTabManager");
    }

    @Test
    void writeToFile() throws Exception {
        final ManagerClassGenerator.ManagerContext context = new ManagerClassGenerator.ManagerContext(
                "Test", "example.com", "v1", "tests", true
        );

        final JavaFile result = generator.generate("com.example.generated", context);
        generator.writeToFile(result, tempDir);

        final File generatedFile = new File(tempDir, "com/example/generated/TestManager.java");
        assertThat(generatedFile).exists();
    }
}
