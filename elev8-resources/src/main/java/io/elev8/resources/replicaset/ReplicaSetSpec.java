package io.elev8.resources.replicaset;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * ReplicaSetSpec defines the desired state of a ReplicaSet.
 * A ReplicaSet ensures that a specified number of pod replicas are running at any given time.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReplicaSetSpec {
    /**
     * Number of desired pods. Defaults to 1.
     */
    @Builder.Default
    private Integer replicas = 1;

    /**
     * Label selector for pods. Existing ReplicaSets whose pods are
     * selected by this will be the ones affected by this deployment.
     */
    @NonNull
    @Singular("selector")
    private Map<String, String> selector;

    /**
     * Template describes the pods that will be created.
     */
    @NonNull
    private ReplicaSetPodTemplateSpec template;

    /**
     * Minimum number of seconds for which a newly created pod should be ready
     * without any of its container crashing, for it to be considered available.
     * Defaults to 0 (pod will be considered available as soon as it is ready).
     */
    private Integer minReadySeconds;
}
