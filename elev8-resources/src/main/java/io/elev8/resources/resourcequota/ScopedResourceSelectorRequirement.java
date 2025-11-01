package io.elev8.resources.resourcequota;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * ScopedResourceSelectorRequirement is a selector that contains values, a scope name,
 * and an operator that relates the scope name and values.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScopedResourceSelectorRequirement {
    /**
     * The name of the scope that the selector applies to.
     * Valid values: Terminating, NotTerminating, BestEffort, NotBestEffort,
     *               PriorityClass, CrossNamespacePodAffinity
     */
    private String scopeName;

    /**
     * Represents a scope's relationship to a set of values.
     * Valid operators: In, NotIn, Exists, DoesNotExist
     */
    private String operator;

    /**
     * An array of string values. If the operator is In or NotIn,
     * the values array must be non-empty. If the operator is Exists or DoesNotExist,
     * the values array must be empty.
     */
    @Singular("value")
    private List<String> values;
}
