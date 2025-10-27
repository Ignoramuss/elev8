package io.elev8.auth.accessentries;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Represents an EKS Access Entry for cluster authentication.
 * Access Entries provide a modern alternative to the aws-auth ConfigMap.
 */
@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessEntry {

    String principalArn;

    @Singular
    List<String> kubernetesGroups;

    String username;

    @Builder.Default
    String type = "STANDARD";

    @Singular
    Map<String, String> tags;

    Instant createdAt;

    Instant modifiedAt;

    @Singular
    List<AccessPolicy> accessPolicies;
}
