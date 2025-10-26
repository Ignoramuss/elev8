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

    private final String type;

    @Builder.Default
    private List<String> namespaces = new ArrayList<>();

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String type = "cluster";
        private List<String> namespaces = new ArrayList<>();

        public Builder type(final String type) {
            this.type = type;
            return this;
        }

        public Builder namespaces(final List<String> namespaces) {
            this.namespaces = namespaces != null ? new ArrayList<>(namespaces) : new ArrayList<>();
            return this;
        }

        public Builder addNamespace(final String namespace) {
            this.namespaces.add(namespace);
            return this;
        }

        public AccessScope build() {
            if (type == null || type.isEmpty()) {
                throw new IllegalArgumentException("Type is required");
            }
            return new AccessScope(type, namespaces);
        }
    }
}
