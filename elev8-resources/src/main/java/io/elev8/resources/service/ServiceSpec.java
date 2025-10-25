package io.elev8.resources.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceSpec {

    @Singular("selector") Map<String, String> selector;
    @Singular List<ServicePort> ports;
    @Builder.Default String type = "ClusterIP";
    String clusterIP;
    @Singular("externalIP") List<String> externalIPs;
    @Builder.Default String sessionAffinity = "None";
    String loadBalancerIP;

    public static class ServiceSpecBuilder {
        public ServiceSpecBuilder addPort(final int port, final int targetPort) {
            return port(ServicePort.builder()
                    .port(port)
                    .targetPort(targetPort)
                    .build());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServicePort {
        String name;
        @Builder.Default String protocol = "TCP";
        @NonNull Integer port;
        Integer targetPort;
        Integer nodePort;
    }
}
