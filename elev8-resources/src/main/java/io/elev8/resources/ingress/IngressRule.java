package io.elev8.resources.ingress;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * IngressRule represents the rules mapping the paths under a specified host to
 * the related backend services. Incoming requests are first evaluated for a host
 * match, then routed to the backend associated with the matching IngressRuleValue.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IngressRule {
    /**
     * Host is the fully qualified domain name of a network host. If a host is
     * specified, the IngressRuleValue applies to that host. If the host value is
     * not specified, this rule applies to all inbound HTTP traffic.
     */
    private String host;

    /**
     * HTTP represents a rule to apply against incoming HTTP requests. If no
     * matching rule is found, traffic is routed based on the requested path.
     */
    private HTTPIngressRuleValue http;
}
