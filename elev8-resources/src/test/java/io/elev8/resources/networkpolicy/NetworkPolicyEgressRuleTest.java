package io.elev8.resources.networkpolicy;

import io.elev8.resources.LabelSelector;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkPolicyEgressRuleTest {

    @Test
    void shouldBuildEgressRuleWithTo() {
        final NetworkPolicyEgressRule rule = NetworkPolicyEgressRule.builder()
                .to(NetworkPolicyPeer.builder()
                        .podSelector(LabelSelector.builder()
                                .matchLabel("role", "backend")
                                .build())
                        .build())
                .build();

        assertThat(rule.getTo()).hasSize(1);
        assertThat(rule.getTo().get(0).getPodSelector()).isNotNull();
        assertThat(rule.getPorts()).isEmpty();
    }

    @Test
    void shouldBuildEgressRuleWithPorts() {
        final NetworkPolicyEgressRule rule = NetworkPolicyEgressRule.builder()
                .port(NetworkPolicyPort.builder()
                        .protocol("TCP")
                        .port(5432)
                        .build())
                .build();

        assertThat(rule.getPorts()).hasSize(1);
        assertThat(rule.getPorts().get(0).getProtocol()).isEqualTo("TCP");
        assertThat(rule.getPorts().get(0).getPort()).isEqualTo(5432);
        assertThat(rule.getTo()).isEmpty();
    }

    @Test
    void shouldBuildEgressRuleWithMultiplePeers() {
        final NetworkPolicyEgressRule rule = NetworkPolicyEgressRule.builder()
                .to(NetworkPolicyPeer.builder()
                        .podSelector(LabelSelector.builder()
                                .matchLabel("role", "database")
                                .build())
                        .build())
                .to(NetworkPolicyPeer.builder()
                        .ipBlock(IPBlock.builder()
                                .cidr("0.0.0.0/0")
                                .except("169.254.169.254/32")
                                .build())
                        .build())
                .build();

        assertThat(rule.getTo()).hasSize(2);
    }

    @Test
    void shouldBuildEgressRuleWithMultiplePorts() {
        final NetworkPolicyEgressRule rule = NetworkPolicyEgressRule.builder()
                .port(NetworkPolicyPort.builder()
                        .protocol("TCP")
                        .port(443)
                        .build())
                .port(NetworkPolicyPort.builder()
                        .protocol("UDP")
                        .port(53)
                        .build())
                .build();

        assertThat(rule.getPorts()).hasSize(2);
    }

    @Test
    void shouldBuildCompleteEgressRule() {
        final NetworkPolicyEgressRule rule = NetworkPolicyEgressRule.builder()
                .to(NetworkPolicyPeer.builder()
                        .podSelector(LabelSelector.builder()
                                .matchLabel("app", "database")
                                .build())
                        .build())
                .port(NetworkPolicyPort.builder()
                        .protocol("TCP")
                        .port(5432)
                        .build())
                .build();

        assertThat(rule.getTo()).hasSize(1);
        assertThat(rule.getPorts()).hasSize(1);
    }
}
