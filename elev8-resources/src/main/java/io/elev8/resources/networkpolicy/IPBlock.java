package io.elev8.resources.networkpolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * IPBlock describes a particular CIDR (Ex. "192.168.1.1/24", "2001:db9::/64") that is allowed
 * to/from the pods matched by a NetworkPolicySpec's podSelector. The except entry describes CIDRs
 * that should not be included within this rule.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IPBlock {
    /**
     * CIDR is a string representing the IP Block.
     * Valid examples are "192.168.1.1/24" or "2001:db9::/64".
     */
    @NonNull
    private String cidr;

    /**
     * Except is a slice of CIDRs that should not be included within an IP Block.
     * Valid examples are "192.168.1.1/24" or "2001:db9::/64".
     * Except values will be rejected if they are outside the CIDR range.
     */
    @Singular("except")
    private List<String> except;
}
