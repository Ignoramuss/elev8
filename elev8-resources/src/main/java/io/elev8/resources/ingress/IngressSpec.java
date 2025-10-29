package io.elev8.resources.ingress;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * IngressSpec describes the Ingress the user wishes to exist.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IngressSpec {
    /**
     * IngressClassName is the name of an IngressClass cluster resource. Ingress
     * controller implementations use this field to know whether they should be
     * serving this Ingress resource, by a transitive connection
     * (controller -> IngressClass -> Ingress resource).
     */
    private String ingressClassName;

    /**
     * DefaultBackend is the backend that should handle requests that don't
     * match any rule. If Rules are not specified, DefaultBackend must be specified.
     * If DefaultBackend is not set, the handling of requests that do not match any
     * of the rules will be up to the Ingress controller.
     */
    private IngressBackend defaultBackend;

    /**
     * A list of host rules used to configure the Ingress. If unspecified, or
     * no rule matches, all traffic is sent to the default backend.
     */
    @Singular("rule")
    private List<IngressRule> rules;

    /**
     * TLS configuration. Currently the Ingress only supports a single TLS
     * port, 443. If multiple members of this list specify different hosts, they
     * will be multiplexed on the same port according to the hostname specified
     * through the SNI TLS extension, if the ingress controller fulfilling the
     * ingress supports SNI.
     */
    @Singular("tl")
    private List<IngressTLS> tls;
}
