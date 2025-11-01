package io.elev8.resources.resourcequota;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * ResourceQuotaStatus defines the observed use of the resource quota.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceQuotaStatus {
    /**
     * Hard is the set of enforced hard limits for each named resource.
     * This is typically a copy of spec.hard.
     */
    @Singular("hardLimit")
    private Map<String, String> hard;

    /**
     * Used is the current observed total usage of the resource in the namespace.
     * Example: {"requests.cpu": "5", "requests.memory": "10Gi", "pods": "25"}
     */
    @Singular("usedResource")
    private Map<String, String> used;
}
