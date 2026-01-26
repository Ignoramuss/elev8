package io.elev8.resources.crd;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * CustomResourceDefinitionStatus indicates the state of the CustomResourceDefinition.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomResourceDefinitionStatus {

    @Singular
    private List<CRDCondition> conditions;

    private CRDNames acceptedNames;

    @Singular
    private List<String> storedVersions;
}
