package io.elev8.resources.namespace;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NamespaceTest {

    @Test
    void shouldBuildNamespaceWithRequiredFields() {
        final Namespace namespace = Namespace.builder()
                .name("production")
                .build();

        assertThat(namespace.getApiVersion()).isEqualTo("v1");
        assertThat(namespace.getKind()).isEqualTo("Namespace");
        assertThat(namespace.getName()).isEqualTo("production");
    }

    @Test
    void shouldBuildNamespaceWithLabels() {
        final Namespace namespace = Namespace.builder()
                .name("development")
                .label("env", "dev")
                .label("team", "backend")
                .build();

        assertThat(namespace.getMetadata().getLabels()).containsEntry("env", "dev");
        assertThat(namespace.getMetadata().getLabels()).containsEntry("team", "backend");
    }

    @Test
    void shouldBuildNamespaceWithSpec() {
        final NamespaceSpec spec = NamespaceSpec.builder()
                .finalizer("kubernetes")
                .build();

        final Namespace namespace = Namespace.builder()
                .name("test-namespace")
                .spec(spec)
                .build();

        assertThat(namespace.getSpec()).isNotNull();
        assertThat(namespace.getSpec().getFinalizers()).containsExactly("kubernetes");
    }

    @Test
    void shouldBuildNamespaceWithFinalizers() {
        final Namespace namespace = Namespace.builder()
                .name("finalized-namespace")
                .addFinalizer("kubernetes")
                .addFinalizer("example.com/custom-finalizer")
                .build();

        assertThat(namespace.getSpec()).isNotNull();
        assertThat(namespace.getSpec().getFinalizers()).hasSize(2);
        assertThat(namespace.getSpec().getFinalizers()).containsExactly("kubernetes", "example.com/custom-finalizer");
    }

    @Test
    void shouldBuildNamespaceWithFinalizersList() {
        final List<String> finalizers = new ArrayList<>();
        finalizers.add("kubernetes");
        finalizers.add("custom-controller");

        final Namespace namespace = Namespace.builder()
                .name("test-namespace")
                .finalizers(finalizers)
                .build();

        assertThat(namespace.getSpec()).isNotNull();
        assertThat(namespace.getSpec().getFinalizers()).hasSize(2);
        assertThat(namespace.getSpec().getFinalizers()).containsExactlyInAnyOrder("kubernetes", "custom-controller");
    }

    @Test
    void shouldBuildNamespaceWithStatus() {
        final NamespaceStatus status = new NamespaceStatus();
        status.setPhase(Namespace.PHASE_ACTIVE);

        final Namespace namespace = Namespace.builder()
                .name("active-namespace")
                .status(status)
                .build();

        assertThat(namespace.getStatus()).isNotNull();
        assertThat(namespace.getStatus().getPhase()).isEqualTo("Active");
    }

    @Test
    void shouldSerializeToJson() {
        final Namespace namespace = Namespace.builder()
                .name("test-namespace")
                .label("env", "test")
                .addFinalizer("kubernetes")
                .build();

        final String json = namespace.toJson();

        assertThat(json).contains("\"apiVersion\":\"v1\"");
        assertThat(json).contains("\"kind\":\"Namespace\"");
        assertThat(json).contains("\"name\":\"test-namespace\"");
        assertThat(json).contains("\"kubernetes\"");
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() {
        final Namespace namespace = Namespace.builder()
                .name("minimal-namespace")
                .build();

        final String json = namespace.toJson();

        assertThat(json).contains("\"apiVersion\":\"v1\"");
        assertThat(json).contains("\"kind\":\"Namespace\"");
        assertThat(json).contains("\"name\":\"minimal-namespace\"");
        assertThat(json).doesNotContain("\"spec\"");
        assertThat(json).doesNotContain("\"status\"");
    }

    @Test
    void shouldSerializeNamespaceWithCompleteSpec() {
        final Namespace namespace = Namespace.builder()
                .name("complete-namespace")
                .label("purpose", "testing")
                .addFinalizer("kubernetes")
                .addFinalizer("custom-finalizer")
                .build();

        final String json = namespace.toJson();

        assertThat(json).contains("\"finalizers\"");
        assertThat(json).contains("\"kubernetes\"");
        assertThat(json).contains("\"custom-finalizer\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> Namespace.builder()
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Namespace name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> Namespace.builder()
                .name("")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Namespace name is required");
    }

    @Test
    void shouldAllowNamespaceWithoutLabels() {
        final Namespace namespace = Namespace.builder()
                .name("simple-namespace")
                .build();

        assertThat(namespace.getName()).isEqualTo("simple-namespace");
        assertThat(namespace.getMetadata().getLabels()).isEmpty();
    }

    @Test
    void shouldAllowNamespaceWithoutSpec() {
        final Namespace namespace = Namespace.builder()
                .name("no-spec-namespace")
                .build();

        assertThat(namespace.getName()).isEqualTo("no-spec-namespace");
        assertThat(namespace.getSpec()).isNull();
    }

    @Test
    void shouldSupportPhaseConstants() {
        assertThat(Namespace.PHASE_ACTIVE).isEqualTo("Active");
        assertThat(Namespace.PHASE_TERMINATING).isEqualTo("Terminating");
    }

    @Test
    void shouldBuildNamespaceWithStatusAndConditions() {
        final NamespaceStatus.NamespaceCondition condition = new NamespaceStatus.NamespaceCondition();
        condition.setType("NamespaceContentRemaining");
        condition.setStatus("True");
        condition.setReason("SomeResourcesRemain");
        condition.setMessage("Some resources are remaining: pods");

        final NamespaceStatus status = new NamespaceStatus();
        status.setPhase(Namespace.PHASE_TERMINATING);
        status.setConditions(List.of(condition));

        final Namespace namespace = Namespace.builder()
                .name("terminating-namespace")
                .status(status)
                .build();

        assertThat(namespace.getStatus()).isNotNull();
        assertThat(namespace.getStatus().getPhase()).isEqualTo("Terminating");
        assertThat(namespace.getStatus().getConditions()).hasSize(1);
        assertThat(namespace.getStatus().getConditions().get(0).getType()).isEqualTo("NamespaceContentRemaining");
        assertThat(namespace.getStatus().getConditions().get(0).getStatus()).isEqualTo("True");
    }

    @Test
    void shouldAllowMultipleLabels() {
        final Namespace namespace = Namespace.builder()
                .name("multi-label-namespace")
                .label("env", "production")
                .label("team", "platform")
                .label("cost-center", "engineering")
                .build();

        assertThat(namespace.getMetadata().getLabels()).hasSize(3);
        assertThat(namespace.getMetadata().getLabels()).containsEntry("env", "production");
        assertThat(namespace.getMetadata().getLabels()).containsEntry("team", "platform");
        assertThat(namespace.getMetadata().getLabels()).containsEntry("cost-center", "engineering");
    }
}
