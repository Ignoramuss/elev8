package io.elev8.resources.networkpolicy;

import io.elev8.resources.LabelSelector;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkPolicySpecTest {

    @Test
    void shouldBuildSpecWithPodSelector() {
        final NetworkPolicySpec spec = NetworkPolicySpec.builder()
                .podSelector(LabelSelector.builder()
                        .matchLabel("role", "db")
                        .build())
                .build();

        assertThat(spec.getPodSelector()).isNotNull();
        assertThat(spec.getPodSelector().getMatchLabels()).containsEntry("role", "db");
    }

    @Test
    void shouldBuildSpecWithPolicyTypes() {
        final NetworkPolicySpec spec = NetworkPolicySpec.builder()
                .podSelector(LabelSelector.builder().build())
                .policyType("Ingress")
                .policyType("Egress")
                .build();

        assertThat(spec.getPolicyTypes()).hasSize(2);
        assertThat(spec.getPolicyTypes()).contains("Ingress", "Egress");
    }

    @Test
    void shouldBuildSpecWithIngressRules() {
        final NetworkPolicySpec spec = NetworkPolicySpec.builder()
                .podSelector(LabelSelector.builder().build())
                .ingressRule(NetworkPolicyIngressRule.builder()
                        .from(NetworkPolicyPeer.builder()
                                .podSelector(LabelSelector.builder()
                                        .matchLabel("role", "frontend")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(spec.getIngress()).hasSize(1);
        assertThat(spec.getIngress().get(0).getFrom()).hasSize(1);
    }

    @Test
    void shouldBuildSpecWithEgressRules() {
        final NetworkPolicySpec spec = NetworkPolicySpec.builder()
                .podSelector(LabelSelector.builder().build())
                .egressRule(NetworkPolicyEgressRule.builder()
                        .to(NetworkPolicyPeer.builder()
                                .podSelector(LabelSelector.builder()
                                        .matchLabel("role", "backend")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(spec.getEgress()).hasSize(1);
        assertThat(spec.getEgress().get(0).getTo()).hasSize(1);
    }

    @Test
    void shouldBuildSpecWithBothIngressAndEgress() {
        final NetworkPolicySpec spec = NetworkPolicySpec.builder()
                .podSelector(LabelSelector.builder()
                        .matchLabel("app", "payment")
                        .build())
                .policyType("Ingress")
                .policyType("Egress")
                .ingressRule(NetworkPolicyIngressRule.builder()
                        .from(NetworkPolicyPeer.builder()
                                .podSelector(LabelSelector.builder()
                                        .matchLabel("role", "frontend")
                                        .build())
                                .build())
                        .port(NetworkPolicyPort.builder()
                                .protocol("TCP")
                                .port(8080)
                                .build())
                        .build())
                .egressRule(NetworkPolicyEgressRule.builder()
                        .to(NetworkPolicyPeer.builder()
                                .podSelector(LabelSelector.builder()
                                        .matchLabel("role", "database")
                                        .build())
                                .build())
                        .port(NetworkPolicyPort.builder()
                                .protocol("TCP")
                                .port(5432)
                                .build())
                        .build())
                .build();

        assertThat(spec.getPolicyTypes()).hasSize(2);
        assertThat(spec.getIngress()).hasSize(1);
        assertThat(spec.getEgress()).hasSize(1);
    }

    @Test
    void shouldBuildSpecWithEmptyPodSelector() {
        final NetworkPolicySpec spec = NetworkPolicySpec.builder()
                .podSelector(LabelSelector.builder().build())
                .build();

        assertThat(spec.getPodSelector()).isNotNull();
        assertThat(spec.getPodSelector().getMatchLabels()).isEmpty();
    }
}
