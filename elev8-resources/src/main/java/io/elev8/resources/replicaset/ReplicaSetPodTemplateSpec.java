package io.elev8.resources.replicaset;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.Metadata;
import io.elev8.resources.pod.PodSpec;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

/**
 * ReplicaSetPodTemplateSpec describes the data a pod should have when created from a template.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReplicaSetPodTemplateSpec {
    /**
     * Standard object's metadata.
     * More info: https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#metadata
     */
    private Metadata metadata;

    /**
     * Specification of the desired behavior of the pod.
     * More info: https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#spec-and-status
     */
    @NonNull
    private PodSpec spec;

    /**
     * Custom builder method to add a label to the pod template metadata.
     */
    public static class ReplicaSetPodTemplateSpecBuilder {
        public ReplicaSetPodTemplateSpecBuilder label(final String key, final String value) {
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
