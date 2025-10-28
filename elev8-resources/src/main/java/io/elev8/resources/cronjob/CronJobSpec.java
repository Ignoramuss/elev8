package io.elev8.resources.cronjob;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CronJobSpec {

    @NonNull
    private String schedule;

    @NonNull
    private CronJobJobTemplateSpec jobTemplate;

    @Builder.Default
    private String concurrencyPolicy = "Allow";

    @Builder.Default
    private Boolean suspend = false;

    @Builder.Default
    private Integer successfulJobsHistoryLimit = 3;

    @Builder.Default
    private Integer failedJobsHistoryLimit = 1;

    private Long startingDeadlineSeconds;
}
