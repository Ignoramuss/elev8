package io.elev8.resources.networkpolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * NetworkPolicyPort describes a port to allow traffic on.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NetworkPolicyPort {
    /**
     * The protocol (TCP, UDP, or SCTP) which traffic must match.
     * If not specified, this field defaults to TCP.
     */
    private String protocol;

    /**
     * The port on the given protocol.
     * This can be either a numerical port (Integer) or a named port on a pod (String).
     * If this field is not provided, this matches all port names and numbers.
     * If present, only traffic on the specified protocol AND port will be matched.
     */
    private Object port;

    /**
     * If set, indicates that the range of ports from port to endPort, inclusive,
     * should be allowed by the policy. This field cannot be defined if the port field is not defined
     * or if the port field is defined as a named (string) port.
     * The endPort must be equal or greater than port.
     */
    private Integer endPort;
}
