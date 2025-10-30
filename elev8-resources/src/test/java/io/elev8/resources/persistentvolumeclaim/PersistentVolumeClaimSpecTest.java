package io.elev8.resources.persistentvolumeclaim;

import io.elev8.resources.LabelSelector;
import io.elev8.resources.ResourceRequirements;
import io.elev8.resources.TypedLocalObjectReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersistentVolumeClaimSpecTest {

    @Test
    void shouldBuildSpecWithStorageRequest() {
        final PersistentVolumeClaimSpec spec = PersistentVolumeClaimSpec.builder()
                .accessMode("ReadWriteOnce")
                .resources(ResourceRequirements.builder()
                        .request("storage", "10Gi")
                        .build())
                .build();

        assertThat(spec.getAccessModes()).containsExactly("ReadWriteOnce");
        assertThat(spec.getResources().getRequests()).containsEntry("storage", "10Gi");
    }

    @Test
    void shouldBuildSpecWithStorageClassName() {
        final PersistentVolumeClaimSpec spec = PersistentVolumeClaimSpec.builder()
                .storageClassName("fast-ssd")
                .accessMode("ReadWriteOnce")
                .resources(ResourceRequirements.builder()
                        .request("storage", "50Gi")
                        .build())
                .build();

        assertThat(spec.getStorageClassName()).isEqualTo("fast-ssd");
    }

    @Test
    void shouldBuildSpecWithVolumeSelector() {
        final LabelSelector selector = LabelSelector.builder()
                .matchLabel("type", "ssd")
                .matchLabel("environment", "production")
                .build();

        final PersistentVolumeClaimSpec spec = PersistentVolumeClaimSpec.builder()
                .selector(selector)
                .accessMode("ReadWriteOnce")
                .resources(ResourceRequirements.builder()
                        .request("storage", "100Gi")
                        .build())
                .build();

        assertThat(spec.getSelector()).isEqualTo(selector);
        assertThat(spec.getSelector().getMatchLabels()).containsEntry("type", "ssd");
    }

    @Test
    void shouldBuildSpecWithVolumeName() {
        final PersistentVolumeClaimSpec spec = PersistentVolumeClaimSpec.builder()
                .volumeName("my-existing-pv")
                .accessMode("ReadWriteOnce")
                .resources(ResourceRequirements.builder()
                        .request("storage", "10Gi")
                        .build())
                .build();

        assertThat(spec.getVolumeName()).isEqualTo("my-existing-pv");
    }

    @Test
    void shouldBuildSpecWithDataSource() {
        final TypedLocalObjectReference dataSource = TypedLocalObjectReference.builder()
                .apiGroup("snapshot.storage.k8s.io")
                .kind("VolumeSnapshot")
                .name("my-snapshot")
                .build();

        final PersistentVolumeClaimSpec spec = PersistentVolumeClaimSpec.builder()
                .dataSource(dataSource)
                .accessMode("ReadWriteOnce")
                .resources(ResourceRequirements.builder()
                        .request("storage", "20Gi")
                        .build())
                .build();

        assertThat(spec.getDataSource()).isEqualTo(dataSource);
        assertThat(spec.getDataSource().getKind()).isEqualTo("VolumeSnapshot");
    }

    @Test
    void shouldBuildSpecWithBlockVolumeMode() {
        final PersistentVolumeClaimSpec spec = PersistentVolumeClaimSpec.builder()
                .volumeMode("Block")
                .accessMode("ReadWriteOnce")
                .resources(ResourceRequirements.builder()
                        .request("storage", "10Gi")
                        .build())
                .build();

        assertThat(spec.getVolumeMode()).isEqualTo("Block");
    }
}
