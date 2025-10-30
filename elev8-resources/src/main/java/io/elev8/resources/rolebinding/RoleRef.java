package io.elev8.resources.rolebinding;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * RoleRef contains information that points to the role being used.
 * This can reference a Role in the current namespace or a ClusterRole in the global namespace.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleRef {
    /**
     * APIGroup is the group for the resource being referenced.
     * For Role, this should be "rbac.authorization.k8s.io".
     */
    private String apiGroup;

    /**
     * Kind is the type of resource being referenced.
     * Valid values are "Role" or "ClusterRole".
     */
    private String kind;

    /**
     * Name is the name of the role being referenced.
     */
    private String name;
}
