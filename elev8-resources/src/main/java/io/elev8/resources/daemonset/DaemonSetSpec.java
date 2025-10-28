package io.elev8.resources.daemonset;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.Metadata;
import io.elev8.resources.pod.PodSpec;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DaemonSetSpec {

    @Singular("selector") Map<String, String> selector;
    @NonNull DaemonSetPodTemplateSpec template;
    @Builder.Default String updateStrategy = "RollingUpdate";
    Integer minReadySeconds;
    @Builder.Default Integer revisionHistoryLimit = 10;
}
