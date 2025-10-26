package io.elev8.auth.accessentries;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                .addNamespace("default")
                .build();

        assertThat(scope.getType()).isEqualTo("namespace");
        assertThat(scope.getNamespaces()).containsExactly("default");
    }

    @Test
    void shouldAddMultipleNamespaces() {
        final AccessScope scope = AccessScope.builder()
                .type("namespace")
                .addNamespace("default")
                .addNamespace("kube-system")
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
    void shouldThrowExceptionWhenTypeIsNull() {
        assertThatThrownBy(() -> AccessScope.builder()
                .type(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Type is required");
    }

    @Test
    void shouldThrowExceptionWhenTypeIsEmpty() {
        assertThatThrownBy(() -> AccessScope.builder()
                .type("")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Type is required");
    }
}
