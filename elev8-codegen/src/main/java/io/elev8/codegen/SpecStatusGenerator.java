package io.elev8.codegen;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.elev8.resources.crd.schema.JSONSchemaProps;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generator for Spec and Status classes from JSON Schema.
 */
@Slf4j
public class SpecStatusGenerator {

    private final TypeMapper typeMapper;
    private final GeneratorConfig config;

    public SpecStatusGenerator(final TypeMapper typeMapper, final GeneratorConfig config) {
        this.typeMapper = typeMapper;
        this.config = config;
    }

    /**
     * Result of generating a class, including the main class and any nested classes.
     */
    public record GenerationResult(JavaFile mainClass, List<JavaFile> nestedClasses) {
    }

    /**
     * Generate a spec or status class from a JSON Schema.
     *
     * @param schema    the JSON Schema
     * @param className the class name to generate
     * @param targetPackage the target package
     * @return the generation result
     */
    public GenerationResult generate(final JSONSchemaProps schema, final String className,
                                     final String targetPackage) {
        final List<JavaFile> nestedClasses = new ArrayList<>();
        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC);

        addClassAnnotations(classBuilder);

        if (schema != null && schema.getProperties() != null) {
            for (final Map.Entry<String, JSONSchemaProps> entry : schema.getProperties().entrySet()) {
                final String fieldName = entry.getKey();
                final JSONSchemaProps fieldSchema = entry.getValue();
                final TypeMapper.TypeContext context = new TypeMapper.TypeContext(targetPackage, className);

                addField(classBuilder, fieldName, fieldSchema, context, nestedClasses, targetPackage);
            }
        }

        final TypeSpec typeSpec = classBuilder.build();
        final JavaFile mainClass = JavaFile.builder(targetPackage, typeSpec)
                .skipJavaLangImports(true)
                .indent("    ")
                .build();

        return new GenerationResult(mainClass, nestedClasses);
    }

    /**
     * Write generated classes to the output directory.
     *
     * @param result          the generation result
     * @param outputDirectory the output directory
     * @throws IOException if writing fails
     */
    public void writeToFile(final GenerationResult result, final File outputDirectory) throws IOException {
        result.mainClass().writeTo(outputDirectory);
        for (final JavaFile nestedClass : result.nestedClasses()) {
            nestedClass.writeTo(outputDirectory);
        }
    }

    private void addClassAnnotations(final TypeSpec.Builder classBuilder) {
        if (config.isUseLombok()) {
            classBuilder.addAnnotation(Data.class);
            classBuilder.addAnnotation(Builder.class);
            classBuilder.addAnnotation(Jacksonized.class);
        }
        classBuilder.addAnnotation(AnnotationSpec.builder(JsonInclude.class)
                .addMember("value", "$T.NON_NULL", JsonInclude.Include.class)
                .build());
    }

    private void addField(final TypeSpec.Builder classBuilder,
                          final String fieldName,
                          final JSONSchemaProps schema,
                          final TypeMapper.TypeContext context,
                          final List<JavaFile> nestedClasses,
                          final String targetPackage) {
        final TypeName fieldType = typeMapper.mapType(schema, fieldName, context);
        final String javaFieldName = TypeMapper.toFieldName(fieldName);

        final FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, javaFieldName, Modifier.PRIVATE);

        if (!fieldName.equals(javaFieldName)) {
            fieldBuilder.addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                    .addMember("value", "$S", fieldName)
                    .build());
        }

        if (config.isUseLombok() && isListOrMap(fieldType)) {
            fieldBuilder.addAnnotation(Singular.class);
        }

        if (schema.getDescription() != null && !schema.getDescription().isEmpty()) {
            fieldBuilder.addJavadoc("$L\n", schema.getDescription());
        }

        classBuilder.addField(fieldBuilder.build());

        if (typeMapper.requiresNestedClass(schema)) {
            generateNestedClass(schema, fieldName, context, nestedClasses, targetPackage);
        }
    }

    private void generateNestedClass(final JSONSchemaProps schema,
                                     final String fieldName,
                                     final TypeMapper.TypeContext context,
                                     final List<JavaFile> nestedClasses,
                                     final String targetPackage) {
        final String nestedClassName;
        final JSONSchemaProps nestedSchema;

        if ("array".equals(schema.getType()) && schema.getItems() != null) {
            nestedClassName = context.nestedClassName(toSingular(fieldName));
            nestedSchema = schema.getItems();
        } else {
            nestedClassName = context.nestedClassName(fieldName);
            nestedSchema = schema;
        }

        final GenerationResult nestedResult = generate(nestedSchema, nestedClassName, targetPackage);
        nestedClasses.add(nestedResult.mainClass());
        nestedClasses.addAll(nestedResult.nestedClasses());
    }

    private boolean isListOrMap(final TypeName typeName) {
        if (typeName instanceof com.squareup.javapoet.ParameterizedTypeName parameterized) {
            final ClassName rawType = parameterized.rawType;
            return rawType.equals(ClassName.get(List.class))
                    || rawType.equals(ClassName.get(Map.class));
        }
        return false;
    }

    private String toSingular(final String plural) {
        if (plural == null || plural.isEmpty()) {
            return plural;
        }
        if (plural.endsWith("ies")) {
            return plural.substring(0, plural.length() - 3) + "y";
        }
        if (plural.endsWith("es")) {
            return plural.substring(0, plural.length() - 2);
        }
        if (plural.endsWith("s") && plural.length() > 1) {
            return plural.substring(0, plural.length() - 1);
        }
        return plural;
    }
}
