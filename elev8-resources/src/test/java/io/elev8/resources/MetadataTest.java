package io.elev8.resources;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataTest {

    @Test
    void shouldBuildMetadataWithAllFields() {
        final Instant now = Instant.now();

        final Metadata metadata = Metadata.builder()
                .name("test-resource")
                .namespace("test-namespace")
                .uid("abc-123")
                .resourceVersion("12345")
                .creationTimestamp(now)
                .label("app", "test")
                .label("env", "dev")
                .annotation("description", "test resource")
                .build();

        assertThat(metadata.getName()).isEqualTo("test-resource");
        assertThat(metadata.getNamespace()).isEqualTo("test-namespace");
        assertThat(metadata.getUid()).isEqualTo("abc-123");
        assertThat(metadata.getResourceVersion()).isEqualTo("12345");
        assertThat(metadata.getCreationTimestamp()).isEqualTo(now);
        assertThat(metadata.getLabels()).containsEntry("app", "test");
        assertThat(metadata.getLabels()).containsEntry("env", "dev");
        assertThat(metadata.getAnnotations()).containsEntry("description", "test resource");
    }

    @Test
    void shouldBuildMetadataWithOnlyRequiredFields() {
        final Metadata metadata = Metadata.builder()
                .name("test-resource")
                .build();

        assertThat(metadata.getName()).isEqualTo("test-resource");
        assertThat(metadata.getNamespace()).isNull();
        assertThat(metadata.getLabels()).isNull();
        assertThat(metadata.getAnnotations()).isNull();
    }

    @Test
    void shouldSupportLabelMaps() {
        final Map<String, String> labels = Map.of("app", "test", "version", "1.0");

        final Metadata metadata = Metadata.builder()
                .name("test-resource")
                .labels(labels)
                .build();

        assertThat(metadata.getLabels()).isEqualTo(labels);
    }

    @Test
    void shouldSupportAnnotationMaps() {
        final Map<String, String> annotations = Map.of("key1", "value1", "key2", "value2");

        final Metadata metadata = Metadata.builder()
                .name("test-resource")
                .annotations(annotations)
                .build();

        assertThat(metadata.getAnnotations()).isEqualTo(annotations);
    }

    @Test
    void shouldSupportSetters() {
        final Metadata metadata = new Metadata();
        metadata.setName("new-name");
        metadata.setNamespace("new-namespace");

        assertThat(metadata.getName()).isEqualTo("new-name");
        assertThat(metadata.getNamespace()).isEqualTo("new-namespace");
    }
}
