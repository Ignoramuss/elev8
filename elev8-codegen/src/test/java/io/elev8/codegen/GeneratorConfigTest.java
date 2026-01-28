package io.elev8.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeneratorConfigTest {

    @TempDir
    File tempDir;

    @Test
    void buildWithDefaults() {
        final GeneratorConfig config = GeneratorConfig.builder()
                .crdDirectory(tempDir)
                .outputDirectory(tempDir)
                .targetPackage("com.example")
                .build();

        assertThat(config.isGenerateManagers()).isTrue();
        assertThat(config.isGenerateBuilders()).isTrue();
        assertThat(config.isUseLombok()).isTrue();
        assertThat(config.getExcludedCrds()).isEmpty();
    }

    @Test
    void buildWithCustomValues() {
        final GeneratorConfig config = GeneratorConfig.builder()
                .crdDirectory(tempDir)
                .outputDirectory(tempDir)
                .targetPackage("com.example")
                .generateManagers(false)
                .generateBuilders(false)
                .useLombok(false)
                .excludedCrds(Set.of("Test1", "Test2"))
                .build();

        assertThat(config.isGenerateManagers()).isFalse();
        assertThat(config.isGenerateBuilders()).isFalse();
        assertThat(config.isUseLombok()).isFalse();
        assertThat(config.getExcludedCrds()).containsExactlyInAnyOrder("Test1", "Test2");
    }

    @Test
    void validateWithValidConfig() {
        final GeneratorConfig config = GeneratorConfig.builder()
                .crdDirectory(tempDir)
                .outputDirectory(tempDir)
                .targetPackage("com.example.test")
                .build();

        config.validate();
    }

    @Test
    void validateWithNullCrdDirectory() {
        final GeneratorConfig config = GeneratorConfig.builder()
                .outputDirectory(tempDir)
                .targetPackage("com.example")
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CRD directory is required");
    }

    @Test
    void validateWithNonExistentCrdDirectory() {
        final GeneratorConfig config = GeneratorConfig.builder()
                .crdDirectory(new File("/nonexistent/path"))
                .outputDirectory(tempDir)
                .targetPackage("com.example")
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CRD directory does not exist");
    }

    @Test
    void validateWithNullOutputDirectory() {
        final GeneratorConfig config = GeneratorConfig.builder()
                .crdDirectory(tempDir)
                .targetPackage("com.example")
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Output directory is required");
    }

    @Test
    void validateWithNullTargetPackage() {
        final GeneratorConfig config = GeneratorConfig.builder()
                .crdDirectory(tempDir)
                .outputDirectory(tempDir)
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Target package is required");
    }

    @Test
    void validateWithEmptyTargetPackage() {
        final GeneratorConfig config = GeneratorConfig.builder()
                .crdDirectory(tempDir)
                .outputDirectory(tempDir)
                .targetPackage("")
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Target package is required");
    }

    @Test
    void validateWithInvalidPackageName() {
        final GeneratorConfig config = GeneratorConfig.builder()
                .crdDirectory(tempDir)
                .outputDirectory(tempDir)
                .targetPackage("com.123invalid")
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid target package name");
    }

    @Test
    void validateWithKeywordPackageName() {
        final GeneratorConfig config = GeneratorConfig.builder()
                .crdDirectory(tempDir)
                .outputDirectory(tempDir)
                .targetPackage("com.class.test")
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid target package name");
    }

    @Test
    void validateValidPackageNames() {
        for (final String validPackage : new String[] {
                "com.example",
                "io.elev8.generated",
                "org.test.package123",
                "com"
        }) {
            final GeneratorConfig config = GeneratorConfig.builder()
                    .crdDirectory(tempDir)
                    .outputDirectory(tempDir)
                    .targetPackage(validPackage)
                    .build();

            config.validate();
        }
    }
}
