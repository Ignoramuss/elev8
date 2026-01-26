package io.elev8.resources.crd.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * CustomResourceSubresourceScale defines how to serve the scale subresource for CustomResources.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomResourceSubresourceScale {
    private String specReplicasPath;
    private String statusReplicasPath;
    private String labelSelectorPath;
}
