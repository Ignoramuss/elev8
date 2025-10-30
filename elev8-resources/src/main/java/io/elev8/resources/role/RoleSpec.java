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
 * RoleSpec defines the desired state of a Role.
 * It contains rules that represent a set of permissions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleSpec {
    /**
     * Rules holds all the PolicyRules for this Role.
     * Each rule defines permissions for specific resources.
     */
    @Singular("rule")
    private List<PolicyRule> rules;
}
