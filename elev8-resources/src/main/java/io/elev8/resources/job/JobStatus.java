package io.elev8.resources.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobStatus {
    Integer active;
    Integer succeeded;
    Integer failed;
    String startTime;
    String completionTime;
    Long observedGeneration;
}
