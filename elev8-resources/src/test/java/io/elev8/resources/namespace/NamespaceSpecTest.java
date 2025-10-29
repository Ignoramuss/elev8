package io.elev8.resources.namespace;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NamespaceSpecTest {

    @Test
    void shouldBuildEmptySpec() {
        final NamespaceSpec spec = NamespaceSpec.builder().build();

        assertThat(spec).isNotNull();
        assertThat(spec.getFinalizers()).isEmpty();
    }

    @Test
    void shouldBuildSpecWithSingleFinalizer() {
        final NamespaceSpec spec = NamespaceSpec.builder()
                .finalizer("kubernetes")
                .build();

        assertThat(spec.getFinalizers()).hasSize(1);
        assertThat(spec.getFinalizers()).containsExactly("kubernetes");
    }

    @Test
    void shouldBuildSpecWithMultipleFinalizers() {
        final NamespaceSpec spec = NamespaceSpec.builder()
                .finalizer("kubernetes")
                .finalizer("example.com/custom-finalizer")
                .finalizer("another.com/finalizer")
                .build();

        assertThat(spec.getFinalizers()).hasSize(3);
        assertThat(spec.getFinalizers()).containsExactly(
                "kubernetes",
                "example.com/custom-finalizer",
                "another.com/finalizer"
        );
    }

    @Test
    void shouldBuildSpecWithFinalizersList() {
        final List<String> finalizers = Arrays.asList("kubernetes", "custom");

        final NamespaceSpec spec = NamespaceSpec.builder()
                .finalizers(finalizers)
                .build();

        assertThat(spec.getFinalizers()).hasSize(2);
        assertThat(spec.getFinalizers()).containsExactlyInAnyOrder("kubernetes", "custom");
    }

    @Test
    void shouldSupportToBuilder() {
        final NamespaceSpec original = NamespaceSpec.builder()
                .finalizer("kubernetes")
                .build();

        final NamespaceSpec modified = original.toBuilder()
                .finalizer("custom-finalizer")
                .build();

        assertThat(modified.getFinalizers()).hasSize(2);
        assertThat(modified.getFinalizers()).containsExactly("kubernetes", "custom-finalizer");
    }

    @Test
    void shouldClearFinalizers() {
        final NamespaceSpec spec = NamespaceSpec.builder()
                .finalizer("kubernetes")
                .finalizer("custom")
                .build();

        final NamespaceSpec cleared = spec.toBuilder()
                .clearFinalizers()
                .build();

        assertThat(cleared.getFinalizers()).isEmpty();
    }
}
