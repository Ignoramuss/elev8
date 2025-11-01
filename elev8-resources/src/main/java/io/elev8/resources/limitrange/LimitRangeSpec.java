package io.elev8.resources.limitrange;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * LimitRangeSpec defines a min/max usage limit for resources that match on kind.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LimitRangeSpec {
    /**
     * Limits is the list of LimitRangeItem objects that are enforced.
     */
    @Singular("limit")
    private List<LimitRangeItem> limits;
}
