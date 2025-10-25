package io.elev8.resources.pod;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PodStatus {

    String phase;
    String reason;
    String message;
    String podIP;
    String hostIP;
    Instant startTime;
    List<ContainerStatus> containerStatuses;

    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContainerStatus {
        String name;
        Boolean ready;
        Integer restartCount;
        String image;
        String imageID;
        String containerID;
    }
}
