package io.elev8.resources.networkpolicy;

import io.elev8.resources.LabelSelector;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkPolicyPeerTest {

    @Test
    void shouldBuildPeerWithPodSelector() {
        final NetworkPolicyPeer peer = NetworkPolicyPeer.builder()
                .podSelector(LabelSelector.builder()
                        .matchLabel("role", "frontend")
                        .build())
                .build();

        assertThat(peer.getPodSelector()).isNotNull();
        assertThat(peer.getPodSelector().getMatchLabels()).containsEntry("role", "frontend");
        assertThat(peer.getNamespaceSelector()).isNull();
        assertThat(peer.getIpBlock()).isNull();
    }

    @Test
    void shouldBuildPeerWithNamespaceSelector() {
        final NetworkPolicyPeer peer = NetworkPolicyPeer.builder()
                .namespaceSelector(LabelSelector.builder()
                        .matchLabel("environment", "production")
                        .build())
                .build();

        assertThat(peer.getNamespaceSelector()).isNotNull();
        assertThat(peer.getNamespaceSelector().getMatchLabels()).containsEntry("environment", "production");
        assertThat(peer.getPodSelector()).isNull();
        assertThat(peer.getIpBlock()).isNull();
    }

    @Test
    void shouldBuildPeerWithIpBlock() {
        final NetworkPolicyPeer peer = NetworkPolicyPeer.builder()
                .ipBlock(IPBlock.builder()
                        .cidr("172.17.0.0/16")
                        .except("172.17.1.0/24")
                        .build())
                .build();

        assertThat(peer.getIpBlock()).isNotNull();
        assertThat(peer.getIpBlock().getCidr()).isEqualTo("172.17.0.0/16");
        assertThat(peer.getIpBlock().getExcept()).contains("172.17.1.0/24");
        assertThat(peer.getPodSelector()).isNull();
        assertThat(peer.getNamespaceSelector()).isNull();
    }

    @Test
    void shouldBuildPeerWithPodAndNamespaceSelector() {
        final NetworkPolicyPeer peer = NetworkPolicyPeer.builder()
                .podSelector(LabelSelector.builder()
                        .matchLabel("app", "backend")
                        .build())
                .namespaceSelector(LabelSelector.builder()
                        .matchLabel("team", "platform")
                        .build())
                .build();

        assertThat(peer.getPodSelector()).isNotNull();
        assertThat(peer.getNamespaceSelector()).isNotNull();
        assertThat(peer.getIpBlock()).isNull();
    }

    @Test
    void shouldBuildPeerWithEmptyPodSelector() {
        final NetworkPolicyPeer peer = NetworkPolicyPeer.builder()
                .podSelector(LabelSelector.builder().build())
                .build();

        assertThat(peer.getPodSelector()).isNotNull();
        assertThat(peer.getPodSelector().getMatchLabels()).isEmpty();
    }
}
