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
 * NetworkPolicyIngressRule describes a particular set of traffic that is allowed to the pods
 * matched by a NetworkPolicySpec's podSelector. The traffic must match both ports and from.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NetworkPolicyIngressRule {
    /**
     * List of sources which should be able to access the pods selected for this rule.
     * Items in this list are combined using a logical OR operation.
     * If this field is empty or missing, this rule matches all sources (traffic not restricted by source).
     * If this field is present and contains at least one item, traffic is allowed only if it matches
     * at least one item in the from list.
     */
    @Singular("from")
    private List<NetworkPolicyPeer> from;

    /**
     * List of destination ports that are allowed on the pods selected for this rule.
     * Each item in this list is combined using a logical OR.
     * If this field is empty or missing, this rule matches all ports (traffic not restricted by port).
     * If this field is present and contains at least one item, then this rule allows traffic
     * only if it matches at least one port in the list.
     */
    @Singular("port")
    private List<NetworkPolicyPort> ports;
}
