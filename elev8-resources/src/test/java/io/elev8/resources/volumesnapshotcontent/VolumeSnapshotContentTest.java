package io.elev8.resources.volumesnapshotcontent;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.elev8.resources.ObjectReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VolumeSnapshotContentTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildVolumeSnapshotContentWithRequiredFields() {
        final VolumeSnapshotContent content = VolumeSnapshotContent.builder()
                .name("snapcontent-123")
                .spec(VolumeSnapshotContentSpec.builder()
                        .volumeSnapshotRef(ObjectReference.builder()
                                .apiVersion("snapshot.storage.k8s.io/v1")
                                .kind("VolumeSnapshot")
                                .name("my-snapshot")
                                .namespace("default")
                                .build())
                        .deletionPolicy("Delete")
                        .driver("ebs.csi.aws.com")
                        .source(VolumeSnapshotContentSource.builder()
                                .volumeHandle("vol-1234567890abcdef0")
                                .build())
                        .build())
                .build();

        assertThat(content.getApiVersion()).isEqualTo("snapshot.storage.k8s.io/v1");
        assertThat(content.getKind()).isEqualTo("VolumeSnapshotContent");
        assertThat(content.getName()).isEqualTo("snapcontent-123");
    }

    @Test
    void shouldBuildVolumeSnapshotContentWithDynamicSource() {
        final VolumeSnapshotContent content = VolumeSnapshotContent.builder()
                .name("dynamic-snapcontent")
                .spec(VolumeSnapshotContentSpec.builder()
                        .volumeSnapshotRef(ObjectReference.builder()
                                .kind("VolumeSnapshot")
                                .name("my-snapshot")
                                .namespace("default")
                                .build())
                        .deletionPolicy("Delete")
                        .driver("ebs.csi.aws.com")
                        .source(VolumeSnapshotContentSource.builder()
                                .volumeHandle("vol-abc123")
                                .build())
                        .build())
                .build();

        assertThat(content.getSpec().getSource().getVolumeHandle()).isEqualTo("vol-abc123");
        assertThat(content.getSpec().getSource().getSnapshotHandle()).isNull();
    }

    @Test
    void shouldBuildVolumeSnapshotContentWithPreExistingSource() {
        final VolumeSnapshotContent content = VolumeSnapshotContent.builder()
                .name("preexisting-snapcontent")
                .spec(VolumeSnapshotContentSpec.builder()
                        .volumeSnapshotRef(ObjectReference.builder()
                                .kind("VolumeSnapshot")
                                .name("my-snapshot")
                                .namespace("default")
                                .build())
                        .deletionPolicy("Retain")
                        .driver("ebs.csi.aws.com")
                        .source(VolumeSnapshotContentSource.builder()
                                .snapshotHandle("snap-0123456789abcdef0")
                                .build())
                        .build())
                .build();

        assertThat(content.getSpec().getSource().getSnapshotHandle()).isEqualTo("snap-0123456789abcdef0");
        assertThat(content.getSpec().getSource().getVolumeHandle()).isNull();
    }

    @Test
    void shouldBuildVolumeSnapshotContentWithDeletePolicy() {
        final VolumeSnapshotContent content = VolumeSnapshotContent.builder()
                .name("delete-policy-content")
                .spec(VolumeSnapshotContentSpec.builder()
                        .volumeSnapshotRef(ObjectReference.builder()
                                .kind("VolumeSnapshot")
                                .name("snap")
                                .namespace("default")
                                .build())
                        .deletionPolicy("Delete")
                        .driver("ebs.csi.aws.com")
                        .source(VolumeSnapshotContentSource.builder()
                                .volumeHandle("vol-123")
                                .build())
                        .build())
                .build();

        assertThat(content.getSpec().getDeletionPolicy()).isEqualTo("Delete");
    }

    @Test
    void shouldBuildVolumeSnapshotContentWithRetainPolicy() {
        final VolumeSnapshotContent content = VolumeSnapshotContent.builder()
                .name("retain-policy-content")
                .spec(VolumeSnapshotContentSpec.builder()
                        .volumeSnapshotRef(ObjectReference.builder()
                                .kind("VolumeSnapshot")
                                .name("snap")
                                .namespace("default")
                                .build())
                        .deletionPolicy("Retain")
                        .driver("pd.csi.storage.gke.io")
                        .source(VolumeSnapshotContentSource.builder()
                                .volumeHandle("vol-456")
                                .build())
                        .build())
                .build();

        assertThat(content.getSpec().getDeletionPolicy()).isEqualTo("Retain");
    }

    @Test
    void shouldBuildVolumeSnapshotContentWithVolumeSnapshotClassName() {
        final VolumeSnapshotContent content = VolumeSnapshotContent.builder()
                .name("class-snapcontent")
                .spec(VolumeSnapshotContentSpec.builder()
                        .volumeSnapshotRef(ObjectReference.builder()
                                .kind("VolumeSnapshot")
                                .name("snap")
                                .namespace("default")
                                .build())
                        .deletionPolicy("Delete")
                        .driver("ebs.csi.aws.com")
                        .source(VolumeSnapshotContentSource.builder()
                                .volumeHandle("vol-789")
                                .build())
                        .volumeSnapshotClassName("premium-snapclass")
                        .build())
                .build();

        assertThat(content.getSpec().getVolumeSnapshotClassName()).isEqualTo("premium-snapclass");
    }

    @Test
    void shouldBuildVolumeSnapshotContentWithSourceVolumeMode() {
        final VolumeSnapshotContent content = VolumeSnapshotContent.builder()
                .name("mode-snapcontent")
                .spec(VolumeSnapshotContentSpec.builder()
                        .volumeSnapshotRef(ObjectReference.builder()
                                .kind("VolumeSnapshot")
                                .name("snap")
                                .namespace("default")
                                .build())
                        .deletionPolicy("Delete")
                        .driver("ebs.csi.aws.com")
                        .source(VolumeSnapshotContentSource.builder()
                                .volumeHandle("vol-101")
                                .build())
                        .sourceVolumeMode("Filesystem")
                        .build())
                .build();

        assertThat(content.getSpec().getSourceVolumeMode()).isEqualTo("Filesystem");
    }

    @Test
    void shouldBuildVolumeSnapshotContentWithCompleteVolumeSnapshotRef() {
        final VolumeSnapshotContent content = VolumeSnapshotContent.builder()
                .name("ref-snapcontent")
                .spec(VolumeSnapshotContentSpec.builder()
                        .volumeSnapshotRef(ObjectReference.builder()
                                .apiVersion("snapshot.storage.k8s.io/v1")
                                .kind("VolumeSnapshot")
                                .name("my-snapshot")
                                .namespace("production")
                                .uid("12345678-1234-1234-1234-123456789012")
                                .resourceVersion("12345")
                                .build())
                        .deletionPolicy("Delete")
                        .driver("ebs.csi.aws.com")
                        .source(VolumeSnapshotContentSource.builder()
                                .volumeHandle("vol-xyz")
                                .build())
                        .build())
                .build();

        final ObjectReference ref = content.getSpec().getVolumeSnapshotRef();
        assertThat(ref.getApiVersion()).isEqualTo("snapshot.storage.k8s.io/v1");
        assertThat(ref.getKind()).isEqualTo("VolumeSnapshot");
        assertThat(ref.getName()).isEqualTo("my-snapshot");
        assertThat(ref.getNamespace()).isEqualTo("production");
        assertThat(ref.getUid()).isEqualTo("12345678-1234-1234-1234-123456789012");
        assertThat(ref.getResourceVersion()).isEqualTo("12345");
    }

    @Test
    void shouldBuildVolumeSnapshotContentWithStatus() {
        final VolumeSnapshotContent content = VolumeSnapshotContent.builder()
                .name("status-snapcontent")
                .spec(VolumeSnapshotContentSpec.builder()
                        .volumeSnapshotRef(ObjectReference.builder()
                                .kind("VolumeSnapshot")
                                .name("snap")
                                .namespace("default")
                                .build())
                        .deletionPolicy("Delete")
                        .driver("ebs.csi.aws.com")
                        .source(VolumeSnapshotContentSource.builder()
                                .volumeHandle("vol-status")
                                .build())
                        .build())
                .status(VolumeSnapshotContentStatus.builder()
                        .snapshotHandle("snap-xyz123")
                        .creationTime(1704067200000000000L)
                        .restoreSize(10737418240L)
                        .readyToUse(true)
                        .build())
                .build();

        assertThat(content.getStatus().getSnapshotHandle()).isEqualTo("snap-xyz123");
        assertThat(content.getStatus().getCreationTime()).isEqualTo(1704067200000000000L);
        assertThat(content.getStatus().getRestoreSize()).isEqualTo(10737418240L);
        assertThat(content.getStatus().getReadyToUse()).isTrue();
    }

    @Test
    void shouldBuildVolumeSnapshotContentWithReadyStatus() {
        final VolumeSnapshotContent content = VolumeSnapshotContent.builder()
                .name("ready-snapcontent")
                .spec(VolumeSnapshotContentSpec.builder()
                        .volumeSnapshotRef(ObjectReference.builder()
                                .kind("VolumeSnapshot")
                                .name("snap")
                                .namespace("default")
                                .build())
                        .deletionPolicy("Delete")
                        .driver("ebs.csi.aws.com")
                        .source(VolumeSnapshotContentSource.builder()
                                .volumeHandle("vol-ready")
                                .build())
                        .build())
                .status(VolumeSnapshotContentStatus.builder()
                        .readyToUse(true)
                        .snapshotHandle("snap-ready-123")
                        .build())
                .build();

        assertThat(content.getStatus().getReadyToUse()).isTrue();
        assertThat(content.getStatus().getSnapshotHandle()).isEqualTo("snap-ready-123");
    }

    @Test
    void shouldBuildVolumeSnapshotContentWithError() {
        final VolumeSnapshotContent content = VolumeSnapshotContent.builder()
                .name("error-snapcontent")
                .spec(VolumeSnapshotContentSpec.builder()
                        .volumeSnapshotRef(ObjectReference.builder()
                                .kind("VolumeSnapshot")
                                .name("snap")
                                .namespace("default")
                                .build())
                        .deletionPolicy("Delete")
                        .driver("ebs.csi.aws.com")
                        .source(VolumeSnapshotContentSource.builder()
                                .volumeHandle("vol-error")
                                .build())
                        .build())
                .status(VolumeSnapshotContentStatus.builder()
                        .error(VolumeSnapshotError.builder()
                                .message("Snapshot creation failed: Insufficient permissions")
                                .time("2025-01-01T00:00:00Z")
                                .build())
                        .build())
                .build();

        assertThat(content.getStatus().getError()).isNotNull();
        assertThat(content.getStatus().getError().getMessage()).contains("Insufficient permissions");
        assertThat(content.getStatus().getError().getTime()).isEqualTo("2025-01-01T00:00:00Z");
    }

    @Test
    void shouldBuildVolumeSnapshotContentWithLabels() {
        final VolumeSnapshotContent content = VolumeSnapshotContent.builder()
                .name("labeled-snapcontent")
                .label("environment", "production")
                .label("backup-tier", "gold")
                .spec(VolumeSnapshotContentSpec.builder()
                        .volumeSnapshotRef(ObjectReference.builder()
                                .kind("VolumeSnapshot")
                                .name("snap")
                                .namespace("default")
                                .build())
                        .deletionPolicy("Retain")
                        .driver("ebs.csi.aws.com")
                        .source(VolumeSnapshotContentSource.builder()
                                .snapshotHandle("snap-labeled")
                                .build())
                        .build())
                .build();

        assertThat(content.getMetadata().getLabels())
                .containsEntry("environment", "production")
                .containsEntry("backup-tier", "gold");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> VolumeSnapshotContent.builder()
                .spec(VolumeSnapshotContentSpec.builder()
                        .volumeSnapshotRef(ObjectReference.builder()
                                .kind("VolumeSnapshot")
                                .name("snap")
                                .namespace("default")
                                .build())
                        .deletionPolicy("Delete")
                        .driver("ebs.csi.aws.com")
                        .source(VolumeSnapshotContentSource.builder()
                                .volumeHandle("vol-test")
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VolumeSnapshotContent name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> VolumeSnapshotContent.builder()
                .name("test-snapcontent")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VolumeSnapshotContent spec is required");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final VolumeSnapshotContent content = VolumeSnapshotContent.builder()
                .name("serialize-snapcontent")
                .spec(VolumeSnapshotContentSpec.builder()
                        .volumeSnapshotRef(ObjectReference.builder()
                                .apiVersion("snapshot.storage.k8s.io/v1")
                                .kind("VolumeSnapshot")
                                .name("my-snapshot")
                                .namespace("default")
                                .build())
                        .deletionPolicy("Delete")
                        .driver("ebs.csi.aws.com")
                        .source(VolumeSnapshotContentSource.builder()
                                .volumeHandle("vol-serialize")
                                .build())
                        .build())
                .build();

        final String json = objectMapper.writeValueAsString(content);

        assertThat(json).contains("\"apiVersion\":\"snapshot.storage.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"VolumeSnapshotContent\"");
        assertThat(json).contains("\"name\":\"serialize-snapcontent\"");
        assertThat(json).contains("\"driver\":\"ebs.csi.aws.com\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "apiVersion": "snapshot.storage.k8s.io/v1",
                  "kind": "VolumeSnapshotContent",
                  "metadata": {
                    "name": "deserialize-snapcontent"
                  },
                  "spec": {
                    "volumeSnapshotRef": {
                      "apiVersion": "snapshot.storage.k8s.io/v1",
                      "kind": "VolumeSnapshot",
                      "name": "my-snapshot",
                      "namespace": "default"
                    },
                    "deletionPolicy": "Delete",
                    "driver": "ebs.csi.aws.com",
                    "source": {
                      "volumeHandle": "vol-deserialize"
                    }
                  },
                  "status": {
                    "snapshotHandle": "snap-ready",
                    "creationTime": 1704067200000000000,
                    "restoreSize": 10737418240,
                    "readyToUse": true
                  }
                }
                """;

        final VolumeSnapshotContent content = objectMapper.readValue(json, VolumeSnapshotContent.class);

        assertThat(content.getApiVersion()).isEqualTo("snapshot.storage.k8s.io/v1");
        assertThat(content.getKind()).isEqualTo("VolumeSnapshotContent");
        assertThat(content.getName()).isEqualTo("deserialize-snapcontent");
        assertThat(content.getSpec().getDriver()).isEqualTo("ebs.csi.aws.com");
        assertThat(content.getSpec().getSource().getVolumeHandle()).isEqualTo("vol-deserialize");
        assertThat(content.getStatus().getReadyToUse()).isTrue();
        assertThat(content.getStatus().getCreationTime()).isEqualTo(1704067200000000000L);
        assertThat(content.getStatus().getRestoreSize()).isEqualTo(10737418240L);
    }
}
