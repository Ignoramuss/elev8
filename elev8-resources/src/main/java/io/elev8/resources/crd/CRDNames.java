package io.elev8.resources.crd;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * CustomResourceDefinitionNames indicates the names to serve this CustomResourceDefinition.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CRDNames {
    private String plural;
    private String singular;

    @Singular
    private List<String> shortNames;

    private String kind;
    private String listKind;

    @Singular("category")
    private List<String> categories;
}
