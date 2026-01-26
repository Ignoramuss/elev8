package io.elev8.resources.crd.conversion;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * ServiceReference holds a reference to a Kubernetes Service.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceReference {
    private String namespace;
    private String name;
    private String path;
    private Integer port;
}
