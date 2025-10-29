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
 * IngressLoadBalancerStatus represents the status of a load-balancer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IngressLoadBalancerStatus {
    /**
     * Ingress is a list containing ingress points for the load-balancer.
     * Traffic intended for the service should be sent to these ingress points.
     */
    @Singular("ingress")
    private List<IngressLoadBalancerIngress> ingress;
}
