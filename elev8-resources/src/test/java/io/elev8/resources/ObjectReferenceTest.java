package io.elev8.resources;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectReferenceTest {

    @Test
    void shouldBuildObjectReferenceWithAllFields() {
        final ObjectReference reference = ObjectReference.builder()
                .kind("Pod")
                .namespace("default")
                .name("test-pod")
                .uid("abc-123-def-456")
                .apiVersion("v1")
                .resourceVersion("12345")
                .build();

        assertThat(reference.getKind()).isEqualTo("Pod");
        assertThat(reference.getNamespace()).isEqualTo("default");
        assertThat(reference.getName()).isEqualTo("test-pod");
        assertThat(reference.getUid()).isEqualTo("abc-123-def-456");
        assertThat(reference.getApiVersion()).isEqualTo("v1");
        assertThat(reference.getResourceVersion()).isEqualTo("12345");
    }

    @Test
    void shouldBuildObjectReferenceWithMinimalFields() {
        final ObjectReference reference = ObjectReference.builder()
                .name("test-resource")
                .build();

        assertThat(reference.getName()).isEqualTo("test-resource");
        assertThat(reference.getKind()).isNull();
        assertThat(reference.getNamespace()).isNull();
        assertThat(reference.getUid()).isNull();
        assertThat(reference.getApiVersion()).isNull();
        assertThat(reference.getResourceVersion()).isNull();
    }

    @Test
    void shouldSupportSetters() {
        final ObjectReference reference = ObjectReference.builder()
                .name("old-name")
                .build();

        reference.setName("new-name");
        reference.setKind("Job");
        reference.setNamespace("production");

        assertThat(reference.getName()).isEqualTo("new-name");
        assertThat(reference.getKind()).isEqualTo("Job");
        assertThat(reference.getNamespace()).isEqualTo("production");
    }

    @Test
    void shouldSupportToBuilder() {
        final ObjectReference original = ObjectReference.builder()
                .kind("Pod")
                .name("test-pod")
                .namespace("default")
                .build();

        final ObjectReference modified = original.toBuilder()
                .name("modified-pod")
                .uid("new-uid-123")
                .build();

        assertThat(modified.getKind()).isEqualTo("Pod");
        assertThat(modified.getName()).isEqualTo("modified-pod");
        assertThat(modified.getNamespace()).isEqualTo("default");
        assertThat(modified.getUid()).isEqualTo("new-uid-123");
    }

    @Test
    void shouldBuildObjectReferenceForJobReference() {
        final ObjectReference jobRef = ObjectReference.builder()
                .kind("Job")
                .namespace("batch-jobs")
                .name("backup-job-12345")
                .apiVersion("batch/v1")
                .build();

        assertThat(jobRef.getKind()).isEqualTo("Job");
        assertThat(jobRef.getNamespace()).isEqualTo("batch-jobs");
        assertThat(jobRef.getName()).isEqualTo("backup-job-12345");
        assertThat(jobRef.getApiVersion()).isEqualTo("batch/v1");
    }

    @Test
    void shouldAllowNullValuesForOptionalFields() {
        final ObjectReference reference = ObjectReference.builder()
                .name("test")
                .kind(null)
                .namespace(null)
                .uid(null)
                .apiVersion(null)
                .resourceVersion(null)
                .build();

        assertThat(reference.getName()).isEqualTo("test");
        assertThat(reference.getKind()).isNull();
        assertThat(reference.getNamespace()).isNull();
        assertThat(reference.getUid()).isNull();
        assertThat(reference.getApiVersion()).isNull();
        assertThat(reference.getResourceVersion()).isNull();
    }
}
