package io.elev8.resources.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.Metadata;
import io.elev8.resources.pod.PodSpec;
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
public class JobSpec {

    @NonNull JobPodTemplateSpec template;
    Integer completions;
    Integer parallelism;
    @Builder.Default Integer backoffLimit = 6;
    Long activeDeadlineSeconds;
    Integer ttlSecondsAfterFinished;
}
