package io.elev8.resources.pod;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PodSpec {

    @Singular List<Container> containers;
    @Singular List<Container> initContainers;
    @Builder.Default String restartPolicy = "Always";
    String serviceAccountName;
    String nodeName;
    Long terminationGracePeriodSeconds;
}
