package io.elev8.resources.persistentvolume;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersistentVolumeTest {

    @Test
    void shouldBuildPersistentVolumeWithRequiredFields() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .capacity("storage", "10Gi")
                .accessMode("ReadWriteOnce")
                .hostPath(HostPathVolumeSource.builder()
                        .path("/mnt/data")
                        .build())
                .build();

        final PersistentVolume pv = PersistentVolume.builder()
                .name("pv-local")
                .spec(spec)
                .build();

        assertThat(pv.getApiVersion()).isEqualTo("v1");
        assertThat(pv.getKind()).isEqualTo("PersistentVolume");
        assertThat(pv.getName()).isEqualTo("pv-local");
        assertThat(pv.getSpec()).isEqualTo(spec);
    }

    @Test
    void shouldBuildPersistentVolumeWithLabels() {
        final PersistentVolume pv = PersistentVolume.builder()
                .name("pv-local")
                .label("type", "local")
                .label("env", "dev")
                .spec(PersistentVolumeSpec.builder()
                        .capacity("storage", "10Gi")
                        .accessMode("ReadWriteOnce")
                        .build())
                .build();

        assertThat(pv.getMetadata().getLabels()).containsEntry("type", "local");
        assertThat(pv.getMetadata().getLabels()).containsEntry("env", "dev");
    }

    @Test
    void shouldBuildPersistentVolumeWithHostPath() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .capacity("storage", "5Gi")
                .accessMode("ReadWriteOnce")
                .persistentVolumeReclaimPolicy("Retain")
                .hostPath(HostPathVolumeSource.builder()
                        .path("/mnt/data")
                        .type("DirectoryOrCreate")
                        .build())
                .build();

        final PersistentVolume pv = PersistentVolume.builder()
                .name("pv-hostpath")
                .spec(spec)
                .build();

        assertThat(pv.getSpec().getHostPath()).isNotNull();
        assertThat(pv.getSpec().getHostPath().getPath()).isEqualTo("/mnt/data");
        assertThat(pv.getSpec().getHostPath().getType()).isEqualTo("DirectoryOrCreate");
    }

    @Test
    void shouldBuildPersistentVolumeWithNFS() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .capacity("storage", "100Gi")
                .accessMode("ReadWriteMany")
                .nfs(NFSVolumeSource.builder()
                        .server("nfs-server.example.com")
                        .path("/exports/data")
                        .readOnly(false)
                        .build())
                .build();

        final PersistentVolume pv = PersistentVolume.builder()
                .name("pv-nfs")
                .spec(spec)
                .build();

        assertThat(pv.getSpec().getNfs()).isNotNull();
        assertThat(pv.getSpec().getNfs().getServer()).isEqualTo("nfs-server.example.com");
        assertThat(pv.getSpec().getNfs().getPath()).isEqualTo("/exports/data");
    }

    @Test
    void shouldBuildPersistentVolumeWithAWSEBS() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .capacity("storage", "20Gi")
                .accessMode("ReadWriteOnce")
                .awsElasticBlockStore(AWSElasticBlockStoreVolumeSource.builder()
                        .volumeID("vol-0123456789abcdef0")
                        .fsType("ext4")
                        .build())
                .build();

        final PersistentVolume pv = PersistentVolume.builder()
                .name("pv-aws-ebs")
                .spec(spec)
                .build();

        assertThat(pv.getSpec().getAwsElasticBlockStore()).isNotNull();
        assertThat(pv.getSpec().getAwsElasticBlockStore().getVolumeID()).isEqualTo("vol-0123456789abcdef0");
    }

    @Test
    void shouldBuildPersistentVolumeWithAzureDisk() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .capacity("storage", "50Gi")
                .accessMode("ReadWriteOnce")
                .azureDisk(AzureDiskVolumeSource.builder()
                        .diskName("my-disk")
                        .diskURI("https://mystorageaccount.blob.core.windows.net/vhds/my-disk.vhd")
                        .build())
                .build();

        final PersistentVolume pv = PersistentVolume.builder()
                .name("pv-azure")
                .spec(spec)
                .build();

        assertThat(pv.getSpec().getAzureDisk()).isNotNull();
        assertThat(pv.getSpec().getAzureDisk().getDiskName()).isEqualTo("my-disk");
    }

    @Test
    void shouldBuildPersistentVolumeWithGCEPD() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .capacity("storage", "30Gi")
                .accessMode("ReadWriteOnce")
                .gcePersistentDisk(GCEPersistentDiskVolumeSource.builder()
                        .pdName("my-gce-disk")
                        .fsType("ext4")
                        .build())
                .build();

        final PersistentVolume pv = PersistentVolume.builder()
                .name("pv-gce")
                .spec(spec)
                .build();

        assertThat(pv.getSpec().getGcePersistentDisk()).isNotNull();
        assertThat(pv.getSpec().getGcePersistentDisk().getPdName()).isEqualTo("my-gce-disk");
    }

    @Test
    void shouldBuildPersistentVolumeWithCSI() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .capacity("storage", "100Gi")
                .accessMode("ReadWriteOnce")
                .csi(CSIPersistentVolumeSource.builder()
                        .driver("ebs.csi.aws.com")
                        .volumeHandle("vol-0123456789abcdef0")
                        .fsType("ext4")
                        .build())
                .build();

        final PersistentVolume pv = PersistentVolume.builder()
                .name("pv-csi")
                .spec(spec)
                .build();

        assertThat(pv.getSpec().getCsi()).isNotNull();
        assertThat(pv.getSpec().getCsi().getDriver()).isEqualTo("ebs.csi.aws.com");
    }

    @Test
    void shouldBuildPersistentVolumeWithMultipleAccessModes() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .capacity("storage", "10Gi")
                .accessMode("ReadWriteOnce")
                .accessMode("ReadOnlyMany")
                .hostPath(HostPathVolumeSource.builder()
                        .path("/mnt/data")
                        .build())
                .build();

        final PersistentVolume pv = PersistentVolume.builder()
                .name("pv-multi-access")
                .spec(spec)
                .build();

        assertThat(pv.getSpec().getAccessModes()).hasSize(2);
        assertThat(pv.getSpec().getAccessModes()).containsExactly("ReadWriteOnce", "ReadOnlyMany");
    }

    @Test
    void shouldBuildPersistentVolumeWithStorageClass() {
        final PersistentVolumeSpec spec = PersistentVolumeSpec.builder()
                .capacity("storage", "10Gi")
                .accessMode("ReadWriteOnce")
                .storageClassName("fast")
                .hostPath(HostPathVolumeSource.builder()
                        .path("/mnt/data")
                        .build())
                .build();

        final PersistentVolume pv = PersistentVolume.builder()
                .name("pv-with-sc")
                .spec(spec)
                .build();

        assertThat(pv.getSpec().getStorageClassName()).isEqualTo("fast");
    }

    @Test
    void shouldBuildPersistentVolumeWithStatus() {
        final PersistentVolumeStatus status = PersistentVolumeStatus.builder()
                .phase("Available")
                .build();

        final PersistentVolume pv = PersistentVolume.builder()
                .name("pv-local")
                .spec(PersistentVolumeSpec.builder()
                        .capacity("storage", "10Gi")
                        .accessMode("ReadWriteOnce")
                        .build())
                .status(status)
                .build();

        assertThat(pv.getStatus()).isNotNull();
        assertThat(pv.getStatus().getPhase()).isEqualTo("Available");
    }

    @Test
    void shouldSerializeToJson() {
        final PersistentVolume pv = PersistentVolume.builder()
                .name("pv-local")
                .spec(PersistentVolumeSpec.builder()
                        .capacity("storage", "10Gi")
                        .accessMode("ReadWriteOnce")
                        .hostPath(HostPathVolumeSource.builder()
                                .path("/mnt/data")
                                .build())
                        .build())
                .build();

        final String json = pv.toJson();

        assertThat(json).contains("\"apiVersion\":\"v1\"");
        assertThat(json).contains("\"kind\":\"PersistentVolume\"");
        assertThat(json).contains("\"name\":\"pv-local\"");
        assertThat(json).contains("\"storage\":\"10Gi\"");
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() {
        final PersistentVolume pv = PersistentVolume.builder()
                .name("pv-local")
                .spec(PersistentVolumeSpec.builder()
                        .capacity("storage", "10Gi")
                        .accessMode("ReadWriteOnce")
                        .build())
                .build();

        final String json = pv.toJson();

        assertThat(json).doesNotContain("\"status\"");
        assertThat(json).doesNotContain("\"hostPath\"");
        assertThat(json).doesNotContain("\"nfs\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> PersistentVolume.builder()
                .spec(PersistentVolumeSpec.builder()
                        .capacity("storage", "10Gi")
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PersistentVolume name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> PersistentVolume.builder()
                .name("")
                .spec(PersistentVolumeSpec.builder()
                        .capacity("storage", "10Gi")
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PersistentVolume name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> PersistentVolume.builder()
                .name("pv-local")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PersistentVolume spec is required");
    }
}
