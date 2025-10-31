package io.elev8.resources.networkpolicy;

import io.elev8.resources.LabelSelector;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NetworkPolicyTest {

    @Test
    void shouldBuildNetworkPolicyWithRequiredFields() {
        final NetworkPolicy networkPolicy = NetworkPolicy.builder()
                .name("test-policy")
                .namespace("default")
                .spec(NetworkPolicySpec.builder()
                        .podSelector(LabelSelector.builder().build())
                        .build())
                .build();

        assertThat(networkPolicy.getApiVersion()).isEqualTo("networking.k8s.io/v1");
        assertThat(networkPolicy.getKind()).isEqualTo("NetworkPolicy");
        assertThat(networkPolicy.getName()).isEqualTo("test-policy");
        assertThat(networkPolicy.getNamespace()).isEqualTo("default");
    }

    @Test
    void shouldBuildNetworkPolicyWithLabels() {
        final NetworkPolicy networkPolicy = NetworkPolicy.builder()
                .name("test-policy")
                .namespace("default")
                .label("app", "backend")
                .label("env", "prod")
                .spec(NetworkPolicySpec.builder()
                        .podSelector(LabelSelector.builder().build())
                        .build())
                .build();

        assertThat(networkPolicy.getMetadata().getLabels()).containsEntry("app", "backend");
        assertThat(networkPolicy.getMetadata().getLabels()).containsEntry("env", "prod");
    }

    @Test
    void shouldBuildNetworkPolicyWithIngressRules() {
        final NetworkPolicy networkPolicy = NetworkPolicy.builder()
                .name("allow-from-frontend")
                .namespace("default")
                .spec(NetworkPolicySpec.builder()
                        .podSelector(LabelSelector.builder()
                                .matchLabel("role", "db")
                                .build())
                        .policyType("Ingress")
                        .ingressRule(NetworkPolicyIngressRule.builder()
                                .from(NetworkPolicyPeer.builder()
                                        .podSelector(LabelSelector.builder()
                                                .matchLabel("role", "frontend")
                                                .build())
                                        .build())
                                .port(NetworkPolicyPort.builder()
                                        .protocol("TCP")
                                        .port(5432)
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(networkPolicy.getSpec().getIngress()).hasSize(1);
        assertThat(networkPolicy.getSpec().getPolicyTypes()).contains("Ingress");
    }

    @Test
    void shouldBuildNetworkPolicyWithEgressRules() {
        final NetworkPolicy networkPolicy = NetworkPolicy.builder()
                .name("restrict-egress")
                .namespace("default")
                .spec(NetworkPolicySpec.builder()
                        .podSelector(LabelSelector.builder()
                                .matchLabel("app", "web")
                                .build())
                        .policyType("Egress")
                        .egressRule(NetworkPolicyEgressRule.builder()
                                .to(NetworkPolicyPeer.builder()
                                        .podSelector(LabelSelector.builder()
                                                .matchLabel("role", "db")
                                                .build())
                                        .build())
                                .port(NetworkPolicyPort.builder()
                                        .protocol("TCP")
                                        .port(5432)
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(networkPolicy.getSpec().getEgress()).hasSize(1);
        assertThat(networkPolicy.getSpec().getPolicyTypes()).contains("Egress");
    }

    @Test
    void shouldBuildNetworkPolicyWithBothIngressAndEgress() {
        final NetworkPolicy networkPolicy = NetworkPolicy.builder()
                .name("full-policy")
                .namespace("default")
                .spec(NetworkPolicySpec.builder()
                        .podSelector(LabelSelector.builder()
                                .matchLabel("app", "payment")
                                .build())
                        .policyType("Ingress")
                        .policyType("Egress")
                        .ingressRule(NetworkPolicyIngressRule.builder()
                                .from(NetworkPolicyPeer.builder()
                                        .podSelector(LabelSelector.builder()
                                                .matchLabel("app", "frontend")
                                                .build())
                                        .build())
                                .build())
                        .egressRule(NetworkPolicyEgressRule.builder()
                                .to(NetworkPolicyPeer.builder()
                                        .podSelector(LabelSelector.builder()
                                                .matchLabel("app", "database")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(networkPolicy.getSpec().getPolicyTypes()).hasSize(2);
        assertThat(networkPolicy.getSpec().getIngress()).hasSize(1);
        assertThat(networkPolicy.getSpec().getEgress()).hasSize(1);
    }

    @Test
    void shouldBuildNetworkPolicyWithEmptyPodSelector() {
        final NetworkPolicy networkPolicy = NetworkPolicy.builder()
                .name("deny-all")
                .namespace("default")
                .spec(NetworkPolicySpec.builder()
                        .podSelector(LabelSelector.builder().build())
                        .policyType("Ingress")
                        .build())
                .build();

        assertThat(networkPolicy.getSpec().getPodSelector()).isNotNull();
        assertThat(networkPolicy.getSpec().getPodSelector().getMatchLabels()).isEmpty();
    }

    @Test
    void shouldBuildNetworkPolicyWithIpBlock() {
        final NetworkPolicy networkPolicy = NetworkPolicy.builder()
                .name("allow-external")
                .namespace("default")
                .spec(NetworkPolicySpec.builder()
                        .podSelector(LabelSelector.builder()
                                .matchLabel("role", "api")
                                .build())
                        .policyType("Ingress")
                        .ingressRule(NetworkPolicyIngressRule.builder()
                                .from(NetworkPolicyPeer.builder()
                                        .ipBlock(IPBlock.builder()
                                                .cidr("172.17.0.0/16")
                                                .except("172.17.1.0/24")
                                                .build())
                                        .build())
                                .port(NetworkPolicyPort.builder()
                                        .protocol("TCP")
                                        .port(443)
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(networkPolicy.getSpec().getIngress()).hasSize(1);
        assertThat(networkPolicy.getSpec().getIngress().get(0).getFrom().get(0).getIpBlock()).isNotNull();
        assertThat(networkPolicy.getSpec().getIngress().get(0).getFrom().get(0).getIpBlock().getCidr())
                .isEqualTo("172.17.0.0/16");
    }

    @Test
    void shouldSerializeToJson() {
        final NetworkPolicy networkPolicy = NetworkPolicy.builder()
                .name("test-policy")
                .namespace("default")
                .spec(NetworkPolicySpec.builder()
                        .podSelector(LabelSelector.builder()
                                .matchLabel("app", "web")
                                .build())
                        .policyType("Ingress")
                        .build())
                .build();

        final String json = networkPolicy.toJson();

        assertThat(json).contains("\"apiVersion\":\"networking.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"NetworkPolicy\"");
        assertThat(json).contains("\"name\":\"test-policy\"");
        assertThat(json).contains("\"app\":\"web\"");
    }

    @Test
    void shouldSerializeSpecToJson() {
        final NetworkPolicy networkPolicy = NetworkPolicy.builder()
                .name("test-policy")
                .namespace("default")
                .spec(NetworkPolicySpec.builder()
                        .podSelector(LabelSelector.builder().build())
                        .policyType("Ingress")
                        .ingressRule(NetworkPolicyIngressRule.builder()
                                .from(NetworkPolicyPeer.builder()
                                        .podSelector(LabelSelector.builder()
                                                .matchLabel("app", "web")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final String json = networkPolicy.toJson();

        assertThat(json).contains("\"ingress\"");
        assertThat(json).contains("\"app\":\"web\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> NetworkPolicy.builder()
                .namespace("default")
                .spec(NetworkPolicySpec.builder()
                        .podSelector(LabelSelector.builder().build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NetworkPolicy name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> NetworkPolicy.builder()
                .name("")
                .namespace("default")
                .spec(NetworkPolicySpec.builder()
                        .podSelector(LabelSelector.builder().build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NetworkPolicy name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> NetworkPolicy.builder()
                .name("test-policy")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NetworkPolicy spec is required");
    }

    @Test
    void shouldAllowNetworkPolicyWithoutNamespace() {
        final NetworkPolicy networkPolicy = NetworkPolicy.builder()
                .name("test-policy")
                .spec(NetworkPolicySpec.builder()
                        .podSelector(LabelSelector.builder().build())
                        .build())
                .build();

        assertThat(networkPolicy.getName()).isEqualTo("test-policy");
        assertThat(networkPolicy.getNamespace()).isNull();
    }
}
