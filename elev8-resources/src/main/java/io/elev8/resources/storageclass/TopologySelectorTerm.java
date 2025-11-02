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
 * TopologySelectorTerm represents the result of label queries for topology selection.
 * A null or empty topologySelectorTerm matches no objects. The requirements are ANDed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopologySelectorTerm {
    /**
     * A list of topology selector label requirements by labels.
     */
    @Singular("matchLabelExpression")
    private List<TopologySelectorLabelRequirement> matchLabelExpressions;
}
