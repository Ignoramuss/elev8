package io.elev8.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * TypedLocalObjectReference contains enough information to let you locate the
 * typed referenced object inside the same namespace.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TypedLocalObjectReference {
    /**
     * APIGroup is the group for the resource being referenced.
     * If APIGroup is not specified, the specified Kind must be in the core API group.
     * For any other third-party types, APIGroup is required.
     */
    private String apiGroup;

    /**
     * Kind is the type of resource being referenced.
     * Examples: PersistentVolumeClaim, VolumeSnapshot
     */
    private String kind;

    /**
     * Name is the name of resource being referenced.
     */
    private String name;
}
