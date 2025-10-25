package io.elev8.resources.deployment;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.Metadata;
import io.elev8.resources.pod.PodSpec;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeploymentSpec {

    @Builder.Default Integer replicas = 1;
    @Singular("selector") Map<String, String> selector;
    @NonNull PodTemplateSpec template;
    @Builder.Default String strategy = "RollingUpdate";
    @Builder.Default Integer revisionHistoryLimit = 10;

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
