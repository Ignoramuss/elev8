package io.elev8.resources.role;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * PolicyRule holds information that describes a policy rule, but does not contain information
 * about who the rule applies to or which namespace the rule applies to.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolicyRule {
    /**
     * APIGroups is the name of the APIGroup that contains the resources.
     * If multiple API groups are specified, any action requested against one of
     * the enumerated resources in any API group will be allowed.
     * "" represents the core API group and "*" represents all API groups.
     */
    @Singular("apiGroup")
    private List<String> apiGroups;

    /**
     * Resources is a list of resources this rule applies to.
     * "*" represents all resources in the specified apiGroups.
     */
    @Singular("resource")
    private List<String> resources;

    /**
     * Verbs is a list of Verbs that apply to ALL the ResourceKinds contained in this rule.
     * "*" represents all verbs. Valid verbs include: get, list, watch, create, update, patch, delete.
     */
    @Singular("verb")
    private List<String> verbs;

    /**
     * ResourceNames is an optional white list of names that the rule applies to.
     * An empty set means that everything is allowed.
     */
    @Singular("resourceName")
    private List<String> resourceNames;
}
