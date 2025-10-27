package io.elev8.auth.accessentries;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Represents the scope of an access policy.
 */
@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessScope {

    @Builder.Default
    String type = "cluster";

    @Singular
    List<String> namespaces;
}
