package io.elev8.resources.rolebinding;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleBindingTest {

    @Test
    void shouldBuildRoleBindingWithRequiredFields() {
        final RoleBinding roleBinding = RoleBinding.builder()
                .name("read-pods")
                .namespace("default")
                .build();

        assertThat(roleBinding.getApiVersion()).isEqualTo("rbac.authorization.k8s.io/v1");
        assertThat(roleBinding.getKind()).isEqualTo("RoleBinding");
        assertThat(roleBinding.getName()).isEqualTo("read-pods");
        assertThat(roleBinding.getNamespace()).isEqualTo("default");
    }

    @Test
    void shouldBuildRoleBindingWithLabels() {
        final RoleBinding roleBinding = RoleBinding.builder()
                .name("read-pods")
                .namespace("default")
                .label("app", "backend")
                .label("env", "prod")
                .build();

        assertThat(roleBinding.getMetadata().getLabels()).containsEntry("app", "backend");
        assertThat(roleBinding.getMetadata().getLabels()).containsEntry("env", "prod");
    }

    @Test
    void shouldBuildRoleBindingWithSpec() {
        final RoleBindingSpec spec = RoleBindingSpec.builder()
                .subject(Subject.builder()
                        .kind("ServiceAccount")
                        .name("my-service-account")
                        .namespace("default")
                        .build())
                .roleRef(RoleRef.builder()
                        .apiGroup("rbac.authorization.k8s.io")
                        .kind("Role")
                        .name("pod-reader")
                        .build())
                .build();

        final RoleBinding roleBinding = RoleBinding.builder()
                .name("read-pods")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(roleBinding.getSpec()).isEqualTo(spec);
        assertThat(roleBinding.getSpec().getSubjects()).hasSize(1);
        assertThat(roleBinding.getSpec().getRoleRef()).isNotNull();
    }

    @Test
    void shouldBuildRoleBindingWithMultipleSubjects() {
        final RoleBindingSpec spec = RoleBindingSpec.builder()
                .subject(Subject.builder()
                        .kind("ServiceAccount")
                        .name("service-account-1")
                        .namespace("default")
                        .build())
                .subject(Subject.builder()
                        .kind("User")
                        .name("jane")
                        .apiGroup("rbac.authorization.k8s.io")
                        .build())
                .subject(Subject.builder()
                        .kind("Group")
                        .name("developers")
                        .apiGroup("rbac.authorization.k8s.io")
                        .build())
                .roleRef(RoleRef.builder()
                        .apiGroup("rbac.authorization.k8s.io")
                        .kind("Role")
                        .name("pod-reader")
                        .build())
                .build();

        final RoleBinding roleBinding = RoleBinding.builder()
                .name("read-pods")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(roleBinding.getSpec().getSubjects()).hasSize(3);
        assertThat(roleBinding.getSpec().getSubjects().get(0).getKind()).isEqualTo("ServiceAccount");
        assertThat(roleBinding.getSpec().getSubjects().get(1).getKind()).isEqualTo("User");
        assertThat(roleBinding.getSpec().getSubjects().get(2).getKind()).isEqualTo("Group");
    }

    @Test
    void shouldBuildRoleBindingWithClusterRoleRef() {
        final RoleBindingSpec spec = RoleBindingSpec.builder()
                .subject(Subject.builder()
                        .kind("ServiceAccount")
                        .name("my-service-account")
                        .namespace("default")
                        .build())
                .roleRef(RoleRef.builder()
                        .apiGroup("rbac.authorization.k8s.io")
                        .kind("ClusterRole")
                        .name("view")
                        .build())
                .build();

        final RoleBinding roleBinding = RoleBinding.builder()
                .name("read-pods")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(roleBinding.getSpec().getRoleRef().getKind()).isEqualTo("ClusterRole");
        assertThat(roleBinding.getSpec().getRoleRef().getName()).isEqualTo("view");
    }

    @Test
    void shouldSerializeToJson() {
        final RoleBinding roleBinding = RoleBinding.builder()
                .name("read-pods")
                .namespace("default")
                .spec(RoleBindingSpec.builder()
                        .subject(Subject.builder()
                                .kind("ServiceAccount")
                                .name("my-service-account")
                                .namespace("default")
                                .build())
                        .roleRef(RoleRef.builder()
                                .apiGroup("rbac.authorization.k8s.io")
                                .kind("Role")
                                .name("pod-reader")
                                .build())
                        .build())
                .build();

        final String json = roleBinding.toJson();

        assertThat(json).contains("\"apiVersion\":\"rbac.authorization.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"RoleBinding\"");
        assertThat(json).contains("\"name\":\"read-pods\"");
        assertThat(json).contains("\"ServiceAccount\"");
        assertThat(json).contains("\"my-service-account\"");
        assertThat(json).contains("\"pod-reader\"");
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() {
        final RoleBinding roleBinding = RoleBinding.builder()
                .name("read-pods")
                .namespace("default")
                .build();

        final String json = roleBinding.toJson();

        assertThat(json).doesNotContain("\"spec\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> RoleBinding.builder()
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RoleBinding name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> RoleBinding.builder()
                .name("")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RoleBinding name is required");
    }

    @Test
    void shouldAllowRoleBindingWithoutNamespace() {
        final RoleBinding roleBinding = RoleBinding.builder()
                .name("read-pods")
                .build();

        assertThat(roleBinding.getName()).isEqualTo("read-pods");
        assertThat(roleBinding.getNamespace()).isNull();
    }

    @Test
    void shouldAllowRoleBindingWithoutSpec() {
        final RoleBinding roleBinding = RoleBinding.builder()
                .name("read-pods")
                .namespace("default")
                .build();

        assertThat(roleBinding.getSpec()).isNull();
    }
}
