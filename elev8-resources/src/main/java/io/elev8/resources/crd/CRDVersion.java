package io.elev8.resources.crd;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.crd.schema.CustomResourceColumnDefinition;
import io.elev8.resources.crd.schema.CustomResourceSubresources;
import io.elev8.resources.crd.schema.CustomResourceValidation;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * CustomResourceDefinitionVersion describes a version for a CRD.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CRDVersion {
    private String name;
    private Boolean served;
    private Boolean storage;
    private Boolean deprecated;
    private String deprecationWarning;
    private CustomResourceValidation schema;
    private CustomResourceSubresources subresources;

    @Singular("additionalPrinterColumn")
    private List<CustomResourceColumnDefinition> additionalPrinterColumns;
}
