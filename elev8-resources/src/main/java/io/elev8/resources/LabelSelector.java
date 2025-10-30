package io.elev8.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

/**
 * LabelSelector is a label query over a set of resources. The result of matchLabels and
 * matchExpressions are ANDed. An empty label selector matches all objects. A null
 * label selector matches no objects.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LabelSelector {
    /**
     * matchLabels is a map of {key,value} pairs. A single {key,value} in the matchLabels
     * map is equivalent to an element of matchExpressions, whose key field is "key", the
     * operator is "In", and the values array contains only "value". The requirements are ANDed.
     */
    @Singular("matchLabel")
    private Map<String, String> matchLabels;

    /**
     * matchExpressions is a list of label selector requirements. The requirements are ANDed.
     */
    @Singular("matchExpression")
    private List<LabelSelectorRequirement> matchExpressions;

    /**
     * LabelSelectorRequirement is a selector that contains values, a key, and an operator that
     * relates the key and values.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LabelSelectorRequirement {
        /**
         * key is the label key that the selector applies to.
         */
        private String key;

        /**
         * operator represents a key's relationship to a set of values.
         * Valid operators are In, NotIn, Exists and DoesNotExist.
         */
        private String operator;

        /**
         * values is an array of string values. If the operator is In or NotIn,
         * the values array must be non-empty. If the operator is Exists or DoesNotExist,
         * the values array must be empty.
         */
        @Singular("value")
        private List<String> values;
    }
}
