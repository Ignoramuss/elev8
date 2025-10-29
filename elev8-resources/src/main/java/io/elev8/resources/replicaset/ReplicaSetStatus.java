package io.elev8.resources.replicaset;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ReplicaSetStatus represents the current status of a ReplicaSet.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReplicaSetStatus {
    /**
     * The number of replicas managed by this ReplicaSet.
     */
    private Integer replicas;

    /**
     * The number of ready replicas for this ReplicaSet.
     */
    private Integer readyReplicas;

    /**
     * The number of available replicas (ready for at least minReadySeconds)
     * for this ReplicaSet.
     */
    private Integer availableReplicas;

    /**
     * The number of pods that have labels matching the labels of the pod template of the ReplicaSet.
     */
    private Integer fullyLabeledReplicas;

    /**
     * Reflects the generation of the most recently observed ReplicaSet.
     */
    private Long observedGeneration;
}
