package io.elev8.auth.accessentries;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an EKS Access Entry for cluster authentication.
 * Access Entries provide a modern alternative to the aws-auth ConfigMap.
 */
@Getter
@Setter
@Builder(toBuilder = true)
public final class AccessEntry {

    private final String principalArn;

    @Builder.Default
    private List<String> kubernetesGroups = new ArrayList<>();

    private String username;

    @Builder.Default
    private String type = "STANDARD";

    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    private Instant createdAt;

    private Instant modifiedAt;

    @Builder.Default
    private List<AccessPolicy> accessPolicies = new ArrayList<>();
}
