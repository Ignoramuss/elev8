package io.elev8.auth.accessentries;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccessScopeTest {

    @Test
    void shouldCreateAccessScopeWithDefaultType() {
        final AccessScope scope = AccessScope.builder().build();

        assertThat(scope.getType()).isEqualTo("cluster");
        assertThat(scope.getNamespaces()).isEmpty();
    }

    @Test
    void shouldCreateAccessScopeWithNamespaceType() {
        final AccessScope scope = AccessScope.builder()
                .type("namespace")
                .namespaces(List.of("default"))
                .build();

        assertThat(scope.getType()).isEqualTo("namespace");
        assertThat(scope.getNamespaces()).containsExactly("default");
    }

    @Test
    void shouldSetMultipleNamespaces() {
        final AccessScope scope = AccessScope.builder()
                .type("namespace")
                .namespaces(List.of("default", "kube-system"))
                .build();

        assertThat(scope.getNamespaces()).containsExactly("default", "kube-system");
    }

    @Test
    void shouldSetNamespacesFromList() {
        final List<String> namespaces = List.of("default", "kube-system");
        final AccessScope scope = AccessScope.builder()
                .type("namespace")
                .namespaces(namespaces)
                .build();

        assertThat(scope.getNamespaces()).containsExactlyElementsOf(namespaces);
    }

    @Test
    void shouldUseDefaultType() {
        final AccessScope scope = AccessScope.builder()
                .namespaces(List.of("default"))
                .build();

        assertThat(scope.getType()).isEqualTo("cluster");
    }
}
