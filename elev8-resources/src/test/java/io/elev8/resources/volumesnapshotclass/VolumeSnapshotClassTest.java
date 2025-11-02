package io.elev8.resources.volumesnapshotclass;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VolumeSnapshotClassTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildVolumeSnapshotClassWithRequiredFields() {
        final VolumeSnapshotClass vsc = VolumeSnapshotClass.builder()
                .name("csi-snapclass")
                .driver("ebs.csi.aws.com")
                .deletionPolicy("Delete")
                .build();

        assertThat(vsc.getApiVersion()).isEqualTo("snapshot.storage.k8s.io/v1");
        assertThat(vsc.getKind()).isEqualTo("VolumeSnapshotClass");
        assertThat(vsc.getName()).isEqualTo("csi-snapclass");
        assertThat(vsc.getDriver()).isEqualTo("ebs.csi.aws.com");
        assertThat(vsc.getDeletionPolicy()).isEqualTo("Delete");
    }

    @Test
    void shouldBuildVolumeSnapshotClassWithParameters() {
        final VolumeSnapshotClass vsc = VolumeSnapshotClass.builder()
                .name("encrypted-snapshots")
                .driver("ebs.csi.aws.com")
                .deletionPolicy("Retain")
                .parameter("encrypted", "true")
                .parameter("kmsKeyId", "arn:aws:kms:us-east-1:123456789012:key/12345")
                .build();

        assertThat(vsc.getParameters()).hasSize(2);
        assertThat(vsc.getParameters()).containsEntry("encrypted", "true");
        assertThat(vsc.getParameters()).containsKey("kmsKeyId");
        assertThat(vsc.getDeletionPolicy()).isEqualTo("Retain");
    }

    @Test
    void shouldBuildVolumeSnapshotClassWithParametersMap() {
        final VolumeSnapshotClass vsc = VolumeSnapshotClass.builder()
                .name("param-snapshots")
                .driver("pd.csi.storage.gke.io")
                .deletionPolicy("Delete")
                .parameters(Map.of(
                        "snapshot-type", "archive",
                        "storage-locations", "us-central1"
                ))
                .build();

        assertThat(vsc.getParameters()).hasSize(2);
        assertThat(vsc.getParameters()).containsEntry("snapshot-type", "archive");
    }

    @Test
    void shouldBuildVolumeSnapshotClassWithLabels() {
        final VolumeSnapshotClass vsc = VolumeSnapshotClass.builder()
                .name("labeled-snapclass")
                .label("environment", "production")
                .label("tier", "premium")
                .driver("ebs.csi.aws.com")
                .deletionPolicy("Delete")
                .build();

        assertThat(vsc.getMetadata().getLabels())
                .containsEntry("environment", "production")
                .containsEntry("tier", "premium");
    }

    @Test
    void shouldBuildAWSEBSSnapshotClass() {
        final VolumeSnapshotClass vsc = VolumeSnapshotClass.builder()
                .name("aws-ebs-snapclass")
                .driver("ebs.csi.aws.com")
                .deletionPolicy("Delete")
                .parameter("tagSpecification_1", "Name=Snapshot,Value=Production")
                .build();

        assertThat(vsc.getDriver()).isEqualTo("ebs.csi.aws.com");
        assertThat(vsc.getDeletionPolicy()).isEqualTo("Delete");
    }

    @Test
    void shouldBuildGCEPDSnapshotClass() {
        final VolumeSnapshotClass vsc = VolumeSnapshotClass.builder()
                .name("gce-pd-snapclass")
                .driver("pd.csi.storage.gke.io")
                .deletionPolicy("Retain")
                .parameter("storage-locations", "us-central1")
                .build();

        assertThat(vsc.getDriver()).isEqualTo("pd.csi.storage.gke.io");
        assertThat(vsc.getDeletionPolicy()).isEqualTo("Retain");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> VolumeSnapshotClass.builder()
                .driver("ebs.csi.aws.com")
                .deletionPolicy("Delete")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VolumeSnapshotClass name is required");
    }

    @Test
    void shouldThrowExceptionWhenDriverIsNull() {
        assertThatThrownBy(() -> VolumeSnapshotClass.builder()
                .name("test-snapclass")
                .deletionPolicy("Delete")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VolumeSnapshotClass driver is required");
    }

    @Test
    void shouldThrowExceptionWhenDeletionPolicyIsNull() {
        assertThatThrownBy(() -> VolumeSnapshotClass.builder()
                .name("test-snapclass")
                .driver("ebs.csi.aws.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VolumeSnapshotClass deletionPolicy is required");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final VolumeSnapshotClass vsc = VolumeSnapshotClass.builder()
                .name("test-snapclass")
                .driver("ebs.csi.aws.com")
                .deletionPolicy("Delete")
                .parameter("encrypted", "true")
                .build();

        final String json = objectMapper.writeValueAsString(vsc);

        assertThat(json).contains("\"apiVersion\":\"snapshot.storage.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"VolumeSnapshotClass\"");
        assertThat(json).contains("\"name\":\"test-snapclass\"");
        assertThat(json).contains("\"driver\":\"ebs.csi.aws.com\"");
        assertThat(json).contains("\"deletionPolicy\":\"Delete\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "apiVersion": "snapshot.storage.k8s.io/v1",
                  "kind": "VolumeSnapshotClass",
                  "metadata": {
                    "name": "test-snapclass"
                  },
                  "driver": "ebs.csi.aws.com",
                  "deletionPolicy": "Delete",
                  "parameters": {
                    "encrypted": "true"
                  }
                }
                """;

        final VolumeSnapshotClass vsc = objectMapper.readValue(json, VolumeSnapshotClass.class);

        assertThat(vsc.getApiVersion()).isEqualTo("snapshot.storage.k8s.io/v1");
        assertThat(vsc.getKind()).isEqualTo("VolumeSnapshotClass");
        assertThat(vsc.getName()).isEqualTo("test-snapclass");
        assertThat(vsc.getDriver()).isEqualTo("ebs.csi.aws.com");
        assertThat(vsc.getDeletionPolicy()).isEqualTo("Delete");
        assertThat(vsc.getParameters().get("encrypted")).isEqualTo("true");
    }
}
