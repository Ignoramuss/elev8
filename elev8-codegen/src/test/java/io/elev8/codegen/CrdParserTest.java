package io.elev8.codegen;

import io.elev8.resources.crd.CRDVersion;
import io.elev8.resources.crd.CustomResourceDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrdParserTest {

    private CrdParser parser;

    @BeforeEach
    void setUp() {
        parser = new CrdParser();
    }

    @Test
    void parseYamlFile() throws Exception {
        final File crdFile = new File("src/test/resources/crds/crontab.yaml");
        final CustomResourceDefinition crd = parser.parse(crdFile);

        assertThat(crd).isNotNull();
        assertThat(crd.getKind()).isEqualTo("CustomResourceDefinition");
        assertThat(crd.getSpec()).isNotNull();
        assertThat(crd.getSpec().getGroup()).isEqualTo("stable.example.com");
        assertThat(crd.getSpec().getNames().getKind()).isEqualTo("CronTab");
        assertThat(crd.getSpec().getNames().getPlural()).isEqualTo("crontabs");
        assertThat(crd.getSpec().getScope()).isEqualTo("Namespaced");
        assertThat(crd.getSpec().getVersions()).hasSize(1);
    }

    @Test
    void parseClusterScopedCrd() throws Exception {
        final File crdFile = new File("src/test/resources/crds/clusterpolicy.yaml");
        final CustomResourceDefinition crd = parser.parse(crdFile);

        assertThat(crd).isNotNull();
        assertThat(crd.getSpec().getNames().getKind()).isEqualTo("ClusterPolicy");
        assertThat(crd.getSpec().getScope()).isEqualTo("Cluster");
    }

    @Test
    void parseInputStream() throws Exception {
        final String yaml = """
                apiVersion: apiextensions.k8s.io/v1
                kind: CustomResourceDefinition
                metadata:
                  name: tests.example.com
                spec:
                  group: example.com
                  names:
                    kind: Test
                    plural: tests
                  scope: Namespaced
                  versions:
                    - name: v1
                      served: true
                      storage: true
                """;

        final CustomResourceDefinition crd = parser.parse(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)),
                CrdParser.FileType.YAML);

        assertThat(crd).isNotNull();
        assertThat(crd.getSpec().getNames().getKind()).isEqualTo("Test");
    }

    @Test
    void parseDirectory(@TempDir File tempDir) throws Exception {
        writeTestCrd(tempDir, "test1.yaml", "Test1");
        writeTestCrd(tempDir, "test2.yaml", "Test2");

        final List<CustomResourceDefinition> crds = parser.parseDirectory(tempDir);

        assertThat(crds).hasSize(2);
    }

    @Test
    void getStorageVersion() throws Exception {
        final File crdFile = new File("src/test/resources/crds/crontab.yaml");
        final CustomResourceDefinition crd = parser.parse(crdFile);

        final CRDVersion storageVersion = parser.getStorageVersion(crd);

        assertThat(storageVersion).isNotNull();
        assertThat(storageVersion.getName()).isEqualTo("v1");
        assertThat(storageVersion.getStorage()).isTrue();
    }

    @Test
    void parseNullFile() {
        assertThatThrownBy(() -> parser.parse((File) null))
                .isInstanceOf(CrdParseException.class)
                .hasMessage("File cannot be null");
    }

    @Test
    void parseNonExistentFile() {
        assertThatThrownBy(() -> parser.parse(new File("nonexistent.yaml")))
                .isInstanceOf(CrdParseException.class)
                .hasMessageContaining("File does not exist");
    }

    @Test
    void parseInvalidYaml() {
        final String invalidYaml = "not: valid: yaml: :";

        assertThatThrownBy(() -> parser.parse(
                new ByteArrayInputStream(invalidYaml.getBytes(StandardCharsets.UTF_8)),
                CrdParser.FileType.YAML))
                .isInstanceOf(CrdParseException.class);
    }

    @Test
    void parseNonCrdResource() {
        final String notCrd = """
                apiVersion: v1
                kind: Pod
                metadata:
                  name: test-pod
                """;

        assertThatThrownBy(() -> parser.parse(
                new ByteArrayInputStream(notCrd.getBytes(StandardCharsets.UTF_8)),
                CrdParser.FileType.YAML))
                .isInstanceOf(CrdParseException.class)
                .hasMessageContaining("Invalid kind");
    }

    @Test
    void parseMissingSpec() {
        final String missingSpec = """
                apiVersion: apiextensions.k8s.io/v1
                kind: CustomResourceDefinition
                metadata:
                  name: tests.example.com
                """;

        assertThatThrownBy(() -> parser.parse(
                new ByteArrayInputStream(missingSpec.getBytes(StandardCharsets.UTF_8)),
                CrdParser.FileType.YAML))
                .isInstanceOf(CrdParseException.class)
                .hasMessageContaining("CRD spec is missing");
    }

    @Test
    void parseMissingGroup() {
        final String missingGroup = """
                apiVersion: apiextensions.k8s.io/v1
                kind: CustomResourceDefinition
                metadata:
                  name: tests.example.com
                spec:
                  names:
                    kind: Test
                    plural: tests
                  scope: Namespaced
                  versions:
                    - name: v1
                      served: true
                      storage: true
                """;

        assertThatThrownBy(() -> parser.parse(
                new ByteArrayInputStream(missingGroup.getBytes(StandardCharsets.UTF_8)),
                CrdParser.FileType.YAML))
                .isInstanceOf(CrdParseException.class)
                .hasMessageContaining("spec.group is required");
    }

    private void writeTestCrd(final File dir, final String filename, final String kind) throws IOException {
        final String yaml = String.format("""
                apiVersion: apiextensions.k8s.io/v1
                kind: CustomResourceDefinition
                metadata:
                  name: %ss.example.com
                spec:
                  group: example.com
                  names:
                    kind: %s
                    plural: %ss
                  scope: Namespaced
                  versions:
                    - name: v1
                      served: true
                      storage: true
                """, kind.toLowerCase(), kind, kind.toLowerCase());

        try (final FileWriter writer = new FileWriter(new File(dir, filename))) {
            writer.write(yaml);
        }
    }
}
