package io.elev8.resources.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Job extends AbstractResource {

    JobSpec spec;
    JobStatus status;

    public Job() {
        super("batch/v1", "Job", null);
    }

    private Job(final Builder builder) {
        super("batch/v1", "Job", builder.metadata);
        this.spec = builder.spec;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private JobSpec spec;
        private JobStatus status;

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

        public Builder spec(final JobSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder status(final JobStatus status) {
            this.status = status;
            return this;
        }

        public Job build() {
            if (metadata == null || metadata.getName() == null) {
                throw new IllegalArgumentException("Job name is required");
            }
            if (spec == null) {
                throw new IllegalArgumentException("Job spec is required");
            }
            return new Job(this);
        }
    }
}
