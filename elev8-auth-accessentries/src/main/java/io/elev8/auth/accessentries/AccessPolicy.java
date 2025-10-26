package io.elev8.auth.accessentries;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an access policy that can be associated with an Access Entry.
 */
@Getter
@Setter
@Builder(toBuilder = true)
public final class AccessPolicy {

    private final String policyArn;

    @Builder.Default
    private List<AccessScope> accessScopes = new ArrayList<>();

    private Instant associatedAt;

    private Instant modifiedAt;
}
