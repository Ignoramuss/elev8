package io.elev8.resources.ingress;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IngressStatusTest {

    @Test
    void shouldBuildIngressStatusWithLoadBalancer() {
        final IngressLoadBalancerStatus loadBalancer = IngressLoadBalancerStatus.builder()
                .ingress(IngressLoadBalancerIngress.builder()
                        .ip("192.168.1.1")
                        .build())
                .build();

        final IngressStatus status = IngressStatus.builder()
                .loadBalancer(loadBalancer)
                .build();

        assertThat(status.getLoadBalancer()).isEqualTo(loadBalancer);
    }

    @Test
    void shouldBuildIngressStatusWithIPAddress() {
        final IngressStatus status = IngressStatus.builder()
                .loadBalancer(IngressLoadBalancerStatus.builder()
                        .ingress(IngressLoadBalancerIngress.builder()
                                .ip("192.168.1.1")
                                .build())
                        .build())
                .build();

        assertThat(status.getLoadBalancer().getIngress()).hasSize(1);
        assertThat(status.getLoadBalancer().getIngress().get(0).getIp()).isEqualTo("192.168.1.1");
    }

    @Test
    void shouldBuildIngressStatusWithHostname() {
        final IngressStatus status = IngressStatus.builder()
                .loadBalancer(IngressLoadBalancerStatus.builder()
                        .ingress(IngressLoadBalancerIngress.builder()
                                .hostname("example-lb-123456.us-west-2.elb.amazonaws.com")
                                .build())
                        .build())
                .build();

        assertThat(status.getLoadBalancer().getIngress()).hasSize(1);
        assertThat(status.getLoadBalancer().getIngress().get(0).getHostname())
                .isEqualTo("example-lb-123456.us-west-2.elb.amazonaws.com");
    }

    @Test
    void shouldBuildIngressStatusWithMultipleIngresses() {
        final IngressStatus status = IngressStatus.builder()
                .loadBalancer(IngressLoadBalancerStatus.builder()
                        .ingress(IngressLoadBalancerIngress.builder()
                                .ip("192.168.1.1")
                                .build())
                        .ingress(IngressLoadBalancerIngress.builder()
                                .ip("192.168.1.2")
                                .build())
                        .build())
                .build();

        assertThat(status.getLoadBalancer().getIngress()).hasSize(2);
    }

    @Test
    void shouldBuildIngressStatusWithPorts() {
        final IngressStatus status = IngressStatus.builder()
                .loadBalancer(IngressLoadBalancerStatus.builder()
                        .ingress(IngressLoadBalancerIngress.builder()
                                .hostname("example-lb.amazonaws.com")
                                .port(IngressPortStatus.builder()
                                        .port(80)
                                        .protocol("TCP")
                                        .build())
                                .port(IngressPortStatus.builder()
                                        .port(443)
                                        .protocol("TCP")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(status.getLoadBalancer().getIngress().get(0).getPorts()).hasSize(2);
        assertThat(status.getLoadBalancer().getIngress().get(0).getPorts().get(0).getPort()).isEqualTo(80);
        assertThat(status.getLoadBalancer().getIngress().get(0).getPorts().get(1).getPort()).isEqualTo(443);
    }
}
