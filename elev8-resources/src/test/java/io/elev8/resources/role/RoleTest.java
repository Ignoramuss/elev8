package io.elev8.resources.role;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleTest {

    @Test
    void shouldBuildRoleWithRequiredFields() {
        final Role role = Role.builder()
                .name("pod-reader")
                .namespace("default")
                .build();

        assertThat(role.getApiVersion()).isEqualTo("rbac.authorization.k8s.io/v1");
        assertThat(role.getKind()).isEqualTo("Role");
        assertThat(role.getName()).isEqualTo("pod-reader");
        assertThat(role.getNamespace()).isEqualTo("default");
    }

    @Test
    void shouldBuildRoleWithLabels() {
        final Role role = Role.builder()
                .name("pod-reader")
                .namespace("default")
                .label("app", "backend")
                .label("env", "prod")
                .build();

        assertThat(role.getMetadata().getLabels()).containsEntry("app", "backend");
        assertThat(role.getMetadata().getLabels()).containsEntry("env", "prod");
    }

    @Test
    void shouldBuildRoleWithSpec() {
        final RoleSpec spec = RoleSpec.builder()
                .rule(PolicyRule.builder()
                        .apiGroup("")
                        .resource("pods")
                        .verb("get")
                        .verb("list")
                        .build())
                .build();

        final Role role = Role.builder()
                .name("pod-reader")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(role.getSpec()).isEqualTo(spec);
        assertThat(role.getSpec().getRules()).hasSize(1);
    }

    @Test
    void shouldBuildRoleWithMultipleRules() {
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

        final Role role = Role.builder()
                .name("multi-resource-reader")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(role.getSpec().getRules()).hasSize(2);
        assertThat(role.getSpec().getRules().get(0).getResources()).contains("pods");
        assertThat(role.getSpec().getRules().get(1).getResources()).contains("services");
    }

    @Test
    void shouldBuildRoleWithWildcardPermissions() {
        final RoleSpec spec = RoleSpec.builder()
                .rule(PolicyRule.builder()
                        .apiGroup("*")
                        .resource("*")
                        .verb("*")
                        .build())
                .build();

        final Role role = Role.builder()
                .name("admin")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(role.getSpec().getRules()).hasSize(1);
        assertThat(role.getSpec().getRules().get(0).getApiGroups()).contains("*");
        assertThat(role.getSpec().getRules().get(0).getResources()).contains("*");
        assertThat(role.getSpec().getRules().get(0).getVerbs()).contains("*");
    }

    @Test
    void shouldBuildRoleWithResourceNames() {
        final RoleSpec spec = RoleSpec.builder()
                .rule(PolicyRule.builder()
                        .apiGroup("")
                        .resource("secrets")
                        .verb("get")
                        .resourceName("my-secret")
                        .resourceName("another-secret")
                        .build())
                .build();

        final Role role = Role.builder()
                .name("secret-reader")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(role.getSpec().getRules().get(0).getResourceNames()).hasSize(2);
        assertThat(role.getSpec().getRules().get(0).getResourceNames())
                .contains("my-secret", "another-secret");
    }

    @Test
    void shouldSerializeToJson() {
        final Role role = Role.builder()
                .name("pod-reader")
                .namespace("default")
                .spec(RoleSpec.builder()
                        .rule(PolicyRule.builder()
                                .apiGroup("")
                                .resource("pods")
                                .verb("get")
                                .verb("list")
                                .build())
                        .build())
                .build();

        final String json = role.toJson();

        assertThat(json).contains("\"apiVersion\":\"rbac.authorization.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"Role\"");
        assertThat(json).contains("\"name\":\"pod-reader\"");
        assertThat(json).contains("\"pods\"");
        assertThat(json).contains("\"get\"");
        assertThat(json).contains("\"list\"");
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() {
        final Role role = Role.builder()
                .name("pod-reader")
                .namespace("default")
                .build();

        final String json = role.toJson();

        assertThat(json).doesNotContain("\"spec\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> Role.builder()
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> Role.builder()
                .name("")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role name is required");
    }

    @Test
    void shouldAllowRoleWithoutNamespace() {
        final Role role = Role.builder()
                .name("pod-reader")
                .build();

        assertThat(role.getName()).isEqualTo("pod-reader");
        assertThat(role.getNamespace()).isNull();
    }

    @Test
    void shouldAllowRoleWithoutSpec() {
        final Role role = Role.builder()
                .name("pod-reader")
                .namespace("default")
                .build();

        assertThat(role.getSpec()).isNull();
    }
}
