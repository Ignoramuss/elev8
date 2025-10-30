package io.elev8.resources.persistentvolumeclaim;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersistentVolumeClaimStatusTest {

    @Test
    void shouldBuildStatusWithPhase() {
        final PersistentVolumeClaimStatus status = PersistentVolumeClaimStatus.builder()
                .phase("Bound")
                .build();

        assertThat(status.getPhase()).isEqualTo("Bound");
    }

    @Test
    void shouldBuildStatusWithCapacity() {
        final PersistentVolumeClaimStatus status = PersistentVolumeClaimStatus.builder()
                .phase("Bound")
                .capacity("storage", "10Gi")
                .build();

        assertThat(status.getCapacity()).containsEntry("storage", "10Gi");
    }

    @Test
    void shouldBuildStatusWithAccessModes() {
        final PersistentVolumeClaimStatus status = PersistentVolumeClaimStatus.builder()
                .phase("Bound")
                .accessMode("ReadWriteOnce")
                .capacity("storage", "10Gi")
                .build();

        assertThat(status.getAccessModes()).containsExactly("ReadWriteOnce");
    }

    @Test
    void shouldBuildStatusWithConditions() {
        final PersistentVolumeClaimStatus.PersistentVolumeClaimCondition condition =
                PersistentVolumeClaimStatus.PersistentVolumeClaimCondition.builder()
                        .type("Resizing")
                        .status("True")
                        .reason("ExpandingVolume")
                        .message("Expanding volume from 10Gi to 20Gi")
                        .build();

        final PersistentVolumeClaimStatus status = PersistentVolumeClaimStatus.builder()
                .phase("Bound")
                .condition(condition)
                .build();

        assertThat(status.getConditions()).hasSize(1);
        assertThat(status.getConditions().get(0).getType()).isEqualTo("Resizing");
        assertThat(status.getConditions().get(0).getStatus()).isEqualTo("True");
    }

    @Test
    void shouldBuildStatusWithAllocatedResources() {
        final PersistentVolumeClaimStatus status = PersistentVolumeClaimStatus.builder()
                .phase("Bound")
                .allocatedResource("storage", "10Gi")
                .build();

        assertThat(status.getAllocatedResources()).containsEntry("storage", "10Gi");
    }
}
