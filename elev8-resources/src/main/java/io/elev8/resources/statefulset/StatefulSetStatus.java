package io.elev8.resources.statefulset;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatefulSetStatus {
    private Long observedGeneration;
    private Integer replicas;
    private Integer readyReplicas;
    private Integer currentReplicas;
    private Integer updatedReplicas;
    private String currentRevision;
    private String updateRevision;
    private Integer collisionCount;
    private Integer availableReplicas;
}
