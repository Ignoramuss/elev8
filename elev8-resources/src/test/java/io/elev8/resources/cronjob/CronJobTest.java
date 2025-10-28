package io.elev8.resources.cronjob;

import io.elev8.resources.job.JobPodTemplateSpec;
import io.elev8.resources.job.JobSpec;
import io.elev8.resources.pod.Container;
import io.elev8.resources.pod.PodSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CronJobTest {

    @Test
    void shouldBuildCronJobWithRequiredFields() {
        final Container container = Container.builder()
                .name("hello")
                .image("busybox:latest")
                .build();

        final CronJobSpec spec = CronJobSpec.builder()
                .schedule("*/5 * * * *")
                .jobTemplate(CronJobJobTemplateSpec.builder()
                        .spec(JobSpec.builder()
                                .template(JobPodTemplateSpec.builder()
                                        .spec(PodSpec.builder()
                                                .container(container)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final CronJob cronJob = CronJob.builder()
                .name("hello-cron")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(cronJob.getApiVersion()).isEqualTo("batch/v1");
        assertThat(cronJob.getKind()).isEqualTo("CronJob");
        assertThat(cronJob.getName()).isEqualTo("hello-cron");
        assertThat(cronJob.getNamespace()).isEqualTo("default");
        assertThat(cronJob.getSpec()).isEqualTo(spec);
        assertThat(cronJob.getSpec().getSchedule()).isEqualTo("*/5 * * * *");
    }

    @Test
    void shouldBuildCronJobWithLabels() {
        final CronJob cronJob = CronJob.builder()
                .name("test-cron")
                .namespace("default")
                .label("app", "batch")
                .label("env", "prod")
                .spec(CronJobSpec.builder()
                        .schedule("0 0 * * *")
                        .jobTemplate(CronJobJobTemplateSpec.builder()
                                .spec(JobSpec.builder()
                                        .template(JobPodTemplateSpec.builder()
                                                .spec(PodSpec.builder()
                                                        .container(Container.builder()
                                                                .name("worker")
                                                                .image("worker:1.0")
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(cronJob.getMetadata().getLabels()).containsEntry("app", "batch");
        assertThat(cronJob.getMetadata().getLabels()).containsEntry("env", "prod");
    }

    @Test
    void shouldBuildCronJobWithConcurrencyPolicy() {
        final CronJobSpec spec = CronJobSpec.builder()
                .schedule("0 * * * *")
                .concurrencyPolicy("Forbid")
                .jobTemplate(CronJobJobTemplateSpec.builder()
                        .spec(JobSpec.builder()
                                .template(JobPodTemplateSpec.builder()
                                        .spec(PodSpec.builder()
                                                .container(Container.builder()
                                                        .name("worker")
                                                        .image("worker:latest")
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final CronJob cronJob = CronJob.builder()
                .name("test-cron")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(cronJob.getSpec().getConcurrencyPolicy()).isEqualTo("Forbid");
    }

    @Test
    void shouldBuildCronJobWithSuspendAndHistoryLimits() {
        final CronJobSpec spec = CronJobSpec.builder()
                .schedule("0 2 * * *")
                .suspend(true)
                .successfulJobsHistoryLimit(5)
                .failedJobsHistoryLimit(3)
                .startingDeadlineSeconds(300L)
                .jobTemplate(CronJobJobTemplateSpec.builder()
                        .spec(JobSpec.builder()
                                .template(JobPodTemplateSpec.builder()
                                        .spec(PodSpec.builder()
                                                .container(Container.builder()
                                                        .name("worker")
                                                        .image("worker:latest")
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final CronJob cronJob = CronJob.builder()
                .name("test-cron")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(cronJob.getSpec().getSuspend()).isTrue();
        assertThat(cronJob.getSpec().getSuccessfulJobsHistoryLimit()).isEqualTo(5);
        assertThat(cronJob.getSpec().getFailedJobsHistoryLimit()).isEqualTo(3);
        assertThat(cronJob.getSpec().getStartingDeadlineSeconds()).isEqualTo(300L);
    }

    @Test
    void shouldBuildCronJobWithDefaultValues() {
        final CronJobSpec spec = CronJobSpec.builder()
                .schedule("0 0 * * *")
                .jobTemplate(CronJobJobTemplateSpec.builder()
                        .spec(JobSpec.builder()
                                .template(JobPodTemplateSpec.builder()
                                        .spec(PodSpec.builder()
                                                .container(Container.builder()
                                                        .name("worker")
                                                        .image("worker:latest")
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final CronJob cronJob = CronJob.builder()
                .name("test-cron")
                .namespace("default")
                .spec(spec)
                .build();

        assertThat(cronJob.getSpec().getConcurrencyPolicy()).isEqualTo("Allow");
        assertThat(cronJob.getSpec().getSuspend()).isFalse();
        assertThat(cronJob.getSpec().getSuccessfulJobsHistoryLimit()).isEqualTo(3);
        assertThat(cronJob.getSpec().getFailedJobsHistoryLimit()).isEqualTo(1);
    }

    @Test
    void shouldBuildCronJobWithJobTemplateLabels() {
        final CronJob cronJob = CronJob.builder()
                .name("test-cron")
                .namespace("default")
                .spec(CronJobSpec.builder()
                        .schedule("0 0 * * *")
                        .jobTemplate(CronJobJobTemplateSpec.builder()
                                .label("app", "batch")
                                .label("version", "v1")
                                .spec(JobSpec.builder()
                                        .template(JobPodTemplateSpec.builder()
                                                .spec(PodSpec.builder()
                                                        .container(Container.builder()
                                                                .name("worker")
                                                                .image("worker:latest")
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(cronJob.getSpec().getJobTemplate().getMetadata().getLabels())
                .containsEntry("app", "batch")
                .containsEntry("version", "v1");
    }

    @Test
    void shouldSerializeToJson() {
        final CronJob cronJob = CronJob.builder()
                .name("hello-cron")
                .namespace("default")
                .spec(CronJobSpec.builder()
                        .schedule("*/1 * * * *")
                        .concurrencyPolicy("Allow")
                        .successfulJobsHistoryLimit(3)
                        .failedJobsHistoryLimit(1)
                        .jobTemplate(CronJobJobTemplateSpec.builder()
                                .spec(JobSpec.builder()
                                        .template(JobPodTemplateSpec.builder()
                                                .spec(PodSpec.builder()
                                                        .container(Container.builder()
                                                                .name("hello")
                                                                .image("busybox:latest")
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        final String json = cronJob.toJson();

        assertThat(json).contains("\"apiVersion\":\"batch/v1\"");
        assertThat(json).contains("\"kind\":\"CronJob\"");
        assertThat(json).contains("\"name\":\"hello-cron\"");
        assertThat(json).contains("\"schedule\":\"*/1 * * * *\"");
        assertThat(json).contains("\"concurrencyPolicy\":\"Allow\"");
        assertThat(json).contains("\"suspend\":false");
        assertThat(json).contains("\"successfulJobsHistoryLimit\":3");
        assertThat(json).contains("\"failedJobsHistoryLimit\":1");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> CronJob.builder()
                .namespace("default")
                .spec(CronJobSpec.builder()
                        .schedule("0 0 * * *")
                        .jobTemplate(CronJobJobTemplateSpec.builder()
                                .spec(JobSpec.builder()
                                        .template(JobPodTemplateSpec.builder()
                                                .spec(PodSpec.builder()
                                                        .container(Container.builder()
                                                                .name("test")
                                                                .image("test")
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CronJob name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> CronJob.builder()
                .name("test-cron")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CronJob spec is required");
    }

    @Test
    void shouldThrowExceptionWhenScheduleIsNull() {
        assertThatThrownBy(() -> CronJob.builder()
                .name("test-cron")
                .namespace("default")
                .spec(CronJobSpec.builder()
                        .jobTemplate(CronJobJobTemplateSpec.builder()
                                .spec(JobSpec.builder()
                                        .template(JobPodTemplateSpec.builder()
                                                .spec(PodSpec.builder()
                                                        .container(Container.builder()
                                                                .name("test")
                                                                .image("test")
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowExceptionWhenScheduleIsEmpty() {
        assertThatThrownBy(() -> CronJob.builder()
                .name("test-cron")
                .namespace("default")
                .spec(CronJobSpec.builder()
                        .schedule("")
                        .jobTemplate(CronJobJobTemplateSpec.builder()
                                .spec(JobSpec.builder()
                                        .template(JobPodTemplateSpec.builder()
                                                .spec(PodSpec.builder()
                                                        .container(Container.builder()
                                                                .name("test")
                                                                .image("test")
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CronJob schedule is required");
    }

    @Test
    void shouldThrowExceptionWhenJobTemplateIsNull() {
        assertThatThrownBy(() -> CronJob.builder()
                .name("test-cron")
                .namespace("default")
                .spec(CronJobSpec.builder()
                        .schedule("0 0 * * *")
                        .build())
                .build())
                .isInstanceOf(NullPointerException.class);
    }
}
