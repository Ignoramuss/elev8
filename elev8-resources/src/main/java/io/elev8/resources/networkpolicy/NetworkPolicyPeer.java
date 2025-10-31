package io.elev8.resources.networkpolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.LabelSelector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * NetworkPolicyPeer describes a peer to allow traffic to/from. Only certain combinations of
 * fields are allowed:
 * - podSelector: Selects pods in the same namespace
 * - namespaceSelector: Selects all pods in namespaces matching the selector
 * - podSelector + namespaceSelector: Selects pods in namespaces matching both selectors
 * - ipBlock: Selects particular IP CIDR ranges (cannot combine with pod/namespace selectors)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NetworkPolicyPeer {
    /**
     * Selects pods in the same namespace as the NetworkPolicy which should be allowed
     * as ingress sources or egress destinations.
     * If this field is present and contains at least one item, only traffic from/to pods
     * matching this selector is allowed.
     * A null podSelector matches no pods.
     * An empty podSelector matches all pods in this namespace.
     */
    private LabelSelector podSelector;

    /**
     * Selects namespaces using cluster-scoped labels.
     * If present but empty, it selects all namespaces.
     * If specified along with podSelector, selects pods in namespaces matching the selector.
     */
    private LabelSelector namespaceSelector;

    /**
     * Defines a range of IP addresses which should be allowed as ingress sources or egress destinations.
     * This should be a valid CIDR block.
     * Except entries (optional) in IPBlock should be valid CIDRs, strictly within CIDR range.
     * Cannot be specified along with podSelector or namespaceSelector.
     */
    private IPBlock ipBlock;
}
