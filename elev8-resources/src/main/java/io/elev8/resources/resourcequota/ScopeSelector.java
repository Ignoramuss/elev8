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
 * ScopeSelector represents the AND of the selectors represented by the scoped-resource selector requirements.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScopeSelector {
    /**
     * A list of scope selector requirements by scope of the resources.
     */
    @Singular("matchExpression")
    private List<ScopedResourceSelectorRequirement> matchExpressions;
}
