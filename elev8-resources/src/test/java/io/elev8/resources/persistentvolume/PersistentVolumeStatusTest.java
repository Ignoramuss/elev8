package io.elev8.resources.persistentvolume;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersistentVolumeStatusTest {

    @Test
    void shouldBuildStatusWithPhase() {
        final PersistentVolumeStatus status = PersistentVolumeStatus.builder()
                .phase("Available")
                .build();

        assertThat(status.getPhase()).isEqualTo("Available");
    }

    @Test
    void shouldBuildStatusWithBoundPhase() {
        final PersistentVolumeStatus status = PersistentVolumeStatus.builder()
                .phase("Bound")
                .build();

        assertThat(status.getPhase()).isEqualTo("Bound");
    }

    @Test
    void shouldBuildStatusWithMessage() {
        final PersistentVolumeStatus status = PersistentVolumeStatus.builder()
                .phase("Failed")
                .message("Volume provisioning failed")
                .reason("ProvisioningFailed")
                .build();

        assertThat(status.getPhase()).isEqualTo("Failed");
        assertThat(status.getMessage()).isEqualTo("Volume provisioning failed");
        assertThat(status.getReason()).isEqualTo("ProvisioningFailed");
    }

    @Test
    void shouldBuildStatusWithReason() {
        final PersistentVolumeStatus status = PersistentVolumeStatus.builder()
                .phase("Released")
                .reason("ClaimDeleted")
                .build();

        assertThat(status.getPhase()).isEqualTo("Released");
        assertThat(status.getReason()).isEqualTo("ClaimDeleted");
    }
}
