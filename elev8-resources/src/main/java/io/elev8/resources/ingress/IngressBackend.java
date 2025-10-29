package io.elev8.resources.ingress;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.ObjectReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * IngressBackend describes all endpoints for a given service and port.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IngressBackend {
    /**
     * Service references a service as a backend. This is a mutually exclusive
     * setting with "Resource".
     */
    private IngressServiceBackend service;

    /**
     * Resource is an ObjectRef to another Kubernetes resource in the namespace
     * of the Ingress object. If resource is specified, a service.Name and
     * service.Port must not be specified. This is a mutually exclusive setting
     * with "Service".
     */
    private ObjectReference resource;
}
