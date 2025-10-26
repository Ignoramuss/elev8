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

    private String type;

    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    private Instant createdAt;

    private Instant modifiedAt;

    @Builder.Default
    private List<AccessPolicy> accessPolicies = new ArrayList<>();

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String principalArn;
        private List<String> kubernetesGroups = new ArrayList<>();
        private String username;
        private String type = "STANDARD";
        private Map<String, String> tags = new HashMap<>();
        private Instant createdAt;
        private Instant modifiedAt;
        private List<AccessPolicy> accessPolicies = new ArrayList<>();

        public Builder principalArn(final String principalArn) {
            this.principalArn = principalArn;
            return this;
        }

        public Builder kubernetesGroups(final List<String> kubernetesGroups) {
            this.kubernetesGroups = kubernetesGroups != null ? new ArrayList<>(kubernetesGroups) : new ArrayList<>();
            return this;
        }

        public Builder addKubernetesGroup(final String group) {
            this.kubernetesGroups.add(group);
            return this;
        }

        public Builder username(final String username) {
            this.username = username;
            return this;
        }

        public Builder type(final String type) {
            this.type = type;
            return this;
        }

        public Builder tags(final Map<String, String> tags) {
            this.tags = tags != null ? new HashMap<>(tags) : new HashMap<>();
            return this;
        }

        public Builder addTag(final String key, final String value) {
            this.tags.put(key, value);
            return this;
        }

        public Builder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder modifiedAt(final Instant modifiedAt) {
            this.modifiedAt = modifiedAt;
            return this;
        }

        public Builder accessPolicies(final List<AccessPolicy> accessPolicies) {
            this.accessPolicies = accessPolicies != null ? new ArrayList<>(accessPolicies) : new ArrayList<>();
            return this;
        }

        public Builder addAccessPolicy(final AccessPolicy accessPolicy) {
            this.accessPolicies.add(accessPolicy);
            return this;
        }

        public AccessEntry build() {
            if (principalArn == null || principalArn.isEmpty()) {
                throw new IllegalArgumentException("Principal ARN is required");
            }
            return new AccessEntry(principalArn, kubernetesGroups, username, type, tags,
                                 createdAt, modifiedAt, accessPolicies);
        }
    }
}
