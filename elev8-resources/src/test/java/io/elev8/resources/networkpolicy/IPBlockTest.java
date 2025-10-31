package io.elev8.resources.networkpolicy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IPBlockTest {

    @Test
    void shouldBuildIPBlockWithCidr() {
        final IPBlock ipBlock = IPBlock.builder()
                .cidr("192.168.1.0/24")
                .build();

        assertThat(ipBlock.getCidr()).isEqualTo("192.168.1.0/24");
        assertThat(ipBlock.getExcept()).isEmpty();
    }

    @Test
    void shouldBuildIPBlockWithExceptions() {
        final IPBlock ipBlock = IPBlock.builder()
                .cidr("172.17.0.0/16")
                .except("172.17.1.0/24")
                .except("172.17.2.0/24")
                .build();

        assertThat(ipBlock.getCidr()).isEqualTo("172.17.0.0/16");
        assertThat(ipBlock.getExcept()).hasSize(2);
        assertThat(ipBlock.getExcept()).contains("172.17.1.0/24", "172.17.2.0/24");
    }

    @Test
    void shouldBuildIPBlockWithIPv6() {
        final IPBlock ipBlock = IPBlock.builder()
                .cidr("2001:db8::/64")
                .except("2001:db8::1/128")
                .build();

        assertThat(ipBlock.getCidr()).isEqualTo("2001:db8::/64");
        assertThat(ipBlock.getExcept()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenCidrIsNull() {
        assertThatThrownBy(() -> IPBlock.builder()
                .build())
                .isInstanceOf(NullPointerException.class);
    }
}
