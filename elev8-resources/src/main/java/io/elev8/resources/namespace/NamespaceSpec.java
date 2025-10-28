package io.elev8.resources.namespace;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * NamespaceSpec defines the desired state of a Namespace.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NamespaceSpec {
    /**
     * Finalizers is an opaque list of values that must be empty to permanently remove object from storage.
     * More info: https://kubernetes.io/docs/tasks/administer-cluster/namespaces/
     */
    @Singular
    private List<String> finalizers;
}
