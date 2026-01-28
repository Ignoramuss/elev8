package io.elev8.codegen;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import io.elev8.resources.crd.CustomResourceDefinition;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;

/**
 * Generator for resource classes that extend AbstractResource.
 */
@Slf4j
public class ResourceClassGenerator {

    private final GeneratorConfig config;

    public ResourceClassGenerator(final GeneratorConfig config) {
        this.config = config;
    }

    /**
     * Context for generating a resource class.
     */
    public record ResourceContext(
            String kind,
            String apiVersion,
            String specClassName,
            String statusClassName,
            boolean hasSpec,
            boolean hasStatus,
            boolean isNamespaced
    ) {
    }

    /**
     * Generate a resource class from CRD metadata.
     *
     * @param crd           the CustomResourceDefinition
     * @param version       the API version string (e.g., "stable.example.com/v1")
     * @param targetPackage the target package
     * @param context       the resource context
     * @return the generated JavaFile
     */
    public JavaFile generate(final CustomResourceDefinition crd,
                             final String version,
                             final String targetPackage,
                             final ResourceContext context) {
        final String className = context.kind();

        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .superclass(AbstractResource.class);

        addClassAnnotations(classBuilder);
        addFields(classBuilder, targetPackage, context);
        addDefaultConstructor(classBuilder, context.apiVersion(), className);
        addBuilderConstructor(classBuilder, context.apiVersion(), className, context);
        addBuilderMethod(classBuilder, className);
        addBuilderClass(classBuilder, className, targetPackage, context);

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

    private void addClassAnnotations(final TypeSpec.Builder classBuilder) {
        if (config.isUseLombok()) {
            classBuilder.addAnnotation(Getter.class);
            classBuilder.addAnnotation(Setter.class);
        }
        classBuilder.addAnnotation(AnnotationSpec.builder(JsonInclude.class)
                .addMember("value", "$T.NON_NULL", JsonInclude.Include.class)
                .build());
    }

    private void addFields(final TypeSpec.Builder classBuilder, final String targetPackage,
                           final ResourceContext context) {
        if (context.hasSpec()) {
            final ClassName specType = ClassName.get(targetPackage, context.specClassName());
            classBuilder.addField(FieldSpec.builder(specType, "spec", Modifier.PRIVATE).build());
        }
        if (context.hasStatus()) {
            final ClassName statusType = ClassName.get(targetPackage, context.statusClassName());
            classBuilder.addField(FieldSpec.builder(statusType, "status", Modifier.PRIVATE).build());
        }
    }

    private void addDefaultConstructor(final TypeSpec.Builder classBuilder, final String apiVersion,
                                       final String className) {
        classBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super($S, $S, null)", apiVersion, className)
                .build());
    }

    private void addBuilderConstructor(final TypeSpec.Builder classBuilder, final String apiVersion,
                                       final String className, final ResourceContext context) {
        final MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ClassName.bestGuess("Builder"), "builder")
                .addStatement("super($S, $S, builder.metadata)", apiVersion, className);

        if (context.hasSpec()) {
            constructorBuilder.addStatement("this.spec = builder.spec");
        }
        if (context.hasStatus()) {
            constructorBuilder.addStatement("this.status = builder.status");
        }

        classBuilder.addMethod(constructorBuilder.build());
    }

    private void addBuilderMethod(final TypeSpec.Builder classBuilder, final String className) {
        classBuilder.addMethod(MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.bestGuess("Builder"))
                .addStatement("return new Builder()")
                .build());
    }

    private void addBuilderClass(final TypeSpec.Builder classBuilder, final String resourceClassName,
                                 final String targetPackage, final ResourceContext context) {
        final TypeSpec.Builder builderBuilder = TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        builderBuilder.addField(Metadata.class, "metadata", Modifier.PRIVATE);

        if (context.hasSpec()) {
            final ClassName specType = ClassName.get(targetPackage, context.specClassName());
            builderBuilder.addField(specType, "spec", Modifier.PRIVATE);
        }
        if (context.hasStatus()) {
            final ClassName statusType = ClassName.get(targetPackage, context.statusClassName());
            builderBuilder.addField(statusType, "status", Modifier.PRIVATE);
        }

        builderBuilder.addMethod(MethodSpec.methodBuilder("metadata")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.bestGuess("Builder"))
                .addParameter(Metadata.class, "metadata", Modifier.FINAL)
                .addStatement("this.metadata = metadata")
                .addStatement("return this")
                .build());

        builderBuilder.addMethod(MethodSpec.methodBuilder("name")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.bestGuess("Builder"))
                .addParameter(String.class, "name", Modifier.FINAL)
                .beginControlFlow("if (this.metadata == null)")
                .addStatement("this.metadata = $T.builder().build()", Metadata.class)
                .endControlFlow()
                .addStatement("this.metadata.setName(name)")
                .addStatement("return this")
                .build());

        if (context.isNamespaced()) {
            builderBuilder.addMethod(MethodSpec.methodBuilder("namespace")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.bestGuess("Builder"))
                    .addParameter(String.class, "namespace", Modifier.FINAL)
                    .beginControlFlow("if (this.metadata == null)")
                    .addStatement("this.metadata = $T.builder().build()", Metadata.class)
                    .endControlFlow()
                    .addStatement("this.metadata.setNamespace(namespace)")
                    .addStatement("return this")
                    .build());
        }

        builderBuilder.addMethod(MethodSpec.methodBuilder("label")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.bestGuess("Builder"))
                .addParameter(String.class, "key", Modifier.FINAL)
                .addParameter(String.class, "value", Modifier.FINAL)
                .beginControlFlow("if (this.metadata == null)")
                .addStatement("this.metadata = $T.builder().build()", Metadata.class)
                .endControlFlow()
                .addStatement("this.metadata = $T.builder()" +
                        ".name(this.metadata.getName())" +
                        ".namespace(this.metadata.getNamespace())" +
                        ".labels(this.metadata.getLabels())" +
                        ".label(key, value)" +
                        ".build()", Metadata.class)
                .addStatement("return this")
                .build());

        if (context.hasSpec()) {
            final ClassName specType = ClassName.get(targetPackage, context.specClassName());
            builderBuilder.addMethod(MethodSpec.methodBuilder("spec")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.bestGuess("Builder"))
                    .addParameter(specType, "spec", Modifier.FINAL)
                    .addStatement("this.spec = spec")
                    .addStatement("return this")
                    .build());
        }

        if (context.hasStatus()) {
            final ClassName statusType = ClassName.get(targetPackage, context.statusClassName());
            builderBuilder.addMethod(MethodSpec.methodBuilder("status")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.bestGuess("Builder"))
                    .addParameter(statusType, "status", Modifier.FINAL)
                    .addStatement("this.status = status")
                    .addStatement("return this")
                    .build());
        }

        addBuildMethod(builderBuilder, resourceClassName, context);

        classBuilder.addType(builderBuilder.build());
    }

    private void addBuildMethod(final TypeSpec.Builder builderBuilder, final String resourceClassName,
                                final ResourceContext context) {
        final MethodSpec.Builder buildMethodBuilder = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.bestGuess(resourceClassName));

        buildMethodBuilder.beginControlFlow("if (metadata == null || metadata.getName() == null)")
                .addStatement("throw new $T($S + $S)",
                        IllegalArgumentException.class, resourceClassName, " name is required")
                .endControlFlow();

        if (context.hasSpec()) {
            buildMethodBuilder.beginControlFlow("if (spec == null)")
                    .addStatement("throw new $T($S + $S)",
                            IllegalArgumentException.class, resourceClassName, " spec is required")
                    .endControlFlow();
        }

        buildMethodBuilder.addStatement("return new $L(this)", resourceClassName);

        builderBuilder.addMethod(buildMethodBuilder.build());
    }
}
