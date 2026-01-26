package io.elev8.resources.crd.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * CustomResourceColumnDefinition specifies a column for server side printing.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomResourceColumnDefinition {
    private String name;
    private String type;
    private String format;
    private String description;
    private Integer priority;
    private String jsonPath;
}
