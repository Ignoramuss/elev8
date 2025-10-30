package io.elev8.resources.rolebinding;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Subject contains a reference to the object or user identities a role binding applies to.
 * This can either hold a direct API object reference, or a value for non-objects such as user and group names.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Subject {
    /**
     * Kind of object being referenced. Values defined by this API group are "User", "Group", and "ServiceAccount".
     * If the Authorizer does not recognize the kind value, the Authorizer should report an error.
     */
    private String kind;

    /**
     * Name of the object being referenced.
     */
    private String name;

    /**
     * Namespace of the referenced object. If the object kind is non-namespace, such as "User" or "Group",
     * and this value is not empty the Authorizer should report an error.
     */
    private String namespace;

    /**
     * APIGroup holds the API group of the referenced subject.
     * Defaults to "" for ServiceAccount subjects.
     * Defaults to "rbac.authorization.k8s.io" for User and Group subjects.
     */
    private String apiGroup;
}
