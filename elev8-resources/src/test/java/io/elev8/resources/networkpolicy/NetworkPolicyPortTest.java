package io.elev8.resources.networkpolicy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkPolicyPortTest {

    @Test
    void shouldBuildPortWithProtocolAndNumber() {
        final NetworkPolicyPort port = NetworkPolicyPort.builder()
                .protocol("TCP")
                .port(8080)
                .build();

        assertThat(port.getProtocol()).isEqualTo("TCP");
        assertThat(port.getPort()).isEqualTo(8080);
        assertThat(port.getEndPort()).isNull();
    }

    @Test
    void shouldBuildPortWithProtocolAndString() {
        final NetworkPolicyPort port = NetworkPolicyPort.builder()
                .protocol("TCP")
                .port("http")
                .build();

        assertThat(port.getProtocol()).isEqualTo("TCP");
        assertThat(port.getPort()).isEqualTo("http");
    }

    @Test
    void shouldBuildPortWithEndPort() {
        final NetworkPolicyPort port = NetworkPolicyPort.builder()
                .protocol("TCP")
                .port(8000)
                .endPort(9000)
                .build();

        assertThat(port.getProtocol()).isEqualTo("TCP");
        assertThat(port.getPort()).isEqualTo(8000);
        assertThat(port.getEndPort()).isEqualTo(9000);
    }

    @Test
    void shouldBuildPortWithUdpProtocol() {
        final NetworkPolicyPort port = NetworkPolicyPort.builder()
                .protocol("UDP")
                .port(53)
                .build();

        assertThat(port.getProtocol()).isEqualTo("UDP");
        assertThat(port.getPort()).isEqualTo(53);
    }

    @Test
    void shouldAllowPortWithoutProtocol() {
        final NetworkPolicyPort port = NetworkPolicyPort.builder()
                .port(443)
                .build();

        assertThat(port.getProtocol()).isNull();
        assertThat(port.getPort()).isEqualTo(443);
    }
}
