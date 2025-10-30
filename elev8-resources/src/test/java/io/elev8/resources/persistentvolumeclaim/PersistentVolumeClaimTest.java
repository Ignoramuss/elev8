package io.elev8.resources.persistentvolumeclaim;

import io.elev8.resources.ResourceRequirements;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersistentVolumeClaimTest {

    @Test
    void shouldBuildPVCWithRequiredFields() {
        final PersistentVolumeClaimSpec spec = PersistentVolumeClaimSpec.builder()
                .accessMode("ReadWriteOnce")
                .resources(ResourceRequirements.builder()
                        .request("storage", "10Gi")
                        .build())
                .build();

        final PersistentVolumeClaim pvc = PersistentVolumeClaim.builder()
                .name("my-pvc")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(pvc.getApiVersion()).isEqualTo("v1");
        assertThat(pvc.getKind()).isEqualTo("PersistentVolumeClaim");
        assertThat(pvc.getName()).isEqualTo("my-pvc");
        assertThat(pvc.getNamespace()).isEqualTo("default");
        assertThat(pvc.getSpec()).isEqualTo(spec);
    }

    @Test
    void shouldBuildPVCWithLabels() {
        final PersistentVolumeClaim pvc = PersistentVolumeClaim.builder()
                .name("my-pvc")
                .namespace("default")
                .label("app", "backend")
                .label("env", "prod")
                .spec(PersistentVolumeClaimSpec.builder()
                        .accessMode("ReadWriteOnce")
                        .resources(ResourceRequirements.builder()
                                .request("storage", "10Gi")
                                .build())
                        .build())
                .build();

        assertThat(pvc.getMetadata().getLabels()).containsEntry("app", "backend");
        assertThat(pvc.getMetadata().getLabels()).containsEntry("env", "prod");
    }

    @Test
    void shouldBuildPVCWithStorageClass() {
        final PersistentVolumeClaimSpec spec = PersistentVolumeClaimSpec.builder()
                .accessMode("ReadWriteOnce")
                .storageClassName("gp2")
                .resources(ResourceRequirements.builder()
                        .request("storage", "100Gi")
                        .build())
                .build();

        final PersistentVolumeClaim pvc = PersistentVolumeClaim.builder()
                .name("ebs-pvc")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(pvc.getSpec().getStorageClassName()).isEqualTo("gp2");
        assertThat(pvc.getSpec().getResources().getRequests()).containsEntry("storage", "100Gi");
    }

    @Test
    void shouldBuildPVCWithMultipleAccessModes() {
        final PersistentVolumeClaimSpec spec = PersistentVolumeClaimSpec.builder()
                .accessMode("ReadWriteOnce")
                .accessMode("ReadWriteMany")
                .resources(ResourceRequirements.builder()
                        .request("storage", "50Gi")
                        .build())
                .build();

        final PersistentVolumeClaim pvc = PersistentVolumeClaim.builder()
                .name("shared-pvc")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(pvc.getSpec().getAccessModes()).containsExactly("ReadWriteOnce", "ReadWriteMany");
    }

    @Test
    void shouldBuildPVCWithVolumeMode() {
        final PersistentVolumeClaimSpec spec = PersistentVolumeClaimSpec.builder()
                .accessMode("ReadWriteOnce")
                .volumeMode("Block")
                .resources(ResourceRequirements.builder()
                        .request("storage", "10Gi")
                        .build())
                .build();

        final PersistentVolumeClaim pvc = PersistentVolumeClaim.builder()
                .name("block-pvc")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(pvc.getSpec().getVolumeMode()).isEqualTo("Block");
    }

    @Test
    void shouldRequireNameForBuild() {
        assertThatThrownBy(() -> PersistentVolumeClaim.builder()
                .namespace("default")
                .spec(PersistentVolumeClaimSpec.builder()
                        .accessMode("ReadWriteOnce")
                        .resources(ResourceRequirements.builder()
                                .request("storage", "10Gi")
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name is required");
    }

    @Test
    void shouldRequireSpecForBuild() {
        assertThatThrownBy(() -> PersistentVolumeClaim.builder()
                .name("my-pvc")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("spec is required");
    }

    @Test
    void shouldSerializeToJson() {
        final PersistentVolumeClaim pvc = PersistentVolumeClaim.builder()
                .name("my-pvc")
                .namespace("default")
                .spec(PersistentVolumeClaimSpec.builder()
                        .accessMode("ReadWriteOnce")
                        .storageClassName("gp2")
                        .resources(ResourceRequirements.builder()
                                .request("storage", "10Gi")
                                .build())
                        .build())
                .build();

        final String json = pvc.toJson();
        assertThat(json).contains("\"apiVersion\":\"v1\"");
        assertThat(json).contains("\"kind\":\"PersistentVolumeClaim\"");
        assertThat(json).contains("\"name\":\"my-pvc\"");
        assertThat(json).contains("\"namespace\":\"default\"");
        assertThat(json).contains("\"storageClassName\":\"gp2\"");
    }
}
