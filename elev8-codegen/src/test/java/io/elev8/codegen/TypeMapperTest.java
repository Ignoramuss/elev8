package io.elev8.codegen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.elev8.resources.crd.schema.JSONSchemaProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TypeMapperTest {

    private TypeMapper typeMapper;
    private TypeMapper.TypeContext context;

    @BeforeEach
    void setUp() {
        typeMapper = new TypeMapper();
        context = new TypeMapper.TypeContext("com.example", "TestClass");
    }

    @Test
    void mapStringType() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("string")
                .build();

        final TypeName result = typeMapper.mapType(schema, "testField", context);

        assertThat(result).isEqualTo(ClassName.get(String.class));
    }

    @Test
    void mapStringDateTimeType() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("string")
                .format("date-time")
                .build();

        final TypeName result = typeMapper.mapType(schema, "testField", context);

        assertThat(result).isEqualTo(ClassName.get(Instant.class));
    }

    @Test
    void mapIntegerType() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("integer")
                .build();

        final TypeName result = typeMapper.mapType(schema, "testField", context);

        assertThat(result).isEqualTo(ClassName.get(Integer.class));
    }

    @Test
    void mapIntegerInt64Type() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("integer")
                .format("int64")
                .build();

        final TypeName result = typeMapper.mapType(schema, "testField", context);

        assertThat(result).isEqualTo(ClassName.get(Long.class));
    }

    @Test
    void mapNumberType() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("number")
                .build();

        final TypeName result = typeMapper.mapType(schema, "testField", context);

        assertThat(result).isEqualTo(ClassName.get(Double.class));
    }

    @Test
    void mapNumberFloatType() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("number")
                .format("float")
                .build();

        final TypeName result = typeMapper.mapType(schema, "testField", context);

        assertThat(result).isEqualTo(ClassName.get(Float.class));
    }

    @Test
    void mapBooleanType() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("boolean")
                .build();

        final TypeName result = typeMapper.mapType(schema, "testField", context);

        assertThat(result).isEqualTo(ClassName.get(Boolean.class));
    }

    @Test
    void mapArrayOfStringsType() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("array")
                .items(JSONSchemaProps.builder().type("string").build())
                .build();

        final TypeName result = typeMapper.mapType(schema, "items", context);

        assertThat(result).isInstanceOf(ParameterizedTypeName.class);
        final ParameterizedTypeName parameterized = (ParameterizedTypeName) result;
        assertThat(parameterized.rawType).isEqualTo(ClassName.get(List.class));
        assertThat(parameterized.typeArguments).containsExactly(ClassName.get(String.class));
    }

    @Test
    void mapArrayOfObjectsType() {
        final JSONSchemaProps itemSchema = JSONSchemaProps.builder()
                .type("object")
                .property("name", JSONSchemaProps.builder().type("string").build())
                .build();
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("array")
                .items(itemSchema)
                .build();

        final TypeName result = typeMapper.mapType(schema, "items", context);

        assertThat(result).isInstanceOf(ParameterizedTypeName.class);
        final ParameterizedTypeName parameterized = (ParameterizedTypeName) result;
        assertThat(parameterized.rawType).isEqualTo(ClassName.get(List.class));
        assertThat(parameterized.typeArguments.get(0).toString()).isEqualTo("com.example.TestClassItem");
    }

    @Test
    void mapObjectWithPropertiesType() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .property("name", JSONSchemaProps.builder().type("string").build())
                .build();

        final TypeName result = typeMapper.mapType(schema, "config", context);

        assertThat(result).isEqualTo(ClassName.get("com.example", "TestClassConfig"));
    }

    @Test
    void mapObjectWithAdditionalPropertiesType() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .additionalProperties(JSONSchemaProps.builder().type("string").build())
                .build();

        final TypeName result = typeMapper.mapType(schema, "labels", context);

        assertThat(result).isInstanceOf(ParameterizedTypeName.class);
        final ParameterizedTypeName parameterized = (ParameterizedTypeName) result;
        assertThat(parameterized.rawType).isEqualTo(ClassName.get(Map.class));
        assertThat(parameterized.typeArguments).containsExactly(
                ClassName.get(String.class),
                ClassName.get(String.class));
    }

    @Test
    void mapIntOrStringType() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .xKubernetesIntOrString(true)
                .build();

        final TypeName result = typeMapper.mapType(schema, "value", context);

        assertThat(result).isEqualTo(TypeName.OBJECT);
    }

    @Test
    void mapNullSchema() {
        final TypeName result = typeMapper.mapType(null, "field", context);

        assertThat(result).isEqualTo(TypeName.OBJECT);
    }

    @Test
    void requiresNestedClassForObjectWithProperties() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .property("name", JSONSchemaProps.builder().type("string").build())
                .build();

        assertThat(typeMapper.requiresNestedClass(schema)).isTrue();
    }

    @Test
    void doesNotRequireNestedClassForObjectWithAdditionalProperties() {
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("object")
                .additionalProperties(JSONSchemaProps.builder().type("string").build())
                .build();

        assertThat(typeMapper.requiresNestedClass(schema)).isFalse();
    }

    @Test
    void requiresNestedClassForArrayWithNestedItems() {
        final JSONSchemaProps itemSchema = JSONSchemaProps.builder()
                .type("object")
                .property("name", JSONSchemaProps.builder().type("string").build())
                .build();
        final JSONSchemaProps schema = JSONSchemaProps.builder()
                .type("array")
                .items(itemSchema)
                .build();

        assertThat(typeMapper.requiresNestedClass(schema)).isTrue();
        assertThat(typeMapper.isArrayWithNestedItems(schema)).isTrue();
    }

    @Test
    void toClassName() {
        assertThat(TypeMapper.toClassName("myField")).isEqualTo("MyField");
        assertThat(TypeMapper.toClassName("my-field")).isEqualTo("MyField");
        assertThat(TypeMapper.toClassName("my_field")).isEqualTo("MyField");
        assertThat(TypeMapper.toClassName("my.field")).isEqualTo("MyField");
    }

    @Test
    void toFieldName() {
        assertThat(TypeMapper.toFieldName("MyField")).isEqualTo("myField");
        assertThat(TypeMapper.toFieldName("my-field")).isEqualTo("myField");
        assertThat(TypeMapper.toFieldName("my_field")).isEqualTo("myField");
        assertThat(TypeMapper.toFieldName("class")).isEqualTo("class_");
    }
}
