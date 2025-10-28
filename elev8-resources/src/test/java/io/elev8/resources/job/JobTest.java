package io.elev8.resources.job;

import io.elev8.resources.pod.Container;
import io.elev8.resources.pod.PodSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobTest {

    @Test
    void shouldBuildJobWithRequiredFields() {
        final Container container = Container.builder()
                .name("batch-processor")
                .image("batch-processor:latest")
                .build();

        final JobSpec spec = JobSpec.builder()
                .template(JobSpec.PodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(container)
                                .build())
                        .build())
                .build();

        final Job job = Job.builder()
                .name("test-job")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(job.getApiVersion()).isEqualTo("batch/v1");
        assertThat(job.getKind()).isEqualTo("Job");
        assertThat(job.getName()).isEqualTo("test-job");
        assertThat(job.getNamespace()).isEqualTo("default");
        assertThat(job.getSpec()).isEqualTo(spec);
    }

    @Test
    void shouldBuildJobWithLabels() {
        final Job job = Job.builder()
                .name("test-job")
                .namespace("default")
                .label("app", "batch")
                .label("env", "prod")
                .spec(JobSpec.builder()
                        .template(JobSpec.PodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("worker")
                                                .image("worker:1.0")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(job.getMetadata().getLabels()).containsEntry("app", "batch");
        assertThat(job.getMetadata().getLabels()).containsEntry("env", "prod");
    }

    @Test
    void shouldBuildJobWithCompletionsAndParallelism() {
        final JobSpec spec = JobSpec.builder()
                .completions(5)
                .parallelism(2)
                .backoffLimit(3)
                .template(JobSpec.PodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("worker")
                                        .image("worker:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        final Job job = Job.builder()
                .name("test-job")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(job.getSpec().getCompletions()).isEqualTo(5);
        assertThat(job.getSpec().getParallelism()).isEqualTo(2);
        assertThat(job.getSpec().getBackoffLimit()).isEqualTo(3);
    }

    @Test
    void shouldBuildJobWithTimeoutAndTTL() {
        final JobSpec spec = JobSpec.builder()
                .activeDeadlineSeconds(3600L)
                .ttlSecondsAfterFinished(100)
                .template(JobSpec.PodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("worker")
                                        .image("worker:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        final Job job = Job.builder()
                .name("test-job")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(job.getSpec().getActiveDeadlineSeconds()).isEqualTo(3600L);
        assertThat(job.getSpec().getTtlSecondsAfterFinished()).isEqualTo(100);
    }

    @Test
    void shouldSerializeToJson() {
        final Job job = Job.builder()
                .name("test-job")
                .namespace("default")
                .spec(JobSpec.builder()
                        .completions(3)
                        .parallelism(1)
                        .template(JobSpec.PodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("pi")
                                                .image("perl:latest")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final String json = job.toJson();

        assertThat(json).contains("\"apiVersion\":\"batch/v1\"");
        assertThat(json).contains("\"kind\":\"Job\"");
        assertThat(json).contains("\"name\":\"test-job\"");
        assertThat(json).contains("\"completions\":3");
        assertThat(json).contains("\"parallelism\":1");
        assertThat(json).contains("\"backoffLimit\":6");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> Job.builder()
                .namespace("default")
                .spec(JobSpec.builder()
                        .template(JobSpec.PodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("test")
                                                .image("test")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Job name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> Job.builder()
                .name("test-job")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Job spec is required");
    }
}
