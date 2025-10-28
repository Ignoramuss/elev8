package io.elev8.resources.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.Metadata;
import io.elev8.resources.pod.PodSpec;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobSpec {

    @NonNull PodTemplateSpec template;
    Integer completions;
    Integer parallelism;
    @Builder.Default Integer backoffLimit = 6;
    Long activeDeadlineSeconds;
    Integer ttlSecondsAfterFinished;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PodTemplateSpec {
        Metadata metadata;
        @NonNull PodSpec spec;

        public static class PodTemplateSpecBuilder {
            public PodTemplateSpecBuilder label(final String key, final String value) {
                if (this.metadata == null) {
                    this.metadata = Metadata.builder().build();
                }
                this.metadata = Metadata.builder()
                        .labels(this.metadata.getLabels())
                        .label(key, value)
                        .build();
                return this;
            }
        }
    }
}
