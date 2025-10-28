package io.elev8.resources.daemonset;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DaemonSetStatus {
    Integer currentNumberScheduled;
    Integer numberMisscheduled;
    Integer desiredNumberScheduled;
    Integer numberReady;
    Long observedGeneration;
    Integer updatedNumberScheduled;
    Integer numberAvailable;
    Integer numberUnavailable;
}
