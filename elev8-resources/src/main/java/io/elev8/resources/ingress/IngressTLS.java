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
 * IngressTLS describes the transport layer security associated with an Ingress.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IngressTLS {
    /**
     * Hosts are a list of hosts included in the TLS certificate. The values in
     * this list must match the name/s used in the tlsSecret. Defaults to the
     * wildcard host setting for the loadbalancer controller fulfilling this
     * Ingress, if left unspecified.
     */
    @Singular("host")
    private List<String> hosts;

    /**
     * SecretName is the name of the secret used to terminate TLS traffic on
     * port 443. Field is left optional to allow TLS routing based on SNI
     * hostname alone. If the SNI host in a listener conflicts with the "Host"
     * header field used by an IngressRule, the SNI host is used for termination
     * and value of the Host header is used for routing.
     */
    private String secretName;
}
