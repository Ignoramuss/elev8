package io.elev8.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * ResourceRequirements describes the compute resource requirements.
 * Used by PersistentVolumeClaims and other resources to specify resource requests and limits.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceRequirements {
    /**
     * Requests describes the minimum amount of compute resources required.
     * If Requests is omitted for a container, it defaults to Limits if that is explicitly specified,
     * otherwise to an implementation-defined value.
     * Common keys: "cpu", "memory", "storage", "ephemeral-storage".
     */
    @Singular("request")
    private Map<String, String> requests;

    /**
     * Limits describes the maximum amount of compute resources allowed.
     * Common keys: "cpu", "memory", "storage", "ephemeral-storage".
     */
    @Singular("limit")
    private Map<String, String> limits;
}
