package io.elev8.resources.resourcequota;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceQuotaTest {

    @Test
    void shouldBuildResourceQuotaWithRequiredFields() {
        final ResourceQuota quota = ResourceQuota.builder()
                .name("compute-quota")
                .namespace("default")
                .spec(ResourceQuotaSpec.builder()
                        .hardLimit("requests.cpu", "10")
                        .hardLimit("requests.memory", "20Gi")
                        .build())
                .build();

        assertThat(quota.getApiVersion()).isEqualTo("v1");
        assertThat(quota.getKind()).isEqualTo("ResourceQuota");
        assertThat(quota.getName()).isEqualTo("compute-quota");
        assertThat(quota.getNamespace()).isEqualTo("default");
    }

    @Test
    void shouldBuildResourceQuotaWithComputeResources() {
        final ResourceQuota quota = ResourceQuota.builder()
                .name("compute-quota")
                .namespace("default")
                .spec(ResourceQuotaSpec.builder()
                        .hardLimit("requests.cpu", "10")
                        .hardLimit("requests.memory", "20Gi")
                        .hardLimit("limits.cpu", "20")
                        .hardLimit("limits.memory", "40Gi")
                        .build())
                .build();

        assertThat(quota.getSpec().getHard()).hasSize(4);
        assertThat(quota.getSpec().getHard()).containsEntry("requests.cpu", "10");
        assertThat(quota.getSpec().getHard()).containsEntry("limits.memory", "40Gi");
    }

    @Test
    void shouldBuildResourceQuotaWithObjectCounts() {
        final ResourceQuota quota = ResourceQuota.builder()
                .name("object-quota")
                .namespace("default")
                .spec(ResourceQuotaSpec.builder()
                        .hardLimit("pods", "50")
                        .hardLimit("services", "10")
                        .hardLimit("secrets", "20")
                        .hardLimit("configmaps", "15")
                        .hardLimit("persistentvolumeclaims", "10")
                        .build())
                .build();

        assertThat(quota.getSpec().getHard()).hasSize(5);
        assertThat(quota.getSpec().getHard()).containsEntry("pods", "50");
    }

    @Test
    void shouldBuildResourceQuotaWithScopes() {
        final ResourceQuota quota = ResourceQuota.builder()
                .name("scoped-quota")
                .namespace("default")
                .spec(ResourceQuotaSpec.builder()
                        .hardLimit("pods", "10")
                        .scope("Terminating")
                        .scope("NotBestEffort")
                        .build())
                .build();

        assertThat(quota.getSpec().getScopes()).hasSize(2);
        assertThat(quota.getSpec().getScopes()).contains("Terminating", "NotBestEffort");
    }

    @Test
    void shouldBuildResourceQuotaWithScopeSelector() {
        final ResourceQuota quota = ResourceQuota.builder()
                .name("priority-quota")
                .namespace("default")
                .spec(ResourceQuotaSpec.builder()
                        .hardLimit("pods", "20")
                        .scopeSelector(ScopeSelector.builder()
                                .matchExpression(ScopedResourceSelectorRequirement.builder()
                                        .scopeName("PriorityClass")
                                        .operator("In")
                                        .value("high")
                                        .value("critical")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(quota.getSpec().getScopeSelector()).isNotNull();
        assertThat(quota.getSpec().getScopeSelector().getMatchExpressions()).hasSize(1);
    }

    @Test
    void shouldBuildResourceQuotaWithStatus() {
        final ResourceQuota quota = ResourceQuota.builder()
                .name("quota-with-status")
                .namespace("default")
                .spec(ResourceQuotaSpec.builder()
                        .hardLimit("requests.cpu", "10")
                        .hardLimit("pods", "50")
                        .build())
                .status(ResourceQuotaStatus.builder()
                        .hardLimit("requests.cpu", "10")
                        .hardLimit("pods", "50")
                        .usedResource("requests.cpu", "5")
                        .usedResource("pods", "25")
                        .build())
                .build();

        assertThat(quota.getStatus()).isNotNull();
        assertThat(quota.getStatus().getHard()).containsEntry("requests.cpu", "10");
        assertThat(quota.getStatus().getUsed()).containsEntry("requests.cpu", "5");
        assertThat(quota.getStatus().getUsed()).containsEntry("pods", "25");
    }

    @Test
    void shouldSerializeToJson() {
        final ResourceQuota quota = ResourceQuota.builder()
                .name("test-quota")
                .namespace("default")
                .spec(ResourceQuotaSpec.builder()
                        .hardLimit("pods", "10")
                        .build())
                .build();

        final String json = quota.toJson();

        assertThat(json).contains("\"apiVersion\":\"v1\"");
        assertThat(json).contains("\"kind\":\"ResourceQuota\"");
        assertThat(json).contains("\"name\":\"test-quota\"");
        assertThat(json).contains("\"pods\":\"10\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> ResourceQuota.builder()
                .namespace("default")
                .spec(ResourceQuotaSpec.builder().hardLimit("pods", "10").build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ResourceQuota name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> ResourceQuota.builder()
                .name("test")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ResourceQuota spec is required");
    }
}
