package io.elev8.resources.networkpolicy;

import io.elev8.resources.LabelSelector;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkPolicyIngressRuleTest {

    @Test
    void shouldBuildIngressRuleWithFrom() {
        final NetworkPolicyIngressRule rule = NetworkPolicyIngressRule.builder()
                .from(NetworkPolicyPeer.builder()
                        .podSelector(LabelSelector.builder()
                                .matchLabel("role", "frontend")
                                .build())
                        .build())
                .build();

        assertThat(rule.getFrom()).hasSize(1);
        assertThat(rule.getFrom().get(0).getPodSelector()).isNotNull();
        assertThat(rule.getPorts()).isEmpty();
    }

    @Test
    void shouldBuildIngressRuleWithPorts() {
        final NetworkPolicyIngressRule rule = NetworkPolicyIngressRule.builder()
                .port(NetworkPolicyPort.builder()
                        .protocol("TCP")
                        .port(8080)
                        .build())
                .build();

        assertThat(rule.getPorts()).hasSize(1);
        assertThat(rule.getPorts().get(0).getProtocol()).isEqualTo("TCP");
        assertThat(rule.getPorts().get(0).getPort()).isEqualTo(8080);
        assertThat(rule.getFrom()).isEmpty();
    }

    @Test
    void shouldBuildIngressRuleWithMultiplePeers() {
        final NetworkPolicyIngressRule rule = NetworkPolicyIngressRule.builder()
                .from(NetworkPolicyPeer.builder()
                        .podSelector(LabelSelector.builder()
                                .matchLabel("role", "frontend")
                                .build())
                        .build())
                .from(NetworkPolicyPeer.builder()
                        .namespaceSelector(LabelSelector.builder()
                                .matchLabel("environment", "production")
                                .build())
                        .build())
                .from(NetworkPolicyPeer.builder()
                        .ipBlock(IPBlock.builder()
                                .cidr("172.17.0.0/16")
                                .build())
                        .build())
                .build();

        assertThat(rule.getFrom()).hasSize(3);
    }

    @Test
    void shouldBuildIngressRuleWithMultiplePorts() {
        final NetworkPolicyIngressRule rule = NetworkPolicyIngressRule.builder()
                .port(NetworkPolicyPort.builder()
                        .protocol("TCP")
                        .port(8080)
                        .build())
                .port(NetworkPolicyPort.builder()
                        .protocol("TCP")
                        .port(443)
                        .build())
                .build();

        assertThat(rule.getPorts()).hasSize(2);
    }

    @Test
    void shouldBuildCompleteIngressRule() {
        final NetworkPolicyIngressRule rule = NetworkPolicyIngressRule.builder()
                .from(NetworkPolicyPeer.builder()
                        .podSelector(LabelSelector.builder()
                                .matchLabel("app", "frontend")
                                .build())
                        .build())
                .port(NetworkPolicyPort.builder()
                        .protocol("TCP")
                        .port(8080)
                        .build())
                .build();

        assertThat(rule.getFrom()).hasSize(1);
        assertThat(rule.getPorts()).hasSize(1);
    }
}
