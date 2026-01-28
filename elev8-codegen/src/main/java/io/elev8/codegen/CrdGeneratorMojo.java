package io.elev8.codegen;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Maven plugin for generating Java classes from Kubernetes CustomResourceDefinition schemas.
 *
 * <p>This plugin reads CRD YAML/JSON files and generates:
 * <ul>
 *   <li>Resource class extending AbstractResource</li>
 *   <li>Spec and Status classes with all fields</li>
 *   <li>Nested types for complex objects</li>
 *   <li>ResourceManager class for CRUD operations</li>
 * </ul>
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class CrdGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Directory containing CRD YAML/JSON files.
     */
    @Parameter(property = "crdDirectory", defaultValue = "${project.basedir}/src/main/resources/crds")
    private File crdDirectory;

    /**
     * Output directory for generated sources.
     */
    @Parameter(property = "outputDirectory",
            defaultValue = "${project.build.directory}/generated-sources/crd")
    private File outputDirectory;

    /**
     * Target package for generated classes.
     */
    @Parameter(property = "targetPackage", required = true)
    private String targetPackage;

    /**
     * Whether to generate manager classes.
     */
    @Parameter(property = "generateManagers", defaultValue = "true")
    private boolean generateManagers;

    /**
     * Whether to generate builders for resource classes.
     */
    @Parameter(property = "generateBuilders", defaultValue = "true")
    private boolean generateBuilders;

    /**
     * Whether to use Lombok annotations in generated classes.
     */
    @Parameter(property = "useLombok", defaultValue = "true")
    private boolean useLombok;

    /**
     * List of CRD kinds to exclude from generation.
     */
    @Parameter(property = "excludedCrds")
    private List<String> excludedCrds;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Elev8 CRD Code Generator");
        getLog().info("CRD Directory: " + crdDirectory);
        getLog().info("Output Directory: " + outputDirectory);
        getLog().info("Target Package: " + targetPackage);

        if (!crdDirectory.exists()) {
            getLog().warn("CRD directory does not exist: " + crdDirectory);
            return;
        }

        try {
            final Set<String> excluded = excludedCrds != null
                    ? new HashSet<>(excludedCrds)
                    : Set.of();

            final GeneratorConfig config = GeneratorConfig.builder()
                    .crdDirectory(crdDirectory)
                    .outputDirectory(outputDirectory)
                    .targetPackage(targetPackage)
                    .generateManagers(generateManagers)
                    .generateBuilders(generateBuilders)
                    .useLombok(useLombok)
                    .excludedCrds(excluded)
                    .build();

            final SchemaToJavaGenerator generator = new SchemaToJavaGenerator(config);
            final List<SchemaToJavaGenerator.GenerationResult> results = generator.generateAll();

            int successCount = 0;
            int failureCount = 0;

            for (final SchemaToJavaGenerator.GenerationResult result : results) {
                if (result.success()) {
                    successCount++;
                    getLog().info("Generated " + result.generatedFiles().size()
                            + " classes for: " + result.resourceName());
                } else {
                    failureCount++;
                    getLog().error("Failed to generate code for: " + result.resourceName()
                            + " - " + result.errorMessage());
                }
            }

            getLog().info("Generation complete. Success: " + successCount + ", Failures: " + failureCount);

            if (failureCount > 0) {
                throw new MojoFailureException("Failed to generate code for " + failureCount + " CRD(s)");
            }

            if (project != null && outputDirectory.exists()) {
                project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
                getLog().info("Added generated sources to compile path: " + outputDirectory);
            }

        } catch (CrdParseException e) {
            throw new MojoExecutionException("Failed to parse CRD files", e);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate code", e);
        }
    }
}
