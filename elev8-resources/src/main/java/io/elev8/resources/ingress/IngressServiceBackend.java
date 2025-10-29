package io.elev8.resources.ingress;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

/**
 * IngressServiceBackend references a Kubernetes Service as a Backend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IngressServiceBackend {
    /**
     * Name is the referenced service. The service must exist in
     * the same namespace as the Ingress object.
     */
    @NonNull
    private String name;

    /**
     * Port of the referenced service. A port name or port number
     * is required for a IngressServiceBackend.
     */
    @NonNull
    private ServiceBackendPort port;
}
