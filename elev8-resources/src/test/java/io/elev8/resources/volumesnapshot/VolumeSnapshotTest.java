package io.elev8.resources.volumesnapshot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VolumeSnapshotTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildVolumeSnapshotWithRequiredFields() {
        final VolumeSnapshot snapshot = VolumeSnapshot.builder()
                .name("my-snapshot")
                .namespace("default")
                .spec(VolumeSnapshotSpec.builder()
                        .source(VolumeSnapshotSource.builder()
                                .persistentVolumeClaimName("my-pvc")
                                .build())
                        .build())
                .build();

        assertThat(snapshot.getApiVersion()).isEqualTo("snapshot.storage.k8s.io/v1");
        assertThat(snapshot.getKind()).isEqualTo("VolumeSnapshot");
        assertThat(snapshot.getName()).isEqualTo("my-snapshot");
        assertThat(snapshot.getNamespace()).isEqualTo("default");
    }

    @Test
    void shouldBuildVolumeSnapshotWithPVCSource() {
        final VolumeSnapshot snapshot = VolumeSnapshot.builder()
                .name("pvc-snapshot")
                .namespace("production")
                .spec(VolumeSnapshotSpec.builder()
                        .source(VolumeSnapshotSource.builder()
                                .persistentVolumeClaimName("data-pvc")
                                .build())
                        .volumeSnapshotClassName("csi-snapclass")
                        .build())
                .build();

        assertThat(snapshot.getSpec().getSource().getPersistentVolumeClaimName()).isEqualTo("data-pvc");
        assertThat(snapshot.getSpec().getSource().getVolumeSnapshotContentName()).isNull();
        assertThat(snapshot.getSpec().getVolumeSnapshotClassName()).isEqualTo("csi-snapclass");
    }

    @Test
    void shouldBuildVolumeSnapshotWithContentSource() {
        final VolumeSnapshot snapshot = VolumeSnapshot.builder()
                .name("content-snapshot")
                .namespace("default")
                .spec(VolumeSnapshotSpec.builder()
                        .source(VolumeSnapshotSource.builder()
                                .volumeSnapshotContentName("snapcontent-123")
                                .build())
                        .build())
                .build();

        assertThat(snapshot.getSpec().getSource().getVolumeSnapshotContentName()).isEqualTo("snapcontent-123");
        assertThat(snapshot.getSpec().getSource().getPersistentVolumeClaimName()).isNull();
    }

    @Test
    void shouldBuildVolumeSnapshotWithSnapshotClassName() {
        final VolumeSnapshot snapshot = VolumeSnapshot.builder()
                .name("class-snapshot")
                .namespace("default")
                .spec(VolumeSnapshotSpec.builder()
                        .source(VolumeSnapshotSource.builder()
                                .persistentVolumeClaimName("my-pvc")
                                .build())
                        .volumeSnapshotClassName("premium-snapclass")
                        .build())
                .build();

        assertThat(snapshot.getSpec().getVolumeSnapshotClassName()).isEqualTo("premium-snapclass");
    }

    @Test
    void shouldBuildVolumeSnapshotWithoutSnapshotClassName() {
        final VolumeSnapshot snapshot = VolumeSnapshot.builder()
                .name("default-class-snapshot")
                .namespace("default")
                .spec(VolumeSnapshotSpec.builder()
                        .source(VolumeSnapshotSource.builder()
                                .persistentVolumeClaimName("my-pvc")
                                .build())
                        .build())
                .build();

        assertThat(snapshot.getSpec().getVolumeSnapshotClassName()).isNull();
    }

    @Test
    void shouldBuildVolumeSnapshotWithStatus() {
        final VolumeSnapshot snapshot = VolumeSnapshot.builder()
                .name("status-snapshot")
                .namespace("default")
                .spec(VolumeSnapshotSpec.builder()
                        .source(VolumeSnapshotSource.builder()
                                .persistentVolumeClaimName("my-pvc")
                                .build())
                        .build())
                .status(VolumeSnapshotStatus.builder()
                        .boundVolumeSnapshotContentName("snapcontent-456")
                        .creationTime("2025-01-01T00:00:00Z")
                        .readyToUse(true)
                        .restoreSize("10Gi")
                        .build())
                .build();

        assertThat(snapshot.getStatus().getBoundVolumeSnapshotContentName()).isEqualTo("snapcontent-456");
        assertThat(snapshot.getStatus().getCreationTime()).isEqualTo("2025-01-01T00:00:00Z");
        assertThat(snapshot.getStatus().getReadyToUse()).isTrue();
        assertThat(snapshot.getStatus().getRestoreSize()).isEqualTo("10Gi");
    }

    @Test
    void shouldBuildVolumeSnapshotWithReadyStatus() {
        final VolumeSnapshot snapshot = VolumeSnapshot.builder()
                .name("ready-snapshot")
                .namespace("default")
                .spec(VolumeSnapshotSpec.builder()
                        .source(VolumeSnapshotSource.builder()
                                .persistentVolumeClaimName("my-pvc")
                                .build())
                        .build())
                .status(VolumeSnapshotStatus.builder()
                        .readyToUse(true)
                        .restoreSize("5Ti")
                        .build())
                .build();

        assertThat(snapshot.getStatus().getReadyToUse()).isTrue();
        assertThat(snapshot.getStatus().getRestoreSize()).isEqualTo("5Ti");
    }

    @Test
    void shouldBuildVolumeSnapshotWithError() {
        final VolumeSnapshot snapshot = VolumeSnapshot.builder()
                .name("error-snapshot")
                .namespace("default")
                .spec(VolumeSnapshotSpec.builder()
                        .source(VolumeSnapshotSource.builder()
                                .persistentVolumeClaimName("my-pvc")
                                .build())
                        .build())
                .status(VolumeSnapshotStatus.builder()
                        .error(VolumeSnapshotError.builder()
                                .message("Failed to create snapshot: Volume not found")
                                .time("2025-01-01T00:00:00Z")
                                .build())
                        .build())
                .build();

        assertThat(snapshot.getStatus().getError()).isNotNull();
        assertThat(snapshot.getStatus().getError().getMessage()).contains("Failed to create snapshot");
        assertThat(snapshot.getStatus().getError().getTime()).isEqualTo("2025-01-01T00:00:00Z");
    }

    @Test
    void shouldBuildVolumeSnapshotWithVolumeGroupReference() {
        final VolumeSnapshot snapshot = VolumeSnapshot.builder()
                .name("group-snapshot")
                .namespace("default")
                .spec(VolumeSnapshotSpec.builder()
                        .source(VolumeSnapshotSource.builder()
                                .persistentVolumeClaimName("my-pvc")
                                .build())
                        .build())
                .status(VolumeSnapshotStatus.builder()
                        .volumeGroupSnapshotName("my-group-snapshot")
                        .build())
                .build();

        assertThat(snapshot.getStatus().getVolumeGroupSnapshotName()).isEqualTo("my-group-snapshot");
    }

    @Test
    void shouldBuildVolumeSnapshotWithLabels() {
        final VolumeSnapshot snapshot = VolumeSnapshot.builder()
                .name("labeled-snapshot")
                .namespace("production")
                .label("environment", "production")
                .label("backup-type", "daily")
                .spec(VolumeSnapshotSpec.builder()
                        .source(VolumeSnapshotSource.builder()
                                .persistentVolumeClaimName("my-pvc")
                                .build())
                        .build())
                .build();

        assertThat(snapshot.getMetadata().getLabels())
                .containsEntry("environment", "production")
                .containsEntry("backup-type", "daily");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> VolumeSnapshot.builder()
                .namespace("default")
                .spec(VolumeSnapshotSpec.builder()
                        .source(VolumeSnapshotSource.builder()
                                .persistentVolumeClaimName("my-pvc")
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VolumeSnapshot name is required");
    }

    @Test
    void shouldThrowExceptionWhenNamespaceIsNull() {
        assertThatThrownBy(() -> VolumeSnapshot.builder()
                .name("test-snapshot")
                .spec(VolumeSnapshotSpec.builder()
                        .source(VolumeSnapshotSource.builder()
                                .persistentVolumeClaimName("my-pvc")
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VolumeSnapshot namespace is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> VolumeSnapshot.builder()
                .name("test-snapshot")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VolumeSnapshot spec is required");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final VolumeSnapshot snapshot = VolumeSnapshot.builder()
                .name("serialize-snapshot")
                .namespace("default")
                .spec(VolumeSnapshotSpec.builder()
                        .source(VolumeSnapshotSource.builder()
                                .persistentVolumeClaimName("my-pvc")
                                .build())
                        .volumeSnapshotClassName("csi-snapclass")
                        .build())
                .build();

        final String json = objectMapper.writeValueAsString(snapshot);

        assertThat(json).contains("\"apiVersion\":\"snapshot.storage.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"VolumeSnapshot\"");
        assertThat(json).contains("\"name\":\"serialize-snapshot\"");
        assertThat(json).contains("\"namespace\":\"default\"");
        assertThat(json).contains("\"persistentVolumeClaimName\":\"my-pvc\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "apiVersion": "snapshot.storage.k8s.io/v1",
                  "kind": "VolumeSnapshot",
                  "metadata": {
                    "name": "deserialize-snapshot",
                    "namespace": "default"
                  },
                  "spec": {
                    "source": {
                      "persistentVolumeClaimName": "my-pvc"
                    },
                    "volumeSnapshotClassName": "csi-snapclass"
                  },
                  "status": {
                    "readyToUse": true,
                    "restoreSize": "10Gi"
                  }
                }
                """;

        final VolumeSnapshot snapshot = objectMapper.readValue(json, VolumeSnapshot.class);

        assertThat(snapshot.getApiVersion()).isEqualTo("snapshot.storage.k8s.io/v1");
        assertThat(snapshot.getKind()).isEqualTo("VolumeSnapshot");
        assertThat(snapshot.getName()).isEqualTo("deserialize-snapshot");
        assertThat(snapshot.getNamespace()).isEqualTo("default");
        assertThat(snapshot.getSpec().getSource().getPersistentVolumeClaimName()).isEqualTo("my-pvc");
        assertThat(snapshot.getStatus().getReadyToUse()).isTrue();
    }
}
