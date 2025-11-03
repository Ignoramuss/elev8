package io.elev8.resources.storageclass;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageClassTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildStorageClassWithRequiredFields() {
        final StorageClass sc = StorageClass.builder()
                .name("standard")
                .provisioner("kubernetes.io/aws-ebs")
                .build();

        assertThat(sc.getApiVersion()).isEqualTo("storage.k8s.io/v1");
        assertThat(sc.getKind()).isEqualTo("StorageClass");
        assertThat(sc.getName()).isEqualTo("standard");
        assertThat(sc.getProvisioner()).isEqualTo("kubernetes.io/aws-ebs");
    }

    @Test
    void shouldBuildStorageClassWithAllFields() {
        final StorageClass sc = StorageClass.builder()
                .name("fast-ssd")
                .provisioner("ebs.csi.aws.com")
                .parameter("type", "gp3")
                .parameter("iops", "3000")
                .parameter("encrypted", "true")
                .reclaimPolicy("Delete")
                .volumeBindingMode("WaitForFirstConsumer")
                .allowVolumeExpansion(true)
                .mountOption("debug")
                .build();

        assertThat(sc.getName()).isEqualTo("fast-ssd");
        assertThat(sc.getProvisioner()).isEqualTo("ebs.csi.aws.com");
        assertThat(sc.getParameters()).containsEntry("type", "gp3");
        assertThat(sc.getParameters()).containsEntry("iops", "3000");
        assertThat(sc.getParameters()).containsEntry("encrypted", "true");
        assertThat(sc.getReclaimPolicy()).isEqualTo("Delete");
        assertThat(sc.getVolumeBindingMode()).isEqualTo("WaitForFirstConsumer");
        assertThat(sc.getAllowVolumeExpansion()).isTrue();
        assertThat(sc.getMountOptions()).containsExactly("debug");
    }

    @Test
    void shouldBuildStorageClassWithParameters() {
        final StorageClass sc = StorageClass.builder()
                .name("gp3-storage")
                .provisioner("ebs.csi.aws.com")
                .parameters(Map.of(
                        "type", "gp3",
                        "iops", "3000",
                        "throughput", "125"
                ))
                .build();

        assertThat(sc.getParameters()).hasSize(3);
        assertThat(sc.getParameters()).containsEntry("type", "gp3");
        assertThat(sc.getParameters()).containsEntry("iops", "3000");
        assertThat(sc.getParameters()).containsEntry("throughput", "125");
    }

    @Test
    void shouldBuildStorageClassWithMountOptions() {
        final StorageClass sc = StorageClass.builder()
                .name("nfs-storage")
                .provisioner("nfs.csi.k8s.io")
                .mountOption("hard")
                .mountOption("nfsvers=4.1")
                .mountOption("noatime")
                .build();

        assertThat(sc.getMountOptions()).hasSize(3);
        assertThat(sc.getMountOptions()).containsExactly("hard", "nfsvers=4.1", "noatime");
    }

    @Test
    void shouldBuildStorageClassWithAllowedTopologies() {
        final StorageClass sc = StorageClass.builder()
                .name("topology-aware-storage")
                .provisioner("ebs.csi.aws.com")
                .allowedTopology(TopologySelectorTerm.builder()
                        .matchLabelExpression(TopologySelectorLabelRequirement.builder()
                                .key("topology.kubernetes.io/zone")
                                .value("us-east-1a")
                                .value("us-east-1b")
                                .build())
                        .build())
                .build();

        assertThat(sc.getAllowedTopologies()).hasSize(1);
        final TopologySelectorTerm term = sc.getAllowedTopologies().get(0);
        assertThat(term.getMatchLabelExpressions()).hasSize(1);
        assertThat(term.getMatchLabelExpressions().get(0).getKey()).isEqualTo("topology.kubernetes.io/zone");
        assertThat(term.getMatchLabelExpressions().get(0).getValues()).containsExactly("us-east-1a", "us-east-1b");
    }

    @Test
    void shouldBuildStorageClassWithReclaimPolicyRetain() {
        final StorageClass sc = StorageClass.builder()
                .name("retain-storage")
                .provisioner("ebs.csi.aws.com")
                .reclaimPolicy("Retain")
                .build();

        assertThat(sc.getReclaimPolicy()).isEqualTo("Retain");
    }

    @Test
    void shouldBuildStorageClassWithImmediateBinding() {
        final StorageClass sc = StorageClass.builder()
                .name("immediate-storage")
                .provisioner("ebs.csi.aws.com")
                .volumeBindingMode("Immediate")
                .build();

        assertThat(sc.getVolumeBindingMode()).isEqualTo("Immediate");
    }

    @Test
    void shouldBuildStorageClassWithVolumeExpansion() {
        final StorageClass sc = StorageClass.builder()
                .name("expandable-storage")
                .provisioner("ebs.csi.aws.com")
                .allowVolumeExpansion(true)
                .build();

        assertThat(sc.getAllowVolumeExpansion()).isTrue();
    }

    @Test
    void shouldBuildAWSEBSStorageClass() {
        final StorageClass sc = StorageClass.builder()
                .name("aws-ebs-gp3")
                .provisioner("ebs.csi.aws.com")
                .parameter("type", "gp3")
                .parameter("iops", "3000")
                .parameter("throughput", "125")
                .parameter("encrypted", "true")
                .reclaimPolicy("Delete")
                .volumeBindingMode("WaitForFirstConsumer")
                .allowVolumeExpansion(true)
                .build();

        assertThat(sc.getProvisioner()).isEqualTo("ebs.csi.aws.com");
        assertThat(sc.getParameters().get("type")).isEqualTo("gp3");
        assertThat(sc.getReclaimPolicy()).isEqualTo("Delete");
    }

    @Test
    void shouldBuildGCEPDStorageClass() {
        final StorageClass sc = StorageClass.builder()
                .name("gce-pd-ssd")
                .provisioner("pd.csi.storage.gke.io")
                .parameter("type", "pd-ssd")
                .parameter("replication-type", "regional-pd")
                .reclaimPolicy("Delete")
                .allowVolumeExpansion(true)
                .build();

        assertThat(sc.getProvisioner()).isEqualTo("pd.csi.storage.gke.io");
        assertThat(sc.getParameters().get("type")).isEqualTo("pd-ssd");
    }

    @Test
    void shouldBuildStorageClassWithLabels() {
        final StorageClass sc = StorageClass.builder()
                .name("labeled-storage")
                .label("environment", "production")
                .label("tier", "premium")
                .provisioner("ebs.csi.aws.com")
                .build();

        assertThat(sc.getMetadata().getLabels())
                .containsEntry("environment", "production")
                .containsEntry("tier", "premium");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> StorageClass.builder()
                .provisioner("ebs.csi.aws.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("StorageClass name is required");
    }

    @Test
    void shouldThrowExceptionWhenProvisionerIsNull() {
        assertThatThrownBy(() -> StorageClass.builder()
                .name("test-storage")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("StorageClass provisioner is required");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final StorageClass sc = StorageClass.builder()
                .name("test-storage")
                .provisioner("ebs.csi.aws.com")
                .parameter("type", "gp3")
                .reclaimPolicy("Delete")
                .build();

        final String json = objectMapper.writeValueAsString(sc);

        assertThat(json).contains("\"apiVersion\":\"storage.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"StorageClass\"");
        assertThat(json).contains("\"name\":\"test-storage\"");
        assertThat(json).contains("\"provisioner\":\"ebs.csi.aws.com\"");
        assertThat(json).contains("\"type\":\"gp3\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "apiVersion": "storage.k8s.io/v1",
                  "kind": "StorageClass",
                  "metadata": {
                    "name": "test-storage"
                  },
                  "provisioner": "ebs.csi.aws.com",
                  "parameters": {
                    "type": "gp3"
                  },
                  "reclaimPolicy": "Delete",
                  "allowVolumeExpansion": true
                }
                """;

        final StorageClass sc = objectMapper.readValue(json, StorageClass.class);

        assertThat(sc.getApiVersion()).isEqualTo("storage.k8s.io/v1");
        assertThat(sc.getKind()).isEqualTo("StorageClass");
        assertThat(sc.getName()).isEqualTo("test-storage");
        assertThat(sc.getProvisioner()).isEqualTo("ebs.csi.aws.com");
        assertThat(sc.getParameters().get("type")).isEqualTo("gp3");
        assertThat(sc.getReclaimPolicy()).isEqualTo("Delete");
        assertThat(sc.getAllowVolumeExpansion()).isTrue();
    }
}
