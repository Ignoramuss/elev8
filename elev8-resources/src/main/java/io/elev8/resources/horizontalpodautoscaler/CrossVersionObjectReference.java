package io.elev8.resources.horizontalpodautoscaler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * CrossVersionObjectReference contains enough information to let you identify the referred resource.
 * This is used to reference the target resource for scaling (Deployment, ReplicaSet, StatefulSet, etc.).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrossVersionObjectReference {
    /**
     * API version of the referent.
     * Example: "apps/v1"
     */
    private String apiVersion;

    /**
     * Kind of the referent.
     * Examples: "Deployment", "ReplicaSet", "StatefulSet", "ReplicationController"
     */
    private String kind;

    /**
     * Name of the referent.
     */
    private String name;
}
