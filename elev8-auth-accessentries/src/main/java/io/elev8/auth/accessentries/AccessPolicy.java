package io.elev8.auth.accessentries;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.List;

/**
 * Represents an access policy that can be associated with an Access Entry.
 */
@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessPolicy {

    String policyArn;

    @Singular
    List<AccessScope> accessScopes;

    Instant associatedAt;

    Instant modifiedAt;
}
