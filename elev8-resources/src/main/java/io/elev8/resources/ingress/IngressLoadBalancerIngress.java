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
 * IngressLoadBalancerIngress represents the status of a load-balancer ingress point:
 * traffic intended for the service should be sent to an ingress point.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IngressLoadBalancerIngress {
    /**
     * IP is set for load-balancer ingress points that are IP based
     * (typically GCE or OpenStack load-balancers).
     */
    private String ip;

    /**
     * Hostname is set for load-balancer ingress points that are DNS based
     * (typically AWS load-balancers).
     */
    private String hostname;

    /**
     * Ports is a list of records of service ports
     * If used, every port defined in the service should have an entry in it.
     */
    @Singular("port")
    private List<IngressPortStatus> ports;
}
