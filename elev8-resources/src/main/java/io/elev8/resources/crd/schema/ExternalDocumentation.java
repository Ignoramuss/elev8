package io.elev8.resources.crd.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * ExternalDocumentation allows referencing an external resource for extended documentation.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalDocumentation {
    private String description;
    private String url;
}
