package io.elev8.resources.limitrange;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * LimitRangeItem defines a min/max usage limit for any resource that matches on kind.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LimitRangeItem {
    /**
     * Type of resource that this limit applies to.
     * Valid values: "Container", "Pod", "PersistentVolumeClaim"
     */
    private String type;

    /**
     * Max usage constraints on this kind by resource name.
     * Resources: "cpu", "memory", "storage", "ephemeral-storage"
     * Values are quantities as strings (e.g., "1Gi", "500m", "10")
     */
    @Singular("maxEntry")
    private Map<String, String> max;

    /**
     * Min usage constraints on this kind by resource name.
     * Resources: "cpu", "memory", "storage", "ephemeral-storage"
     * Values are quantities as strings (e.g., "1Gi", "500m", "10")
     */
    @Singular("minEntry")
    private Map<String, String> min;

    /**
     * Default resource requirement limit value by resource name if resource limit is omitted.
     * For containers, this sets the limit when not specified.
     */
    @Singular("defaultEntry")
    @JsonProperty("default")
    private Map<String, String> defaultLimit;

    /**
     * DefaultRequest is the default resource requirement request value by resource name if resource request is omitted.
     * For containers, this sets the request when not specified.
     */
    @Singular("defaultRequestEntry")
    private Map<String, String> defaultRequest;

    /**
     * MaxLimitRequestRatio represents the max burst value for the named resource.
     * It's the maximum ratio between limit and request for a resource.
     */
    @Singular("maxLimitRequestRatioEntry")
    private Map<String, String> maxLimitRequestRatio;
}
