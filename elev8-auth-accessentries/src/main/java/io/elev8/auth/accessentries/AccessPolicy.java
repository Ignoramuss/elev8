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

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String policyArn;
        private List<AccessScope> accessScopes = new ArrayList<>();
        private Instant associatedAt;
        private Instant modifiedAt;

        public Builder policyArn(final String policyArn) {
            this.policyArn = policyArn;
            return this;
        }

        public Builder accessScopes(final List<AccessScope> accessScopes) {
            this.accessScopes = accessScopes != null ? new ArrayList<>(accessScopes) : new ArrayList<>();
            return this;
        }

        public Builder addAccessScope(final AccessScope accessScope) {
            this.accessScopes.add(accessScope);
            return this;
        }

        public Builder associatedAt(final Instant associatedAt) {
            this.associatedAt = associatedAt;
            return this;
        }

        public Builder modifiedAt(final Instant modifiedAt) {
            this.modifiedAt = modifiedAt;
            return this;
        }

        public AccessPolicy build() {
            if (policyArn == null || policyArn.isEmpty()) {
                throw new IllegalArgumentException("Policy ARN is required");
            }
            return new AccessPolicy(policyArn, accessScopes, associatedAt, modifiedAt);
        }
    }
}
