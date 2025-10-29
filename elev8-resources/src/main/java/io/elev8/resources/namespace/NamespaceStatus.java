package io.elev8.resources.namespace;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * NamespaceStatus is information about the current status of a Namespace.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NamespaceStatus {
    /**
     * Phase is the current lifecycle phase of the namespace.
     * Possible values: Active, Terminating
     */
    private String phase;

    /**
     * Represents the latest available observations of a namespace's current state.
     */
    private List<NamespaceCondition> conditions;

    /**
     * NamespaceCondition contains details about state of namespace.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NamespaceCondition {
        /**
         * Type of namespace controller condition.
         */
        private String type;

        /**
         * Status of the condition (True, False, Unknown).
         */
        private String status;

        /**
         * Last time the condition transitioned from one status to another.
         */
        private String lastTransitionTime;

        /**
         * Unique, one-word, CamelCase reason for the condition's last transition.
         */
        private String reason;

        /**
         * Human-readable message indicating details about last transition.
         */
        private String message;
    }
}
