package io.elev8.resources.storageclass;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * TopologySelectorLabelRequirement is a selector that matches a topology label key and values.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopologySelectorLabelRequirement {
    /**
     * The label key that the selector applies to.
     */
    private String key;

    /**
     * An array of string values. One value must match the label to be selected.
     */
    @Singular("value")
    private List<String> values;
}
