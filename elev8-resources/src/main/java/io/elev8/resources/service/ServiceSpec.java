package io.elev8.resources.service;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceSpec {

    private Map<String, String> selector;
    private List<ServicePort> ports;
    private String type;
    private String clusterIP;
    private List<String> externalIPs;
    private String sessionAffinity;
    private String loadBalancerIP;

    public ServiceSpec() {
    }

    private ServiceSpec(Builder builder) {
        this.selector = builder.selector;
        this.ports = builder.ports;
        this.type = builder.type;
        this.clusterIP = builder.clusterIP;
        this.externalIPs = builder.externalIPs;
        this.sessionAffinity = builder.sessionAffinity;
        this.loadBalancerIP = builder.loadBalancerIP;
    }

    public Map<String, String> getSelector() {
        return selector;
    }

    public void setSelector(Map<String, String> selector) {
        this.selector = selector;
    }

    public List<ServicePort> getPorts() {
        return ports;
    }

    public void setPorts(List<ServicePort> ports) {
        this.ports = ports;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClusterIP() {
        return clusterIP;
    }

    public void setClusterIP(String clusterIP) {
        this.clusterIP = clusterIP;
    }

    public List<String> getExternalIPs() {
        return externalIPs;
    }

    public void setExternalIPs(List<String> externalIPs) {
        this.externalIPs = externalIPs;
    }

    public String getSessionAffinity() {
        return sessionAffinity;
    }

    public void setSessionAffinity(String sessionAffinity) {
        this.sessionAffinity = sessionAffinity;
    }

    public String getLoadBalancerIP() {
        return loadBalancerIP;
    }

    public void setLoadBalancerIP(String loadBalancerIP) {
        this.loadBalancerIP = loadBalancerIP;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<String, String> selector;
        private List<ServicePort> ports;
        private String type = "ClusterIP";
        private String clusterIP;
        private List<String> externalIPs;
        private String sessionAffinity = "None";
        private String loadBalancerIP;

        public Builder selector(Map<String, String> selector) {
            this.selector = selector;
            return this;
        }

        public Builder addSelector(String key, String value) {
            if (this.selector == null) {
                this.selector = new HashMap<>();
            }
            this.selector.put(key, value);
            return this;
        }

        public Builder ports(List<ServicePort> ports) {
            this.ports = ports;
            return this;
        }

        public Builder addPort(ServicePort port) {
            if (this.ports == null) {
                this.ports = new ArrayList<>();
            }
            this.ports.add(port);
            return this;
        }

        public Builder addPort(int port, int targetPort) {
            return addPort(ServicePort.builder()
                    .port(port)
                    .targetPort(targetPort)
                    .build());
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder clusterIP(String clusterIP) {
            this.clusterIP = clusterIP;
            return this;
        }

        public Builder externalIPs(List<String> externalIPs) {
            this.externalIPs = externalIPs;
            return this;
        }

        public Builder sessionAffinity(String sessionAffinity) {
            this.sessionAffinity = sessionAffinity;
            return this;
        }

        public Builder loadBalancerIP(String loadBalancerIP) {
            this.loadBalancerIP = loadBalancerIP;
            return this;
        }

        public ServiceSpec build() {
            if (ports == null || ports.isEmpty()) {
                throw new IllegalArgumentException("At least one port is required");
            }
            return new ServiceSpec(this);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServicePort {
        private String name;
        private String protocol;
        private Integer port;
        private Integer targetPort;
        private Integer nodePort;

        public ServicePort() {
        }

        private ServicePort(Builder builder) {
            this.name = builder.name;
            this.protocol = builder.protocol;
            this.port = builder.port;
            this.targetPort = builder.targetPort;
            this.nodePort = builder.nodePort;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Integer getTargetPort() {
            return targetPort;
        }

        public void setTargetPort(Integer targetPort) {
            this.targetPort = targetPort;
        }

        public Integer getNodePort() {
            return nodePort;
        }

        public void setNodePort(Integer nodePort) {
            this.nodePort = nodePort;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String name;
            private String protocol = "TCP";
            private Integer port;
            private Integer targetPort;
            private Integer nodePort;

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder protocol(String protocol) {
                this.protocol = protocol;
                return this;
            }

            public Builder port(int port) {
                this.port = port;
                return this;
            }

            public Builder targetPort(int targetPort) {
                this.targetPort = targetPort;
                return this;
            }

            public Builder nodePort(int nodePort) {
                this.nodePort = nodePort;
                return this;
            }

            public ServicePort build() {
                if (port == null) {
                    throw new IllegalArgumentException("Service port is required");
                }
                return new ServicePort(this);
            }
        }
    }
}
