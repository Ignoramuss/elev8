package io.elev8.resources.ingress;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * ServiceBackendPort is the service port being referenced.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceBackendPort {
    /**
     * Name is the name of the port on the Service.
     * This is a mutually exclusive setting with "Number".
     */
    private String name;

    /**
     * Number is the numerical port number (e.g. 80) on the Service.
     * This is a mutually exclusive setting with "Name".
     */
    private Integer number;
}
