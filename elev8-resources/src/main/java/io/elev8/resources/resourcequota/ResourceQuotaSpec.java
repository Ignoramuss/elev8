package io.elev8.resources.resourcequota;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

/**
 * ResourceQuotaSpec defines the desired hard limits to enforce for Quota.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceQuotaSpec {
    /**
     * Hard is the set of desired hard limits for each named resource.
     * Examples:
     * - Compute: "requests.cpu", "requests.memory", "limits.cpu", "limits.memory"
     * - Storage: "requests.storage", "persistentvolumeclaims"
     * - Objects: "pods", "services", "secrets", "configmaps"
     */
    @Singular("hardLimit")
    private Map<String, String> hard;

    /**
     * Scopes is a collection of filters that must match each object tracked by a quota.
     * If not specified, the quota matches all objects.
     * Valid values: Terminating, NotTerminating, BestEffort, NotBestEffort,
     *               PriorityClass, CrossNamespacePodAffinity
     */
    @Singular("scope")
    private List<String> scopes;

    /**
     * ScopeSelector is a structured scope restriction using label selectors.
     * Must match each object tracked by a quota.
     * If not specified, the quota matches all objects.
     */
    private ScopeSelector scopeSelector;
}
