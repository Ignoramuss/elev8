package io.elev8.resources;

/**
 * Common interface for Kubernetes resource status conditions.
 * Conditions describe the state of a resource at a particular point in time.
 *
 * <p>Kubernetes resources use conditions to communicate their current state.
 * A condition has a type (e.g., "Ready", "Available"), a status ("True", "False", "Unknown"),
 * and optional reason and message fields for additional context.</p>
 *
 * <p>Example usage with the {@link Conditions} utility class:</p>
 * <pre>
 * List&lt;HorizontalPodAutoscalerCondition&gt; conditions = hpa.getStatus().getConditions();
 * if (Conditions.isTrue(conditions, "ScalingActive")) {
 *     // HPA is actively scaling
 * }
 * </pre>
 *
 * @see Conditions
 */
public interface Condition {

    /**
     * Get the type of the condition.
     * The type is a unique identifier for the condition, such as "Ready", "Available",
     * "ScalingActive", or "Progressing".
     *
     * @return the condition type
     */
    String getType();

    /**
     * Get the status of the condition.
     * Valid values are "True", "False", or "Unknown".
     *
     * @return the condition status
     */
    String getStatus();

    /**
     * Get the last time the condition transitioned from one status to another.
     * Typically in RFC3339 format (e.g., "2024-01-15T10:30:00Z").
     *
     * @return the last transition time, or null if not set
     */
    String getLastTransitionTime();

    /**
     * Get the reason for the condition's last transition.
     * This is typically a one-word CamelCase identifier explaining why the condition changed.
     *
     * @return the reason, or null if not set
     */
    String getReason();

    /**
     * Get a human-readable message with details about the condition.
     * This provides additional context about the condition's state.
     *
     * @return the message, or null if not set
     */
    String getMessage();
}
