package io.elev8.resources.admissionregistration;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MutatingWebhookConfiguration extends AbstractResource {

    private List<MutatingWebhook> webhooks;

    public MutatingWebhookConfiguration() {
        super("admissionregistration.k8s.io/v1", "MutatingWebhookConfiguration", null);
    }

    private MutatingWebhookConfiguration(final Builder builder) {
        super("admissionregistration.k8s.io/v1", "MutatingWebhookConfiguration", builder.metadata);
        this.webhooks = builder.webhooks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private List<MutatingWebhook> webhooks;

        private Builder() {
        }

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

        public Builder label(final String key, final String value) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata = Metadata.builder()
                    .name(this.metadata.getName())
                    .labels(this.metadata.getLabels())
                    .annotations(this.metadata.getAnnotations())
                    .label(key, value)
                    .build();
            return this;
        }

        public Builder annotation(final String key, final String value) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata = Metadata.builder()
                    .name(this.metadata.getName())
                    .labels(this.metadata.getLabels())
                    .annotations(this.metadata.getAnnotations())
                    .annotation(key, value)
                    .build();
            return this;
        }

        public Builder webhooks(final List<MutatingWebhook> webhooks) {
            this.webhooks = webhooks;
            return this;
        }

        public Builder webhook(final MutatingWebhook webhook) {
            if (this.webhooks == null) {
                this.webhooks = new ArrayList<>();
            }
            this.webhooks.add(webhook);
            return this;
        }

        public MutatingWebhookConfiguration build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("MutatingWebhookConfiguration name is required");
            }
            return new MutatingWebhookConfiguration(this);
        }
    }
}
