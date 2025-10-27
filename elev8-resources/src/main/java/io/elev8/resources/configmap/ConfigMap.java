package io.elev8.resources.configmap;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * ConfigMap holds configuration data for pods to consume.
 * ConfigMaps allow you to decouple configuration artifacts from image content.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigMap extends AbstractResource {

    private Map<String, String> data;
    private Map<String, String> binaryData;
    private Boolean immutable;

    public ConfigMap() {
        super("v1", "ConfigMap", null);
    }

    private ConfigMap(final Builder builder) {
        super("v1", "ConfigMap", builder.metadata);
        this.data = builder.data;
        this.binaryData = builder.binaryData;
        this.immutable = builder.immutable;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private Map<String, String> data;
        private Map<String, String> binaryData;
        private Boolean immutable;

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

        public Builder data(final Map<String, String> data) {
            this.data = data;
            return this;
        }

        /**
         * Add a single data entry to the ConfigMap.
         *
         * @param key the data key
         * @param value the data value
         * @return this builder
         */
        public Builder addData(final String key, final String value) {
            if (this.data == null) {
                this.data = new HashMap<>();
            }
            this.data.put(key, value);
            return this;
        }

        public Builder binaryData(final Map<String, String> binaryData) {
            this.binaryData = binaryData;
            return this;
        }

        /**
         * Add a single binary data entry to the ConfigMap.
         * The value should be base64-encoded.
         *
         * @param key the data key
         * @param base64Value the base64-encoded value
         * @return this builder
         */
        public Builder addBinaryData(final String key, final String base64Value) {
            if (this.binaryData == null) {
                this.binaryData = new HashMap<>();
            }
            this.binaryData.put(key, base64Value);
            return this;
        }

        /**
         * Set whether the ConfigMap should be immutable.
         * Immutable ConfigMaps cannot be updated after creation.
         *
         * @param immutable true to make the ConfigMap immutable
         * @return this builder
         */
        public Builder immutable(final boolean immutable) {
            this.immutable = immutable;
            return this;
        }

        public ConfigMap build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("ConfigMap name is required");
            }
            return new ConfigMap(this);
        }
    }
}
