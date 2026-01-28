package io.elev8.codegen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractClusterResourceManager;
import io.elev8.resources.AbstractResourceManager;
import lombok.extern.slf4j.Slf4j;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;

/**
 * Generator for resource manager classes that extend AbstractResourceManager or AbstractClusterResourceManager.
 */
@Slf4j
public class ManagerClassGenerator {

    private final GeneratorConfig config;

    public ManagerClassGenerator(final GeneratorConfig config) {
        this.config = config;
    }

    /**
     * Context for generating a manager class.
     */
    public record ManagerContext(
            String resourceClassName,
            String apiGroup,
            String version,
            String plural,
            boolean isNamespaced
    ) {
        public String managerClassName() {
            return resourceClassName + "Manager";
        }

        public String apiPath() {
            if (apiGroup == null || apiGroup.isEmpty()) {
                return "/api/" + version;
            }
            return "/apis/" + apiGroup + "/" + version;
        }
    }

    /**
     * Generate a manager class for a resource.
     *
     * @param targetPackage the target package
     * @param context       the manager context
     * @return the generated JavaFile
     */
    public JavaFile generate(final String targetPackage, final ManagerContext context) {
        final String managerClassName = context.managerClassName();
        final ClassName resourceClass = ClassName.get(targetPackage, context.resourceClassName());
        final Class<?> baseClass = context.isNamespaced()
                ? AbstractResourceManager.class
                : AbstractClusterResourceManager.class;

        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(managerClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.get(baseClass), resourceClass));

        if (config.isUseLombok()) {
            classBuilder.addAnnotation(Slf4j.class);
        }

        addConstructor(classBuilder, resourceClass, context);
        addGetResourceTypePluralMethod(classBuilder, context);

        final TypeSpec typeSpec = classBuilder.build();
        return JavaFile.builder(targetPackage, typeSpec)
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    /**
     * Write the generated class to the output directory.
     *
     * @param javaFile        the generated JavaFile
     * @param outputDirectory the output directory
     * @throws IOException if writing fails
     */
    public void writeToFile(final JavaFile javaFile, final File outputDirectory) throws IOException {
        javaFile.writeTo(outputDirectory);
    }

    private void addConstructor(final TypeSpec.Builder classBuilder, final ClassName resourceClass,
                                final ManagerContext context) {
        classBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(KubernetesClient.class, "client", Modifier.FINAL)
                .addStatement("super(client, $T.class, $S)", resourceClass, context.apiPath())
                .build());
    }

    private void addGetResourceTypePluralMethod(final TypeSpec.Builder classBuilder,
                                                 final ManagerContext context) {
        classBuilder.addMethod(MethodSpec.methodBuilder("getResourceTypePlural")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .returns(String.class)
                .addStatement("return $S", context.plural())
                .build());
    }
}
