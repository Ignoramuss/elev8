package io.elev8.core.selector;

import io.elev8.core.annotation.Alpha;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Type-safe builder for Kubernetes label selector query strings.
 * Produces selector expressions compatible with the Kubernetes API
 * (e.g., for use in list, watch, and informer operations).
 *
 * <p>Supports equality-based selectors ({@code key=value}, {@code key!=value}),
 * set-based selectors ({@code key in (v1,v2)}, {@code key notin (v1,v2)}),
 * and existence selectors ({@code key}, {@code !key}).
 *
 * <p>Use the {@link Builder} to compose multiple requirements:
 * <pre>{@code
 * LabelSelectorQuery query = LabelSelectorQuery.builder()
 *     .equals("app", "myapp")
 *     .in("env", "staging", "prod")
 *     .exists("managed-by")
 *     .build();
 * }</pre>
 */
@Alpha(since = "0.1.0")
public final class LabelSelectorQuery {

    private final List<Requirement> requirements;

    private LabelSelectorQuery(final List<Requirement> requirements) {
        this.requirements = Collections.unmodifiableList(new ArrayList<>(requirements));
    }

    /**
     * Returns the selector as a comma-separated query string.
     *
     * @return the label selector query string
     */
    public String toQueryString() {
        return requirements.stream()
                .map(Requirement::toExpression)
                .collect(Collectors.joining(","));
    }

    /**
     * Returns an unmodifiable list of all requirements in this selector.
     *
     * @return the requirements
     */
    public List<Requirement> getRequirements() {
        return requirements;
    }

    /**
     * Returns true if this selector has no requirements.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return requirements.isEmpty();
    }

    @Override
    public String toString() {
        return toQueryString();
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    // --- Static convenience factories ---

    /**
     * Creates a selector with a single equality requirement.
     *
     * @param key   the label key
     * @param value the expected value
     * @return a new LabelSelectorQuery
     */
    public static LabelSelectorQuery equals(final String key, final String value) {
        return builder().equals(key, value).build();
    }

    /**
     * Creates a selector with a single not-equals requirement.
     *
     * @param key   the label key
     * @param value the excluded value
     * @return a new LabelSelectorQuery
     */
    public static LabelSelectorQuery notEquals(final String key, final String value) {
        return builder().notEquals(key, value).build();
    }

    /**
     * Creates a selector with a single existence requirement.
     *
     * @param key the label key that must exist
     * @return a new LabelSelectorQuery
     */
    public static LabelSelectorQuery exists(final String key) {
        return builder().exists(key).build();
    }

    /**
     * Creates a selector with a single non-existence requirement.
     *
     * @param key the label key that must not exist
     * @return a new LabelSelectorQuery
     */
    public static LabelSelectorQuery notExists(final String key) {
        return builder().notExists(key).build();
    }

    /**
     * Creates a selector with a single set-membership requirement.
     *
     * @param key    the label key
     * @param values the allowed values
     * @return a new LabelSelectorQuery
     */
    public static LabelSelectorQuery in(final String key, final String... values) {
        return builder().in(key, values).build();
    }

    /**
     * Creates a selector with a single set-exclusion requirement.
     *
     * @param key    the label key
     * @param values the excluded values
     * @return a new LabelSelectorQuery
     */
    public static LabelSelectorQuery notIn(final String key, final String... values) {
        return builder().notIn(key, values).build();
    }

    // --- Inner types ---

    /**
     * Operators supported by Kubernetes label selectors.
     */
    public enum Operator {
        EQUALS,
        NOT_EQUALS,
        IN,
        NOT_IN,
        EXISTS,
        NOT_EXISTS
    }

    /**
     * A single label selector requirement (key, operator, values).
     */
    public static final class Requirement {
        private final String key;
        private final Operator operator;
        private final List<String> values;

        private Requirement(final String key, final Operator operator, final List<String> values) {
            this.key = key;
            this.operator = operator;
            this.values = values != null ? Collections.unmodifiableList(new ArrayList<>(values)) : Collections.emptyList();
        }

        public String getKey() {
            return key;
        }

        public Operator getOperator() {
            return operator;
        }

        public List<String> getValues() {
            return values;
        }

        /**
         * Produces the query string fragment for this requirement.
         *
         * @return the selector expression
         */
        public String toExpression() {
            switch (operator) {
                case EQUALS:
                    return key + "=" + values.get(0);
                case NOT_EQUALS:
                    return key + "!=" + values.get(0);
                case IN:
                    return key + " in (" + String.join(",", values) + ")";
                case NOT_IN:
                    return key + " notin (" + String.join(",", values) + ")";
                case EXISTS:
                    return key;
                case NOT_EXISTS:
                    return "!" + key;
                default:
                    throw new IllegalStateException("Unknown operator: " + operator);
            }
        }
    }

    /**
     * Fluent builder for composing label selector queries.
     */
    public static final class Builder {
        private final List<Requirement> requirements = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds an equality requirement: {@code key=value}.
         *
         * @param key   the label key
         * @param value the expected value
         * @return this builder
         */
        public Builder equals(final String key, final String value) {
            validateKey(key);
            validateValue(value, "value");
            requirements.add(new Requirement(key, Operator.EQUALS, Collections.singletonList(value)));
            return this;
        }

        /**
         * Adds a not-equals requirement: {@code key!=value}.
         *
         * @param key   the label key
         * @param value the excluded value
         * @return this builder
         */
        public Builder notEquals(final String key, final String value) {
            validateKey(key);
            validateValue(value, "value");
            requirements.add(new Requirement(key, Operator.NOT_EQUALS, Collections.singletonList(value)));
            return this;
        }

        /**
         * Adds a set-membership requirement: {@code key in (v1,v2,...)}.
         *
         * @param key    the label key
         * @param values the allowed values (must not be empty)
         * @return this builder
         */
        public Builder in(final String key, final String... values) {
            validateKey(key);
            validateValues(values);
            requirements.add(new Requirement(key, Operator.IN, Arrays.asList(values)));
            return this;
        }

        /**
         * Adds a set-membership requirement: {@code key in (v1,v2,...)}.
         *
         * @param key    the label key
         * @param values the allowed values (must not be empty)
         * @return this builder
         */
        public Builder in(final String key, final List<String> values) {
            validateKey(key);
            validateValuesList(values);
            requirements.add(new Requirement(key, Operator.IN, values));
            return this;
        }

        /**
         * Adds a set-exclusion requirement: {@code key notin (v1,v2,...)}.
         *
         * @param key    the label key
         * @param values the excluded values (must not be empty)
         * @return this builder
         */
        public Builder notIn(final String key, final String... values) {
            validateKey(key);
            validateValues(values);
            requirements.add(new Requirement(key, Operator.NOT_IN, Arrays.asList(values)));
            return this;
        }

        /**
         * Adds a set-exclusion requirement: {@code key notin (v1,v2,...)}.
         *
         * @param key    the label key
         * @param values the excluded values (must not be empty)
         * @return this builder
         */
        public Builder notIn(final String key, final List<String> values) {
            validateKey(key);
            validateValuesList(values);
            requirements.add(new Requirement(key, Operator.NOT_IN, values));
            return this;
        }

        /**
         * Adds an existence requirement: the label key must be present.
         *
         * @param key the label key
         * @return this builder
         */
        public Builder exists(final String key) {
            validateKey(key);
            requirements.add(new Requirement(key, Operator.EXISTS, null));
            return this;
        }

        /**
         * Adds a non-existence requirement: the label key must not be present.
         *
         * @param key the label key
         * @return this builder
         */
        public Builder notExists(final String key) {
            validateKey(key);
            requirements.add(new Requirement(key, Operator.NOT_EXISTS, null));
            return this;
        }

        /**
         * Builds the immutable {@link LabelSelectorQuery}.
         *
         * @return a new LabelSelectorQuery
         */
        public LabelSelectorQuery build() {
            return new LabelSelectorQuery(requirements);
        }

        private static void validateKey(final String key) {
            if (key == null || key.isEmpty()) {
                throw new IllegalArgumentException("Label key must not be null or empty");
            }
        }

        private static void validateValue(final String value, final String paramName) {
            if (value == null) {
                throw new IllegalArgumentException("Label " + paramName + " must not be null");
            }
        }

        private static void validateValues(final String[] values) {
            if (values == null || values.length == 0) {
                throw new IllegalArgumentException("Values must not be null or empty");
            }
            for (final String value : values) {
                if (value == null) {
                    throw new IllegalArgumentException("Values must not contain null elements");
                }
            }
        }

        private static void validateValuesList(final List<String> values) {
            if (values == null || values.isEmpty()) {
                throw new IllegalArgumentException("Values must not be null or empty");
            }
            for (final String value : values) {
                if (value == null) {
                    throw new IllegalArgumentException("Values must not contain null elements");
                }
            }
        }
    }
}
