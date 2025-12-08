package io.elev8.resources;

import java.util.List;
import java.util.Optional;

/**
 * Utility class for working with Kubernetes resource status conditions.
 * Provides null-safe helper methods for common condition operations.
 *
 * <p>Example usage:</p>
 * <pre>
 * List&lt;HorizontalPodAutoscalerCondition&gt; conditions = hpa.getStatus().getConditions();
 *
 * // Check if a condition is true
 * if (Conditions.isTrue(conditions, "ScalingActive")) {
 *     System.out.println("HPA is actively scaling");
 * }
 *
 * // Get the reason for a condition
 * Conditions.getReason(conditions, "AbleToScale")
 *     .ifPresent(reason -&gt; System.out.println("Reason: " + reason));
 *
 * // Find a specific condition
 * Optional&lt;HorizontalPodAutoscalerCondition&gt; condition =
 *     Conditions.findByType(conditions, "ScalingLimited");
 * </pre>
 *
 * @see Condition
 */
public final class Conditions {

    public static final String STATUS_TRUE = "True";
    public static final String STATUS_FALSE = "False";
    public static final String STATUS_UNKNOWN = "Unknown";

    private Conditions() {
    }

    /**
     * Find a condition by type.
     *
     * @param conditions list of conditions (may be null or empty)
     * @param type the condition type to find
     * @param <T> condition type implementing Condition interface
     * @return Optional containing the condition if found, empty otherwise
     */
    public static <T extends Condition> Optional<T> findByType(final List<T> conditions, final String type) {
        if (conditions == null || type == null) {
            return Optional.empty();
        }
        return conditions.stream()
                .filter(c -> c != null && type.equals(c.getType()))
                .findFirst();
    }

    /**
     * Check if a condition of the specified type exists.
     *
     * @param conditions list of conditions (may be null or empty)
     * @param type the condition type to check
     * @param <T> condition type implementing Condition interface
     * @return true if condition exists, false otherwise
     */
    public static <T extends Condition> boolean hasCondition(final List<T> conditions, final String type) {
        return findByType(conditions, type).isPresent();
    }

    /**
     * Check if a condition has status "True".
     *
     * @param conditions list of conditions (may be null or empty)
     * @param type the condition type to check
     * @param <T> condition type implementing Condition interface
     * @return true if condition exists and has status "True", false otherwise
     */
    public static <T extends Condition> boolean isTrue(final List<T> conditions, final String type) {
        return findByType(conditions, type)
                .map(c -> STATUS_TRUE.equals(c.getStatus()))
                .orElse(false);
    }

    /**
     * Check if a condition has status "False".
     *
     * @param conditions list of conditions (may be null or empty)
     * @param type the condition type to check
     * @param <T> condition type implementing Condition interface
     * @return true if condition exists and has status "False", false otherwise
     */
    public static <T extends Condition> boolean isFalse(final List<T> conditions, final String type) {
        return findByType(conditions, type)
                .map(c -> STATUS_FALSE.equals(c.getStatus()))
                .orElse(false);
    }

    /**
     * Check if a condition has status "Unknown".
     *
     * @param conditions list of conditions (may be null or empty)
     * @param type the condition type to check
     * @param <T> condition type implementing Condition interface
     * @return true if condition exists and has status "Unknown", false otherwise
     */
    public static <T extends Condition> boolean isUnknown(final List<T> conditions, final String type) {
        return findByType(conditions, type)
                .map(c -> STATUS_UNKNOWN.equals(c.getStatus()))
                .orElse(false);
    }

    /**
     * Get the status string of a condition.
     *
     * @param conditions list of conditions (may be null or empty)
     * @param type the condition type
     * @param <T> condition type implementing Condition interface
     * @return Optional containing the status if condition exists, empty otherwise
     */
    public static <T extends Condition> Optional<String> getStatus(final List<T> conditions, final String type) {
        return findByType(conditions, type).map(Condition::getStatus);
    }

    /**
     * Get the reason of a condition.
     *
     * @param conditions list of conditions (may be null or empty)
     * @param type the condition type
     * @param <T> condition type implementing Condition interface
     * @return Optional containing the reason if condition exists, empty otherwise
     */
    public static <T extends Condition> Optional<String> getReason(final List<T> conditions, final String type) {
        return findByType(conditions, type).map(Condition::getReason);
    }

    /**
     * Get the message of a condition.
     *
     * @param conditions list of conditions (may be null or empty)
     * @param type the condition type
     * @param <T> condition type implementing Condition interface
     * @return Optional containing the message if condition exists, empty otherwise
     */
    public static <T extends Condition> Optional<String> getMessage(final List<T> conditions, final String type) {
        return findByType(conditions, type).map(Condition::getMessage);
    }

    /**
     * Get the last transition time of a condition.
     *
     * @param conditions list of conditions (may be null or empty)
     * @param type the condition type
     * @param <T> condition type implementing Condition interface
     * @return Optional containing the last transition time if condition exists, empty otherwise
     */
    public static <T extends Condition> Optional<String> getLastTransitionTime(final List<T> conditions,
            final String type) {
        return findByType(conditions, type).map(Condition::getLastTransitionTime);
    }
}
