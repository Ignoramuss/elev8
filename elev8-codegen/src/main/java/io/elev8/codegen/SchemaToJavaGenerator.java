package io.elev8.codegen;

import com.squareup.javapoet.JavaFile;
import io.elev8.resources.crd.CRDVersion;
import io.elev8.resources.crd.CustomResourceDefinition;
import io.elev8.resources.crd.schema.JSONSchemaProps;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main orchestrator for generating Java code from CRD schemas.
 */
@Slf4j
public class SchemaToJavaGenerator {

    private final GeneratorConfig config;
    private final CrdParser crdParser;
    private final TypeMapper typeMapper;
    private final SpecStatusGenerator specStatusGenerator;
    private final ResourceClassGenerator resourceClassGenerator;
    private final ManagerClassGenerator managerClassGenerator;

    public SchemaToJavaGenerator(final GeneratorConfig config) {
        this.config = config;
        this.crdParser = new CrdParser();
        this.typeMapper = new TypeMapper();
        this.specStatusGenerator = new SpecStatusGenerator(typeMapper, config);
        this.resourceClassGenerator = new ResourceClassGenerator(config);
        this.managerClassGenerator = new ManagerClassGenerator(config);
    }

    /**
     * Result of generating code from a CRD.
     */
    public record GenerationResult(
            String resourceName,
            List<JavaFile> generatedFiles,
            boolean success,
            String errorMessage
    ) {
        public static GenerationResult success(final String resourceName, final List<JavaFile> files) {
            return new GenerationResult(resourceName, files, true, null);
        }

        public static GenerationResult failure(final String resourceName, final String error) {
            return new GenerationResult(resourceName, List.of(), false, error);
        }
    }

    /**
     * Generate Java code from all CRDs in the configured directory.
     *
     * @return list of generation results
     * @throws CrdParseException if parsing fails
     * @throws IOException       if writing fails
     */
    public List<GenerationResult> generateAll() throws CrdParseException, IOException {
        config.validate();
        ensureOutputDirectoryExists();

        final List<CustomResourceDefinition> crds = crdParser.parseDirectory(config.getCrdDirectory());
        final List<GenerationResult> results = new ArrayList<>();

        for (final CustomResourceDefinition crd : crds) {
            results.add(generateFromCrd(crd));
        }

        return results;
    }

    /**
     * Generate Java code from a single CRD.
     *
     * @param crd the CustomResourceDefinition
     * @return the generation result
     */
    public GenerationResult generateFromCrd(final CustomResourceDefinition crd) {
        final String kind = crd.getSpec().getNames().getKind();

        try {
            if (config.getExcludedCrds().contains(kind)) {
                log.info("Skipping excluded CRD: {}", kind);
                return GenerationResult.success(kind, List.of());
            }

            final CRDVersion version = crdParser.getStorageVersion(crd);
            if (version == null) {
                return GenerationResult.failure(kind, "No storage version found");
            }

            final List<JavaFile> generatedFiles = new ArrayList<>();
            final String group = crd.getSpec().getGroup();
            final String versionName = version.getName();
            final String apiVersion = group + "/" + versionName;
            final boolean isNamespaced = "Namespaced".equals(crd.getSpec().getScope());

            final JSONSchemaProps rootSchema = getRootSchema(version);
            final JSONSchemaProps specSchema = getPropertySchema(rootSchema, "spec");
            final JSONSchemaProps statusSchema = getPropertySchema(rootSchema, "status");

            final boolean hasSpec = specSchema != null;
            final boolean hasStatus = statusSchema != null;

            final String specClassName = kind + "Spec";
            final String statusClassName = kind + "Status";

            if (hasSpec) {
                log.info("Generating spec class: {}", specClassName);
                final SpecStatusGenerator.GenerationResult specResult =
                        specStatusGenerator.generate(specSchema, specClassName, config.getTargetPackage());
                generatedFiles.add(specResult.mainClass());
                generatedFiles.addAll(specResult.nestedClasses());
            }

            if (hasStatus) {
                log.info("Generating status class: {}", statusClassName);
                final SpecStatusGenerator.GenerationResult statusResult =
                        specStatusGenerator.generate(statusSchema, statusClassName, config.getTargetPackage());
                generatedFiles.add(statusResult.mainClass());
                generatedFiles.addAll(statusResult.nestedClasses());
            }

            log.info("Generating resource class: {}", kind);
            final ResourceClassGenerator.ResourceContext resourceContext =
                    new ResourceClassGenerator.ResourceContext(
                            kind, apiVersion, specClassName, statusClassName, hasSpec, hasStatus, isNamespaced);
            final JavaFile resourceFile = resourceClassGenerator.generate(
                    crd, versionName, config.getTargetPackage(), resourceContext);
            generatedFiles.add(resourceFile);

            if (config.isGenerateManagers()) {
                log.info("Generating manager class: {}Manager", kind);
                final ManagerClassGenerator.ManagerContext managerContext =
                        new ManagerClassGenerator.ManagerContext(
                                kind, group, versionName, crd.getSpec().getNames().getPlural(), isNamespaced);
                final JavaFile managerFile = managerClassGenerator.generate(
                        config.getTargetPackage(), managerContext);
                generatedFiles.add(managerFile);
            }

            writeFiles(generatedFiles);

            return GenerationResult.success(kind, generatedFiles);

        } catch (Exception e) {
            log.error("Failed to generate code for CRD: {}", kind, e);
            return GenerationResult.failure(kind, e.getMessage());
        }
    }

    private void ensureOutputDirectoryExists() throws IOException {
        final File outputDir = config.getOutputDirectory();
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Failed to create output directory: " + outputDir.getAbsolutePath());
        }
    }

    private JSONSchemaProps getRootSchema(final CRDVersion version) {
        if (version.getSchema() == null) {
            return null;
        }
        return version.getSchema().getOpenAPIV3Schema();
    }

    private JSONSchemaProps getPropertySchema(final JSONSchemaProps rootSchema, final String propertyName) {
        if (rootSchema == null || rootSchema.getProperties() == null) {
            return null;
        }
        return rootSchema.getProperties().get(propertyName);
    }

    private void writeFiles(final List<JavaFile> files) throws IOException {
        for (final JavaFile file : files) {
            log.debug("Writing file: {}.{}", file.packageName, file.typeSpec.name);
            file.writeTo(config.getOutputDirectory());
        }
    }
}
