package io.elev8.resources.crd.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * CustomResourceValidation is a list of validation methods for CustomResources.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomResourceValidation {
    private JSONSchemaProps openAPIV3Schema;
}
