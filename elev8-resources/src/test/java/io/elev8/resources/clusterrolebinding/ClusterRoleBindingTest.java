package io.elev8.resources.clusterrolebinding;

import io.elev8.resources.rolebinding.RoleRef;
import io.elev8.resources.rolebinding.Subject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClusterRoleBindingTest {

    @Test
    void shouldBuildClusterRoleBindingWithRequiredFields() {
        final ClusterRoleBinding clusterRoleBinding = ClusterRoleBinding.builder()
                .name("cluster-admin-binding")
                .build();

        assertThat(clusterRoleBinding.getApiVersion()).isEqualTo("rbac.authorization.k8s.io/v1");
        assertThat(clusterRoleBinding.getKind()).isEqualTo("ClusterRoleBinding");
        assertThat(clusterRoleBinding.getName()).isEqualTo("cluster-admin-binding");
        assertThat(clusterRoleBinding.getNamespace()).isNull();
    }

    @Test
    void shouldBuildClusterRoleBindingWithLabels() {
        final ClusterRoleBinding clusterRoleBinding = ClusterRoleBinding.builder()
                .name("cluster-admin-binding")
                .label("app", "backend")
                .label("env", "prod")
                .build();

        assertThat(clusterRoleBinding.getMetadata().getLabels()).containsEntry("app", "backend");
        assertThat(clusterRoleBinding.getMetadata().getLabels()).containsEntry("env", "prod");
    }

    @Test
    void shouldBuildClusterRoleBindingWithSpec() {
        final ClusterRoleBindingSpec spec = ClusterRoleBindingSpec.builder()
                .subject(Subject.builder()
                        .kind("ServiceAccount")
                        .name("my-service-account")
                        .namespace("default")
                        .build())
                .roleRef(RoleRef.builder()
                        .apiGroup("rbac.authorization.k8s.io")
                        .kind("ClusterRole")
                        .name("cluster-admin")
                        .build())
                .build();

        final ClusterRoleBinding clusterRoleBinding = ClusterRoleBinding.builder()
                .name("cluster-admin-binding")
                .spec(spec)
                .build();

        assertThat(clusterRoleBinding.getSpec()).isEqualTo(spec);
        assertThat(clusterRoleBinding.getSpec().getSubjects()).hasSize(1);
        assertThat(clusterRoleBinding.getSpec().getRoleRef()).isNotNull();
    }

    @Test
    void shouldBuildClusterRoleBindingWithMultipleSubjects() {
        final ClusterRoleBindingSpec spec = ClusterRoleBindingSpec.builder()
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
                        .name("system:masters")
                        .apiGroup("rbac.authorization.k8s.io")
                        .build())
                .roleRef(RoleRef.builder()
                        .apiGroup("rbac.authorization.k8s.io")
                        .kind("ClusterRole")
                        .name("cluster-admin")
                        .build())
                .build();

        final ClusterRoleBinding clusterRoleBinding = ClusterRoleBinding.builder()
                .name("admin-binding")
                .spec(spec)
                .build();

        assertThat(clusterRoleBinding.getSpec().getSubjects()).hasSize(3);
        assertThat(clusterRoleBinding.getSpec().getSubjects().get(0).getKind()).isEqualTo("ServiceAccount");
        assertThat(clusterRoleBinding.getSpec().getSubjects().get(1).getKind()).isEqualTo("User");
        assertThat(clusterRoleBinding.getSpec().getSubjects().get(2).getKind()).isEqualTo("Group");
    }

    @Test
    void shouldBuildClusterRoleBindingWithClusterRoleRef() {
        final ClusterRoleBindingSpec spec = ClusterRoleBindingSpec.builder()
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

        final ClusterRoleBinding clusterRoleBinding = ClusterRoleBinding.builder()
                .name("view-binding")
                .spec(spec)
                .build();

        assertThat(clusterRoleBinding.getSpec().getRoleRef().getKind()).isEqualTo("ClusterRole");
        assertThat(clusterRoleBinding.getSpec().getRoleRef().getName()).isEqualTo("view");
    }

    @Test
    void shouldSerializeToJson() {
        final ClusterRoleBinding clusterRoleBinding = ClusterRoleBinding.builder()
                .name("cluster-admin-binding")
                .spec(ClusterRoleBindingSpec.builder()
                        .subject(Subject.builder()
                                .kind("User")
                                .name("admin@example.com")
                                .apiGroup("rbac.authorization.k8s.io")
                                .build())
                        .roleRef(RoleRef.builder()
                                .apiGroup("rbac.authorization.k8s.io")
                                .kind("ClusterRole")
                                .name("cluster-admin")
                                .build())
                        .build())
                .build();

        final String json = clusterRoleBinding.toJson();

        assertThat(json).contains("\"apiVersion\":\"rbac.authorization.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"ClusterRoleBinding\"");
        assertThat(json).contains("\"name\":\"cluster-admin-binding\"");
        assertThat(json).contains("\"User\"");
        assertThat(json).contains("\"admin@example.com\"");
        assertThat(json).contains("\"cluster-admin\"");
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() {
        final ClusterRoleBinding clusterRoleBinding = ClusterRoleBinding.builder()
                .name("cluster-admin-binding")
                .build();

        final String json = clusterRoleBinding.toJson();

        assertThat(json).doesNotContain("\"spec\"");
        assertThat(json).doesNotContain("\"namespace\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> ClusterRoleBinding.builder()
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ClusterRoleBinding name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> ClusterRoleBinding.builder()
                .name("")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ClusterRoleBinding name is required");
    }

    @Test
    void shouldNotHaveNamespace() {
        final ClusterRoleBinding clusterRoleBinding = ClusterRoleBinding.builder()
                .name("cluster-admin-binding")
                .build();

        assertThat(clusterRoleBinding.getNamespace()).isNull();
    }

    @Test
    void shouldAllowClusterRoleBindingWithoutSpec() {
        final ClusterRoleBinding clusterRoleBinding = ClusterRoleBinding.builder()
                .name("cluster-admin-binding")
                .build();

        assertThat(clusterRoleBinding.getSpec()).isNull();
    }
}
