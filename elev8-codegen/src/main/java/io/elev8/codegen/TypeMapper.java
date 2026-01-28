package io.elev8.codegen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.elev8.resources.crd.schema.JSONSchemaProps;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Maps JSON Schema types to Java types for code generation.
 */
public class TypeMapper {

    private static final Set<String> JAVA_KEYWORDS = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "try", "void", "volatile", "while", "true", "false", "null"
    );

    /**
     * Context for type mapping, containing the target package for nested types.
     */
    public record TypeContext(String targetPackage, String parentClassName) {
        public String nestedClassName(final String fieldName) {
            return parentClassName + toClassName(fieldName);
        }
    }

    /**
     * Map a JSON Schema property to a Java type.
     *
     * @param schema    the JSON Schema property
     * @param fieldName the field name (used for generating nested class names)
     * @param context   the type context
     * @return the Java type name
     */
    public TypeName mapType(final JSONSchemaProps schema, final String fieldName, final TypeContext context) {
        if (schema == null) {
            return TypeName.OBJECT;
        }

        if (Boolean.TRUE.equals(schema.getXKubernetesIntOrString())) {
            return TypeName.OBJECT;
        }

        final String type = schema.getType();
        final String format = schema.getFormat();

        if (type == null) {
            if (schema.getProperties() != null && !schema.getProperties().isEmpty()) {
                return ClassName.get(context.targetPackage(), context.nestedClassName(fieldName));
            }
            return TypeName.OBJECT;
        }

        return switch (type) {
            case "string" -> mapStringType(format);
            case "integer" -> mapIntegerType(format);
            case "number" -> mapNumberType(format);
            case "boolean" -> ClassName.get(Boolean.class);
            case "array" -> mapArrayType(schema, fieldName, context);
            case "object" -> mapObjectType(schema, fieldName, context);
            default -> TypeName.OBJECT;
        };
    }

    /**
     * Check if a schema represents a nested object that needs its own class.
     *
     * @param schema the JSON Schema property
     * @return true if this schema requires a nested class
     */
    public boolean requiresNestedClass(final JSONSchemaProps schema) {
        if (schema == null) {
            return false;
        }
        if (Boolean.TRUE.equals(schema.getXKubernetesIntOrString())) {
            return false;
        }
        if ("object".equals(schema.getType()) && schema.getProperties() != null
                && !schema.getProperties().isEmpty()) {
            return true;
        }
        if ("array".equals(schema.getType()) && schema.getItems() != null) {
            return requiresNestedClass(schema.getItems());
        }
        return false;
    }

    /**
     * Check if a schema represents an array with nested items that need their own class.
     *
     * @param schema the JSON Schema property
     * @return true if this is an array with nested class items
     */
    public boolean isArrayWithNestedItems(final JSONSchemaProps schema) {
        if (schema == null || !"array".equals(schema.getType())) {
            return false;
        }
        return schema.getItems() != null && requiresNestedClass(schema.getItems());
    }

    /**
     * Convert a field name to a valid Java class name.
     *
     * @param fieldName the field name
     * @return a valid class name
     */
    public static String toClassName(final String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return "Unknown";
        }
        final StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (final char c : fieldName.toCharArray()) {
            if (c == '-' || c == '_' || c == '.') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Convert a field name to a valid Java field name.
     *
     * @param fieldName the original field name
     * @return a valid Java field name
     */
    public static String toFieldName(final String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return "unknown";
        }
        final StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (int i = 0; i < fieldName.length(); i++) {
            final char c = fieldName.charAt(i);
            if (c == '-' || c == '_' || c == '.') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else if (i == 0) {
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        String name = result.toString();
        if (JAVA_KEYWORDS.contains(name)) {
            name = name + "_";
        }
        return name;
    }

    private TypeName mapStringType(final String format) {
        if (format == null) {
            return ClassName.get(String.class);
        }
        return switch (format) {
            case "date-time" -> ClassName.get(Instant.class);
            case "byte" -> ClassName.get(String.class);
            case "binary" -> ClassName.get(String.class);
            default -> ClassName.get(String.class);
        };
    }

    private TypeName mapIntegerType(final String format) {
        if (format == null) {
            return ClassName.get(Integer.class);
        }
        return switch (format) {
            case "int32" -> ClassName.get(Integer.class);
            case "int64" -> ClassName.get(Long.class);
            default -> ClassName.get(Integer.class);
        };
    }

    private TypeName mapNumberType(final String format) {
        if (format == null) {
            return ClassName.get(Double.class);
        }
        return switch (format) {
            case "float" -> ClassName.get(Float.class);
            case "double" -> ClassName.get(Double.class);
            default -> ClassName.get(Double.class);
        };
    }

    private TypeName mapArrayType(final JSONSchemaProps schema, final String fieldName,
                                  final TypeContext context) {
        final JSONSchemaProps items = schema.getItems();
        final TypeName itemType;

        if (items == null) {
            itemType = TypeName.OBJECT;
        } else if (requiresNestedClass(items)) {
            final String itemClassName = context.nestedClassName(toSingular(fieldName));
            itemType = ClassName.get(context.targetPackage(), itemClassName);
        } else {
            itemType = mapType(items, fieldName, context);
        }

        return ParameterizedTypeName.get(ClassName.get(List.class), itemType);
    }

    private TypeName mapObjectType(final JSONSchemaProps schema, final String fieldName,
                                   final TypeContext context) {
        if (schema.getAdditionalProperties() != null) {
            final TypeName valueType = mapType(schema.getAdditionalProperties(), fieldName, context);
            return ParameterizedTypeName.get(ClassName.get(Map.class),
                    ClassName.get(String.class), valueType);
        }

        if (schema.getProperties() != null && !schema.getProperties().isEmpty()) {
            return ClassName.get(context.targetPackage(), context.nestedClassName(fieldName));
        }

        return ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class), TypeName.OBJECT);
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
