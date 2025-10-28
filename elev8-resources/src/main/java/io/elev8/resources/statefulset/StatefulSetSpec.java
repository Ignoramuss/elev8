package io.elev8.resources.statefulset;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatefulSetSpec {
    @NonNull
    private String serviceName;

    @Builder.Default
    private Integer replicas = 1;

    @Singular("selector")
    private Map<String, String> selector;

    @NonNull
    private StatefulSetPodTemplateSpec template;

    @Singular("volumeClaimTemplate")
    private List<Object> volumeClaimTemplates;

    @Builder.Default
    private String updateStrategy = "RollingUpdate";

    @Builder.Default
    private Integer revisionHistoryLimit = 10;

    @Builder.Default
    private String podManagementPolicy = "OrderedReady";

    private Integer minReadySeconds;
}
