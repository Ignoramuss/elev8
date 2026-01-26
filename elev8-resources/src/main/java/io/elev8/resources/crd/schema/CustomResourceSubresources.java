package io.elev8.resources.crd.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * CustomResourceSubresources defines the status and scale subresources for CustomResources.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomResourceSubresources {
    private Map<String, Object> status;
    private CustomResourceSubresourceScale scale;
}
