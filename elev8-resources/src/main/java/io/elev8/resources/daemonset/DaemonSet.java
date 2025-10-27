package io.elev8.resources.daemonset;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DaemonSet extends AbstractResource {

    DaemonSetSpec spec;
    DaemonSetStatus status;

    public DaemonSet() {
        super("apps/v1", "DaemonSet", null);
    }

    private DaemonSet(final Builder builder) {
        super("apps/v1", "DaemonSet", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private DaemonSetSpec spec;
        private DaemonSetStatus status;

        public Builder metadata(final Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder name(final String name) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata.setName(name);
            return this;
        }

        public Builder namespace(final String namespace) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata.setNamespace(namespace);
            return this;
        }

        public Builder label(final String key, final String value) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata = Metadata.builder()
                    .name(this.metadata.getName())
                    .namespace(this.metadata.getNamespace())
                    .labels(this.metadata.getLabels())
                    .label(key, value)
                    .build();
            return this;
        }

        public Builder spec(final DaemonSetSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final DaemonSetStatus status) {
            this.status = status;
            return this;
        }

        public DaemonSet build() {
            if (metadata == null || metadata.getName() == null) {
                throw new IllegalArgumentException("DaemonSet name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("DaemonSet spec is required");
            }
            return new DaemonSet(this);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DaemonSetStatus {
        Integer currentNumberScheduled;
        Integer numberMisscheduled;
        Integer desiredNumberScheduled;
        Integer numberReady;
        Long observedGeneration;
        Integer updatedNumberScheduled;
        Integer numberAvailable;
        Integer numberUnavailable;
    }
}
