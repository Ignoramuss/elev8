package io.elev8.resources.clusterrole;

import io.elev8.resources.role.PolicyRule;
import io.elev8.resources.role.RoleSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClusterRoleTest {

    @Test
    void shouldBuildClusterRoleWithRequiredFields() {
        final ClusterRole clusterRole = ClusterRole.builder()
                .name("pod-reader")
                .build();

        assertThat(clusterRole.getApiVersion()).isEqualTo("rbac.authorization.k8s.io/v1");
        assertThat(clusterRole.getKind()).isEqualTo("ClusterRole");
        assertThat(clusterRole.getName()).isEqualTo("pod-reader");
        assertThat(clusterRole.getNamespace()).isNull();
    }

    @Test
    void shouldBuildClusterRoleWithLabels() {
        final ClusterRole clusterRole = ClusterRole.builder()
                .name("pod-reader")
                .label("app", "backend")
                .label("env", "prod")
                .build();

        assertThat(clusterRole.getMetadata().getLabels()).containsEntry("app", "backend");
        assertThat(clusterRole.getMetadata().getLabels()).containsEntry("env", "prod");
    }

    @Test
    void shouldBuildClusterRoleWithSpec() {
        final RoleSpec spec = RoleSpec.builder()
                .rule(PolicyRule.builder()
                        .apiGroup("")
                        .resource("pods")
                        .verb("get")
                        .verb("list")
                        .build())
                .build();

        final ClusterRole clusterRole = ClusterRole.builder()
                .name("pod-reader")
                .spec(spec)
                .build();

        assertThat(clusterRole.getSpec()).isEqualTo(spec);
        assertThat(clusterRole.getSpec().getRules()).hasSize(1);
    }

    @Test
    void shouldBuildClusterRoleWithMultipleRules() {
        final RoleSpec spec = RoleSpec.builder()
                .rule(PolicyRule.builder()
                        .apiGroup("")
                        .resource("pods")
                        .verb("get")
                        .verb("list")
                        .build())
                .rule(PolicyRule.builder()
                        .apiGroup("")
                        .resource("services")
                        .verb("get")
                        .build())
                .build();

        final ClusterRole clusterRole = ClusterRole.builder()
                .name("multi-resource-reader")
                .spec(spec)
                .build();

        assertThat(clusterRole.getSpec().getRules()).hasSize(2);
        assertThat(clusterRole.getSpec().getRules().get(0).getResources()).contains("pods");
        assertThat(clusterRole.getSpec().getRules().get(1).getResources()).contains("services");
    }

    @Test
    void shouldBuildClusterRoleWithWildcardPermissions() {
        final RoleSpec spec = RoleSpec.builder()
                .rule(PolicyRule.builder()
                        .apiGroup("*")
                        .resource("*")
                        .verb("*")
                        .build())
                .build();

        final ClusterRole clusterRole = ClusterRole.builder()
                .name("cluster-admin")
                .spec(spec)
                .build();

        assertThat(clusterRole.getSpec().getRules()).hasSize(1);
        assertThat(clusterRole.getSpec().getRules().get(0).getApiGroups()).contains("*");
        assertThat(clusterRole.getSpec().getRules().get(0).getResources()).contains("*");
        assertThat(clusterRole.getSpec().getRules().get(0).getVerbs()).contains("*");
    }

    @Test
    void shouldBuildClusterRoleWithResourceNames() {
        final RoleSpec spec = RoleSpec.builder()
                .rule(PolicyRule.builder()
                        .apiGroup("")
                        .resource("secrets")
                        .verb("get")
                        .resourceName("my-secret")
                        .resourceName("another-secret")
                        .build())
                .build();

        final ClusterRole clusterRole = ClusterRole.builder()
                .name("secret-reader")
                .spec(spec)
                .build();

        assertThat(clusterRole.getSpec().getRules().get(0).getResourceNames()).hasSize(2);
        assertThat(clusterRole.getSpec().getRules().get(0).getResourceNames())
                .contains("my-secret", "another-secret");
    }

    @Test
    void shouldSerializeToJson() {
        final ClusterRole clusterRole = ClusterRole.builder()
                .name("pod-reader")
                .spec(RoleSpec.builder()
                        .rule(PolicyRule.builder()
                                .apiGroup("")
                                .resource("pods")
                                .verb("get")
                                .verb("list")
                                .build())
                        .build())
                .build();

        final String json = clusterRole.toJson();

        assertThat(json).contains("\"apiVersion\":\"rbac.authorization.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"ClusterRole\"");
        assertThat(json).contains("\"name\":\"pod-reader\"");
        assertThat(json).contains("\"pods\"");
        assertThat(json).contains("\"get\"");
        assertThat(json).contains("\"list\"");
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() {
        final ClusterRole clusterRole = ClusterRole.builder()
                .name("pod-reader")
                .build();

        final String json = clusterRole.toJson();

        assertThat(json).doesNotContain("\"spec\"");
        assertThat(json).doesNotContain("\"namespace\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> ClusterRole.builder()
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ClusterRole name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> ClusterRole.builder()
                .name("")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ClusterRole name is required");
    }

    @Test
    void shouldNotHaveNamespace() {
        final ClusterRole clusterRole = ClusterRole.builder()
                .name("pod-reader")
                .build();

        assertThat(clusterRole.getNamespace()).isNull();
    }

    @Test
    void shouldAllowClusterRoleWithoutSpec() {
        final ClusterRole clusterRole = ClusterRole.builder()
                .name("pod-reader")
                .build();

        assertThat(clusterRole.getSpec()).isNull();
    }
}
