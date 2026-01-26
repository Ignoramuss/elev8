package io.elev8.resources.crd.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

/**
 * JSONSchemaProps is a JSON-Schema following Specification Draft 4 with Kubernetes extensions.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JSONSchemaProps {

    @JsonProperty("$ref")
    private String ref;

    @JsonProperty("$schema")
    private String schema;

    private String id;
    private String description;
    private String type;
    private String format;
    private String title;

    @JsonProperty("default")
    private Object defaultValue;

    private Number maximum;
    private Boolean exclusiveMaximum;
    private Number minimum;
    private Boolean exclusiveMinimum;
    private Long maxLength;
    private Long minLength;
    private String pattern;
    private Long maxItems;
    private Long minItems;
    private Boolean uniqueItems;
    private Number multipleOf;

    @JsonProperty("enum")
    @Singular("enumValue")
    private List<Object> enumValues;

    private Long maxProperties;
    private Long minProperties;

    @Singular("requiredField")
    private List<String> required;

    private JSONSchemaProps items;
    private List<JSONSchemaProps> allOf;
    private List<JSONSchemaProps> oneOf;
    private List<JSONSchemaProps> anyOf;
    private JSONSchemaProps not;

    @Singular("property")
    private Map<String, JSONSchemaProps> properties;

    private JSONSchemaProps additionalProperties;
    private Map<String, JSONSchemaProps> patternProperties;
    private Map<String, List<String>> dependencies;

    private JSONSchemaProps additionalItems;
    private Map<String, JSONSchemaProps> definitions;

    private ExternalDocumentation externalDocs;
    private Object example;
    private Boolean nullable;

    @JsonProperty("x-kubernetes-preserve-unknown-fields")
    private Boolean xKubernetesPreserveUnknownFields;

    @JsonProperty("x-kubernetes-embedded-resource")
    private Boolean xKubernetesEmbeddedResource;

    @JsonProperty("x-kubernetes-int-or-string")
    private Boolean xKubernetesIntOrString;

    @JsonProperty("x-kubernetes-list-map-keys")
    @Singular("xKubernetesListMapKey")
    private List<String> xKubernetesListMapKeys;

    @JsonProperty("x-kubernetes-list-type")
    private String xKubernetesListType;

    @JsonProperty("x-kubernetes-map-type")
    private String xKubernetesMapType;

    @JsonProperty("x-kubernetes-validations")
    @Singular("xKubernetesValidation")
    private List<ValidationRule> xKubernetesValidations;
}
