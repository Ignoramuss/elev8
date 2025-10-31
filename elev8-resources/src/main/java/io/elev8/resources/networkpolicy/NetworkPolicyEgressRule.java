package io.elev8.resources.networkpolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * NetworkPolicyEgressRule describes a particular set of traffic that is allowed out of pods
 * matched by a NetworkPolicySpec's podSelector. The traffic must match both ports and to.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NetworkPolicyEgressRule {
    /**
     * List of destinations for outgoing traffic of pods selected for this rule.
     * Items in this list are combined using a logical OR operation.
     * If this field is empty or missing, this rule matches all destinations (traffic not restricted by destination).
     * If this field is present and contains at least one item, traffic is allowed only if it matches
     * at least one item in the to list.
     */
    @Singular("to")
    private List<NetworkPolicyPeer> to;

    /**
     * List of destination ports for outgoing traffic.
     * Each item in this list is combined using a logical OR.
     * If this field is empty or missing, this rule matches all ports (traffic not restricted by port).
     * If this field is present and contains at least one item, then this rule allows traffic
     * only if it matches at least one port in the list.
     */
    @Singular("port")
    private List<NetworkPolicyPort> ports;
}
