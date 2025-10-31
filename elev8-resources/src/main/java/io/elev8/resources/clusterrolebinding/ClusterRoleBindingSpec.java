package io.elev8.resources.clusterrolebinding;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.rolebinding.RoleRef;
import io.elev8.resources.rolebinding.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * ClusterRoleBindingSpec defines the desired state of a ClusterRoleBinding.
 * It contains the subjects (who) and the role reference (what permissions).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClusterRoleBindingSpec {
    /**
     * Subjects holds references to the objects the role applies to.
     * This can include users, groups, or service accounts.
     */
    @Singular("subject")
    private List<Subject> subjects;

    /**
     * RoleRef can reference a ClusterRole or a Role.
     * If the RoleRef cannot be resolved, the Authorizer must return an error.
     */
    private RoleRef roleRef;
}
