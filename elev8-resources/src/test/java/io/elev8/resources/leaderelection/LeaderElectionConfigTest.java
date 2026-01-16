package io.elev8.resources.leaderelection;

import io.elev8.resources.lease.LeaseManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class LeaderElectionConfigTest {

    @Mock
    private LeaseManager leaseManager;

    @Mock
    private LeaderCallbacks callbacks;

    @Test
    void shouldBuildValidConfig() {
        final LeaderElectionConfig config = LeaderElectionConfig.builder()
                .leaseManager(leaseManager)
                .namespace("kube-system")
                .leaseName("test-leader")
                .identity("pod-abc123")
                .leaseDuration(Duration.ofSeconds(15))
                .renewDeadline(Duration.ofSeconds(10))
                .retryPeriod(Duration.ofSeconds(2))
                .callbacks(callbacks)
                .build();

        config.validate();

        assertThat(config.getLeaseManager()).isSameAs(leaseManager);
        assertThat(config.getNamespace()).isEqualTo("kube-system");
        assertThat(config.getLeaseName()).isEqualTo("test-leader");
        assertThat(config.getIdentity()).isEqualTo("pod-abc123");
        assertThat(config.getLeaseDuration()).isEqualTo(Duration.ofSeconds(15));
        assertThat(config.getRenewDeadline()).isEqualTo(Duration.ofSeconds(10));
        assertThat(config.getRetryPeriod()).isEqualTo(Duration.ofSeconds(2));
    }

    @Test
    void shouldHaveDefaultValues() {
        final LeaderElectionConfig config = LeaderElectionConfig.builder()
                .leaseManager(leaseManager)
                .namespace("default")
                .leaseName("leader")
                .identity("test")
                .callbacks(callbacks)
                .build();

        assertThat(config.getLeaseDuration()).isEqualTo(Duration.ofSeconds(15));
        assertThat(config.getRenewDeadline()).isEqualTo(Duration.ofSeconds(10));
        assertThat(config.getRetryPeriod()).isEqualTo(Duration.ofSeconds(2));
        assertThat(config.isReleaseOnCancel()).isTrue();
    }

    @Test
    void shouldRejectNullLeaseManager() {
        final LeaderElectionConfig config = LeaderElectionConfig.builder()
                .namespace("default")
                .leaseName("leader")
                .identity("test")
                .callbacks(callbacks)
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("leaseManager");
    }

    @Test
    void shouldRejectNullNamespace() {
        final LeaderElectionConfig config = LeaderElectionConfig.builder()
                .leaseManager(leaseManager)
                .leaseName("leader")
                .identity("test")
                .callbacks(callbacks)
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("namespace");
    }

    @Test
    void shouldRejectNullLeaseName() {
        final LeaderElectionConfig config = LeaderElectionConfig.builder()
                .leaseManager(leaseManager)
                .namespace("default")
                .identity("test")
                .callbacks(callbacks)
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("leaseName");
    }

    @Test
    void shouldRejectNullIdentity() {
        final LeaderElectionConfig config = LeaderElectionConfig.builder()
                .leaseManager(leaseManager)
                .namespace("default")
                .leaseName("leader")
                .callbacks(callbacks)
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identity");
    }

    @Test
    void shouldRejectNullCallbacks() {
        final LeaderElectionConfig config = LeaderElectionConfig.builder()
                .leaseManager(leaseManager)
                .namespace("default")
                .leaseName("leader")
                .identity("test")
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("callbacks");
    }

    @Test
    void shouldRejectInvalidDurations() {
        final LeaderElectionConfig config = LeaderElectionConfig.builder()
                .leaseManager(leaseManager)
                .namespace("default")
                .leaseName("leader")
                .identity("test")
                .callbacks(callbacks)
                .leaseDuration(Duration.ofSeconds(10))
                .renewDeadline(Duration.ofSeconds(15))
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("leaseDuration must be greater than renewDeadline");
    }

    @Test
    void shouldRejectInvalidRetryPeriod() {
        final LeaderElectionConfig config = LeaderElectionConfig.builder()
                .leaseManager(leaseManager)
                .namespace("default")
                .leaseName("leader")
                .identity("test")
                .callbacks(callbacks)
                .leaseDuration(Duration.ofSeconds(15))
                .renewDeadline(Duration.ofSeconds(2))
                .retryPeriod(Duration.ofSeconds(5))
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("renewDeadline must be greater than retryPeriod");
    }
}
