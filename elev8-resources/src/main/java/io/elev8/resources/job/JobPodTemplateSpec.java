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
public class JobPodTemplateSpec {
    Metadata metadata;
    @NonNull PodSpec spec;

    public static class JobPodTemplateSpecBuilder {
        public JobPodTemplateSpecBuilder label(final String key, final String value) {
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
