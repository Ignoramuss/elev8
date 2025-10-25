package io.elev8.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceList<T extends KubernetesResource> {

    String apiVersion;
    String kind;
    ListMetadata metadata;
    List<T> items;

    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ListMetadata {
        String resourceVersion;
        String continueToken;
    }
}
