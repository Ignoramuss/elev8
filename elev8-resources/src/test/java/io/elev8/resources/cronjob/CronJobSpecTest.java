package io.elev8.resources.cronjob;

import io.elev8.resources.job.JobPodTemplateSpec;
import io.elev8.resources.job.JobSpec;
import io.elev8.resources.pod.Container;
import io.elev8.resources.pod.PodSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CronJobSpecTest {

    @Test
    void shouldBuildCronJobSpecWithRequiredFields() {
        final CronJobSpec spec = CronJobSpec.builder()
                .schedule("*/5 * * * *")
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
                .build();

        assertThat(spec.getSchedule()).isEqualTo("*/5 * * * *");
        assertThat(spec.getJobTemplate()).isNotNull();
        assertThat(spec.getConcurrencyPolicy()).isEqualTo("Allow");
        assertThat(spec.getSuspend()).isFalse();
        assertThat(spec.getSuccessfulJobsHistoryLimit()).isEqualTo(3);
        assertThat(spec.getFailedJobsHistoryLimit()).isEqualTo(1);
    }

    @Test
    void shouldBuildCronJobSpecWithAllFields() {
        final CronJobSpec spec = CronJobSpec.builder()
                .schedule("0 2 * * *")
                .jobTemplate(CronJobJobTemplateSpec.builder()
                        .spec(JobSpec.builder()
                                .completions(1)
                                .template(JobPodTemplateSpec.builder()
                                        .spec(PodSpec.builder()
                                                .container(Container.builder()
                                                        .name("backup")
                                                        .image("backup:latest")
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .concurrencyPolicy("Forbid")
                .suspend(true)
                .successfulJobsHistoryLimit(5)
                .failedJobsHistoryLimit(3)
                .startingDeadlineSeconds(300L)
                .build();

        assertThat(spec.getSchedule()).isEqualTo("0 2 * * *");
        assertThat(spec.getConcurrencyPolicy()).isEqualTo("Forbid");
        assertThat(spec.getSuspend()).isTrue();
        assertThat(spec.getSuccessfulJobsHistoryLimit()).isEqualTo(5);
        assertThat(spec.getFailedJobsHistoryLimit()).isEqualTo(3);
        assertThat(spec.getStartingDeadlineSeconds()).isEqualTo(300L);
    }

    @Test
    void shouldSupportDifferentConcurrencyPolicies() {
        final CronJobJobTemplateSpec template = CronJobJobTemplateSpec.builder()
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
                .build();

        final CronJobSpec allowSpec = CronJobSpec.builder()
                .schedule("* * * * *")
                .jobTemplate(template)
                .concurrencyPolicy("Allow")
                .build();

        final CronJobSpec forbidSpec = CronJobSpec.builder()
                .schedule("* * * * *")
                .jobTemplate(template)
                .concurrencyPolicy("Forbid")
                .build();

        final CronJobSpec replaceSpec = CronJobSpec.builder()
                .schedule("* * * * *")
                .jobTemplate(template)
                .concurrencyPolicy("Replace")
                .build();

        assertThat(allowSpec.getConcurrencyPolicy()).isEqualTo("Allow");
        assertThat(forbidSpec.getConcurrencyPolicy()).isEqualTo("Forbid");
        assertThat(replaceSpec.getConcurrencyPolicy()).isEqualTo("Replace");
    }

    @Test
    void shouldThrowExceptionWhenScheduleIsNull() {
        assertThatThrownBy(() -> CronJobSpec.builder()
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
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowExceptionWhenJobTemplateIsNull() {
        assertThatThrownBy(() -> CronJobSpec.builder()
                .schedule("0 0 * * *")
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldBuildCronJobJobTemplateSpec() {
        final JobSpec jobSpec = JobSpec.builder()
                .completions(1)
                .template(JobPodTemplateSpec.builder()
                        .spec(PodSpec.builder()
                                .container(Container.builder()
                                        .name("worker")
                                        .image("worker:latest")
                                        .build())
                                .build())
                        .build())
                .build();

        final CronJobJobTemplateSpec template = CronJobJobTemplateSpec.builder()
                .label("app", "batch")
                .label("version", "1.0")
                .spec(jobSpec)
                .build();

        assertThat(template.getMetadata().getLabels()).containsEntry("app", "batch");
        assertThat(template.getMetadata().getLabels()).containsEntry("version", "1.0");
        assertThat(template.getSpec()).isEqualTo(jobSpec);
    }
}
