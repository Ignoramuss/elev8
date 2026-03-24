package io.elev8.core.annotation;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.assertThat;

class ApiStabilityAnnotationTest {

    @Alpha(since = "0.1.0", description = "experimental feature")
    static class AlphaAnnotated {}

    @Beta(since = "0.1.0", description = "nearing stability")
    static class BetaAnnotated {}

    @Stable(since = "0.1.0")
    static class StableAnnotated {}

    @Test
    void alphaAnnotationIsRetainedAtRuntime() {
        final var annotation = AlphaAnnotated.class.getAnnotation(Alpha.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.since()).isEqualTo("0.1.0");
        assertThat(annotation.description()).isEqualTo("experimental feature");
    }

    @Test
    void alphaAnnotationHasCorrectRetentionAndTargets() {
        final var retention = Alpha.class.getAnnotation(Retention.class);
        assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);

        final var target = Alpha.class.getAnnotation(Target.class);
        assertThat(target.value()).containsExactlyInAnyOrder(
                ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE, ElementType.CONSTRUCTOR);

        assertThat(Alpha.class.getAnnotation(Documented.class)).isNotNull();
    }

    @Test
    void alphaAnnotationDefaultValues() {
        @Alpha
        class DefaultAnnotated {}
        final var annotation = DefaultAnnotated.class.getAnnotation(Alpha.class);
        assertThat(annotation.since()).isEmpty();
        assertThat(annotation.description()).isEmpty();
    }

    @Test
    void betaAnnotationIsRetainedAtRuntime() {
        final var annotation = BetaAnnotated.class.getAnnotation(Beta.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.since()).isEqualTo("0.1.0");
        assertThat(annotation.description()).isEqualTo("nearing stability");
    }

    @Test
    void betaAnnotationHasCorrectRetentionAndTargets() {
        final var retention = Beta.class.getAnnotation(Retention.class);
        assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);

        final var target = Beta.class.getAnnotation(Target.class);
        assertThat(target.value()).containsExactlyInAnyOrder(
                ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE, ElementType.CONSTRUCTOR);

        assertThat(Beta.class.getAnnotation(Documented.class)).isNotNull();
    }

    @Test
    void betaAnnotationDefaultValues() {
        @Beta
        class DefaultAnnotated {}
        final var annotation = DefaultAnnotated.class.getAnnotation(Beta.class);
        assertThat(annotation.since()).isEmpty();
        assertThat(annotation.description()).isEmpty();
    }

    @Test
    void stableAnnotationIsRetainedAtRuntime() {
        final var annotation = StableAnnotated.class.getAnnotation(Stable.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.since()).isEqualTo("0.1.0");
    }

    @Test
    void stableAnnotationHasCorrectRetentionAndTargets() {
        final var retention = Stable.class.getAnnotation(Retention.class);
        assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);

        final var target = Stable.class.getAnnotation(Target.class);
        assertThat(target.value()).containsExactlyInAnyOrder(
                ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE, ElementType.CONSTRUCTOR);

        assertThat(Stable.class.getAnnotation(Documented.class)).isNotNull();
    }

    @Test
    void stableAnnotationDefaultValues() {
        @Stable
        class DefaultAnnotated {}
        final var annotation = DefaultAnnotated.class.getAnnotation(Stable.class);
        assertThat(annotation.since()).isEmpty();
    }
}
