package io.elev8.resources.csidriver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CSIDriverTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildCSIDriverWithRequiredFields() {
        final CSIDriver driver = CSIDriver.builder()
                .name("ebs.csi.aws.com")
                .build();

        assertThat(driver.getApiVersion()).isEqualTo("storage.k8s.io/v1");
        assertThat(driver.getKind()).isEqualTo("CSIDriver");
        assertThat(driver.getName()).isEqualTo("ebs.csi.aws.com");
    }

    @Test
    void shouldBuildCSIDriverWithAllSpecFields() {
        final CSIDriver driver = CSIDriver.builder()
                .name("full-spec.csi.io")
                .spec(CSIDriverSpec.builder()
                        .attachRequired(false)
                        .podInfoOnMount(true)
                        .fsGroupPolicy("File")
                        .volumeLifecycleMode("Persistent")
                        .volumeLifecycleMode("Ephemeral")
                        .storageCapacity(true)
                        .requiresRepublish(true)
                        .seLinuxMount(true)
                        .nodeAllocatableUpdatePeriodSeconds(30L)
                        .build())
                .build();

        assertThat(driver.getSpec().getAttachRequired()).isFalse();
        assertThat(driver.getSpec().getPodInfoOnMount()).isTrue();
        assertThat(driver.getSpec().getFsGroupPolicy()).isEqualTo("File");
        assertThat(driver.getSpec().getVolumeLifecycleModes()).containsExactly("Persistent", "Ephemeral");
        assertThat(driver.getSpec().getStorageCapacity()).isTrue();
        assertThat(driver.getSpec().getRequiresRepublish()).isTrue();
        assertThat(driver.getSpec().getSeLinuxMount()).isTrue();
        assertThat(driver.getSpec().getNodeAllocatableUpdatePeriodSeconds()).isEqualTo(30L);
    }

    @Test
    void shouldBuildCSIDriverWithAttachRequired() {
        final CSIDriver driver = CSIDriver.builder()
                .name("attach.csi.io")
                .spec(CSIDriverSpec.builder()
                        .attachRequired(true)
                        .build())
                .build();

        assertThat(driver.getSpec().getAttachRequired()).isTrue();
    }

    @Test
    void shouldBuildCSIDriverWithPodInfoOnMount() {
        final CSIDriver driver = CSIDriver.builder()
                .name("podinfo.csi.io")
                .spec(CSIDriverSpec.builder()
                        .podInfoOnMount(true)
                        .build())
                .build();

        assertThat(driver.getSpec().getPodInfoOnMount()).isTrue();
    }

    @Test
    void shouldBuildCSIDriverWithFsGroupPolicyNone() {
        final CSIDriver driver = CSIDriver.builder()
                .name("fsgroup.csi.io")
                .spec(CSIDriverSpec.builder()
                        .fsGroupPolicy("None")
                        .build())
                .build();

        assertThat(driver.getSpec().getFsGroupPolicy()).isEqualTo("None");
    }

    @Test
    void shouldBuildCSIDriverWithFsGroupPolicyFile() {
        final CSIDriver driver = CSIDriver.builder()
                .name("fsgroup-file.csi.io")
                .spec(CSIDriverSpec.builder()
                        .fsGroupPolicy("File")
                        .build())
                .build();

        assertThat(driver.getSpec().getFsGroupPolicy()).isEqualTo("File");
    }

    @Test
    void shouldBuildCSIDriverWithVolumeLifecycleModes() {
        final CSIDriver driver = CSIDriver.builder()
                .name("lifecycle.csi.io")
                .spec(CSIDriverSpec.builder()
                        .volumeLifecycleMode("Persistent")
                        .volumeLifecycleMode("Ephemeral")
                        .build())
                .build();

        assertThat(driver.getSpec().getVolumeLifecycleModes()).containsExactly("Persistent", "Ephemeral");
    }

    @Test
    void shouldBuildCSIDriverWithStorageCapacity() {
        final CSIDriver driver = CSIDriver.builder()
                .name("capacity.csi.io")
                .spec(CSIDriverSpec.builder()
                        .storageCapacity(true)
                        .build())
                .build();

        assertThat(driver.getSpec().getStorageCapacity()).isTrue();
    }

    @Test
    void shouldBuildCSIDriverWithTokenRequests() {
        final CSIDriver driver = CSIDriver.builder()
                .name("token.csi.io")
                .spec(CSIDriverSpec.builder()
                        .tokenRequest(TokenRequest.builder()
                                .audience("gcp")
                                .expirationSeconds(3600L)
                                .build())
                        .tokenRequest(TokenRequest.builder()
                                .audience("")
                                .expirationSeconds(7200L)
                                .build())
                        .build())
                .build();

        assertThat(driver.getSpec().getTokenRequests()).hasSize(2);
        assertThat(driver.getSpec().getTokenRequests().get(0).getAudience()).isEqualTo("gcp");
        assertThat(driver.getSpec().getTokenRequests().get(0).getExpirationSeconds()).isEqualTo(3600L);
        assertThat(driver.getSpec().getTokenRequests().get(1).getAudience()).isEmpty();
    }

    @Test
    void shouldBuildCSIDriverWithRequiresRepublish() {
        final CSIDriver driver = CSIDriver.builder()
                .name("republish.csi.io")
                .spec(CSIDriverSpec.builder()
                        .requiresRepublish(true)
                        .build())
                .build();

        assertThat(driver.getSpec().getRequiresRepublish()).isTrue();
    }

    @Test
    void shouldBuildCSIDriverWithSELinuxMount() {
        final CSIDriver driver = CSIDriver.builder()
                .name("selinux.csi.io")
                .spec(CSIDriverSpec.builder()
                        .seLinuxMount(true)
                        .build())
                .build();

        assertThat(driver.getSpec().getSeLinuxMount()).isTrue();
    }

    @Test
    void shouldBuildCSIDriverWithNodeAllocatableUpdatePeriod() {
        final CSIDriver driver = CSIDriver.builder()
                .name("update-period.csi.io")
                .spec(CSIDriverSpec.builder()
                        .nodeAllocatableUpdatePeriodSeconds(60L)
                        .build())
                .build();

        assertThat(driver.getSpec().getNodeAllocatableUpdatePeriodSeconds()).isEqualTo(60L);
    }

    @Test
    void shouldBuildAWSEBSCSIDriver() {
        final CSIDriver driver = CSIDriver.builder()
                .name("ebs.csi.aws.com")
                .spec(CSIDriverSpec.builder()
                        .attachRequired(true)
                        .podInfoOnMount(false)
                        .volumeLifecycleMode("Persistent")
                        .storageCapacity(true)
                        .fsGroupPolicy("File")
                        .build())
                .build();

        assertThat(driver.getName()).isEqualTo("ebs.csi.aws.com");
        assertThat(driver.getSpec().getAttachRequired()).isTrue();
        assertThat(driver.getSpec().getStorageCapacity()).isTrue();
    }

    @Test
    void shouldBuildCSIDriverWithLabels() {
        final CSIDriver driver = CSIDriver.builder()
                .name("labeled.csi.io")
                .label("environment", "production")
                .label("team", "storage")
                .build();

        assertThat(driver.getMetadata().getLabels())
                .containsEntry("environment", "production")
                .containsEntry("team", "storage");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> CSIDriver.builder()
                .spec(CSIDriverSpec.builder().build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CSIDriver name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsTooLong() {
        final String longName = "a".repeat(64) + ".csi.io";

        assertThatThrownBy(() -> CSIDriver.builder()
                .name(longName)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be 63 characters or less");
    }

    @Test
    void shouldThrowExceptionWhenNameStartsWithDash() {
        assertThatThrownBy(() -> CSIDriver.builder()
                .name("-invalid.csi.io")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must begin and end with alphanumeric");
    }

    @Test
    void shouldThrowExceptionWhenNameEndsWithDash() {
        assertThatThrownBy(() -> CSIDriver.builder()
                .name("invalid.csi.io-")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must begin and end with alphanumeric");
    }

    @Test
    void shouldAllowValidNameWithDotsAndDashes() {
        final CSIDriver driver = CSIDriver.builder()
                .name("valid-driver.with-dots.csi.io")
                .build();

        assertThat(driver.getName()).isEqualTo("valid-driver.with-dots.csi.io");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final CSIDriver driver = CSIDriver.builder()
                .name("test.csi.io")
                .spec(CSIDriverSpec.builder()
                        .attachRequired(false)
                        .podInfoOnMount(true)
                        .build())
                .build();

        final String json = objectMapper.writeValueAsString(driver);

        assertThat(json).contains("\"apiVersion\":\"storage.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"CSIDriver\"");
        assertThat(json).contains("\"name\":\"test.csi.io\"");
        assertThat(json).contains("\"attachRequired\":false");
        assertThat(json).contains("\"podInfoOnMount\":true");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "apiVersion": "storage.k8s.io/v1",
                  "kind": "CSIDriver",
                  "metadata": {
                    "name": "ebs.csi.aws.com"
                  },
                  "spec": {
                    "attachRequired": true,
                    "podInfoOnMount": false,
                    "volumeLifecycleModes": ["Persistent"],
                    "fsGroupPolicy": "File",
                    "storageCapacity": true
                  }
                }
                """;

        final CSIDriver driver = objectMapper.readValue(json, CSIDriver.class);

        assertThat(driver.getApiVersion()).isEqualTo("storage.k8s.io/v1");
        assertThat(driver.getKind()).isEqualTo("CSIDriver");
        assertThat(driver.getName()).isEqualTo("ebs.csi.aws.com");
        assertThat(driver.getSpec().getAttachRequired()).isTrue();
        assertThat(driver.getSpec().getFsGroupPolicy()).isEqualTo("File");
        assertThat(driver.getSpec().getVolumeLifecycleModes()).containsExactly("Persistent");
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() throws Exception {
        final CSIDriver driver = CSIDriver.builder()
                .name("minimal.csi.io")
                .build();

        final String json = objectMapper.writeValueAsString(driver);

        assertThat(json).doesNotContain("\"spec\"");
        assertThat(json).contains("\"name\":\"minimal.csi.io\"");
    }
}
