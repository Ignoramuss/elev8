package io.elev8.resources.persistentvolume;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersistentVolumeSpecTest {

    @Test
    void shouldBuildSpecWithCapacity() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .capacity("storage", "10Gi")
                .build();

        assertThat(spec.getCapacity()).containsEntry("storage", "10Gi");
    }

    @Test
    void shouldBuildSpecWithMultipleAccessModes() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .accessMode("ReadWriteOnce")
                .accessMode("ReadOnlyMany")
                .build();

        assertThat(spec.getAccessModes()).hasSize(2);
        assertThat(spec.getAccessModes()).containsExactly("ReadWriteOnce", "ReadOnlyMany");
    }

    @Test
    void shouldBuildSpecWithReclaimPolicy() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .persistentVolumeReclaimPolicy("Retain")
                .build();

        assertThat(spec.getPersistentVolumeReclaimPolicy()).isEqualTo("Retain");
    }

    @Test
    void shouldBuildSpecWithStorageClassName() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .storageClassName("fast")
                .build();

        assertThat(spec.getStorageClassName()).isEqualTo("fast");
    }

    @Test
    void shouldBuildSpecWithVolumeMode() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .volumeMode("Block")
                .build();

        assertThat(spec.getVolumeMode()).isEqualTo("Block");
    }

    @Test
    void shouldBuildSpecWithMountOptions() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .mountOption("ro")
                .mountOption("soft")
                .build();

        assertThat(spec.getMountOptions()).hasSize(2);
        assertThat(spec.getMountOptions()).containsExactly("ro", "soft");
    }

    @Test
    void shouldBuildSpecWithHostPathSource() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .hostPath(HostPathVolumeSource.builder()
                        .path("/mnt/data")
                        .type("Directory")
                        .build())
                .build();

        assertThat(spec.getHostPath()).isNotNull();
        assertThat(spec.getHostPath().getPath()).isEqualTo("/mnt/data");
    }

    @Test
    void shouldBuildSpecWithNFSSource() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .nfs(NFSVolumeSource.builder()
                        .server("192.168.1.100")
                        .path("/exports")
                        .build())
                .build();

        assertThat(spec.getNfs()).isNotNull();
        assertThat(spec.getNfs().getServer()).isEqualTo("192.168.1.100");
    }

    @Test
    void shouldBuildSpecWithAWSEBS() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .awsElasticBlockStore(AWSElasticBlockStoreVolumeSource.builder()
                        .volumeID("vol-abc123")
                        .fsType("ext4")
                        .build())
                .build();

        assertThat(spec.getAwsElasticBlockStore()).isNotNull();
        assertThat(spec.getAwsElasticBlockStore().getVolumeID()).isEqualTo("vol-abc123");
    }

    @Test
    void shouldBuildCompleteSpec() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .capacity("storage", "100Gi")
                .accessMode("ReadWriteMany")
                .persistentVolumeReclaimPolicy("Retain")
                .storageClassName("nfs")
                .volumeMode("Filesystem")
                .mountOption("ro")
                .nfs(NFSVolumeSource.builder()
                        .server("nfs.example.com")
                        .path("/data")
                        .build())
                .build();

        assertThat(spec.getCapacity()).containsEntry("storage", "100Gi");
        assertThat(spec.getAccessModes()).hasSize(1);
        assertThat(spec.getPersistentVolumeReclaimPolicy()).isEqualTo("Retain");
        assertThat(spec.getStorageClassName()).isEqualTo("nfs");
        assertThat(spec.getNfs()).isNotNull();
    }
}
