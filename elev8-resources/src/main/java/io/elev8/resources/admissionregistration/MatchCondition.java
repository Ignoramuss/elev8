package io.elev8.resources.admissionregistration;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchCondition {
    private String name;
    private String expression;
}
