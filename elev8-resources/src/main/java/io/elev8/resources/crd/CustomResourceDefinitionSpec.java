package io.elev8.resources.crd;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.crd.conversion.CustomResourceConversion;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * CustomResourceDefinitionSpec describes how a user wants their resource to appear.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomResourceDefinitionSpec {
    private String group;
    private CRDNames names;
    private String scope;

    @Singular
    private List<CRDVersion> versions;

    private CustomResourceConversion conversion;
    private Boolean preserveUnknownFields;
}
