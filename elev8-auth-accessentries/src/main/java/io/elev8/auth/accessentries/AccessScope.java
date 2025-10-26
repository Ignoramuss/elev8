package io.elev8.auth.accessentries;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the scope of an access policy.
 */
@Getter
@Setter
@Builder(toBuilder = true)
public final class AccessScope {

    @Builder.Default
    private final String type = "cluster";

    @Builder.Default
    private List<String> namespaces = new ArrayList<>();
}
