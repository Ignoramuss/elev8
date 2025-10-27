package io.elev8.resources.secret;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Secret holds sensitive data such as passwords, OAuth tokens, and SSH keys.
 * Secrets are similar to ConfigMaps but are specifically intended to hold confidential data.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Secret extends AbstractResource {

    /**
     * Opaque secrets (default type) - arbitrary user-defined data.
     */
    public static final String TYPE_OPAQUE = "Opaque";

    /**
     * Service account token.
     */
    public static final String TYPE_SERVICE_ACCOUNT_TOKEN = "kubernetes.io/service-account-token";

    /**
     * Docker config file.
     */
    public static final String TYPE_DOCKERCFG = "kubernetes.io/dockercfg";

    /**
     * Docker config JSON.
     */
    public static final String TYPE_DOCKERCONFIGJSON = "kubernetes.io/dockerconfigjson";

    /**
     * Basic authentication credentials.
     */
    public static final String TYPE_BASIC_AUTH = "kubernetes.io/basic-auth";

    /**
     * SSH authentication credentials.
     */
    public static final String TYPE_SSH_AUTH = "kubernetes.io/ssh-auth";

    /**
     * TLS certificate and key.
     */
    public static final String TYPE_TLS = "kubernetes.io/tls";

    /**
     * Bootstrap token.
     */
    public static final String TYPE_BOOTSTRAP_TOKEN = "bootstrap.kubernetes.io/token";

    private Map<String, String> data;
    private Map<String, String> stringData;
    private String type;
    private Boolean immutable;

    public Secret() {
        super("v1", "Secret", null);
    }

    private Secret(final Builder builder) {
        super("v1", "Secret", builder.metadata);
        this.data = builder.data;
        this.stringData = builder.stringData;
        this.type = builder.type != null ? builder.type : TYPE_OPAQUE;
        this.immutable = builder.immutable;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private Map<String, String> data;
        private Map<String, String> stringData;
        private String type;
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

        /**
         * Set the data map. Data values should be base64-encoded strings.
         *
         * @param data the data map
         * @return this builder
         */
        public Builder data(final Map<String, String> data) {
            this.data = data;
            return this;
        }

        /**
         * Add a single data entry to the Secret.
         * The value should be base64-encoded.
         *
         * @param key the data key
         * @param base64Value the base64-encoded value
         * @return this builder
         */
        public Builder addData(final String key, final String base64Value) {
            if (this.data == null) {
                this.data = new HashMap<>();
            }
            this.data.put(key, base64Value);
            return this;
        }

        /**
         * Set the stringData map. String values will be automatically base64-encoded by Kubernetes.
         * This is a write-only convenience field.
         *
         * @param stringData the string data map
         * @return this builder
         */
        public Builder stringData(final Map<String, String> stringData) {
            this.stringData = stringData;
            return this;
        }

        /**
         * Add a single string data entry to the Secret.
         * The value will be automatically base64-encoded by Kubernetes.
         *
         * @param key the data key
         * @param value the plain text value
         * @return this builder
         */
        public Builder addStringData(final String key, final String value) {
            if (this.stringData == null) {
                this.stringData = new HashMap<>();
            }
            this.stringData.put(key, value);
            return this;
        }

        /**
         * Set the secret type. Defaults to Opaque if not specified.
         * Common types: Opaque, kubernetes.io/tls, kubernetes.io/dockerconfigjson
         *
         * @param type the secret type
         * @return this builder
         */
        public Builder type(final String type) {
            this.type = type;
            return this;
        }

        /**
         * Set whether the Secret should be immutable.
         * Immutable Secrets cannot be updated after creation.
         *
         * @param immutable true to make the Secret immutable
         * @return this builder
         */
        public Builder immutable(final boolean immutable) {
            this.immutable = immutable;
            return this;
        }

        /**
         * Create an Opaque secret (default type for arbitrary user data).
         *
         * @return this builder
         */
        public Builder opaque() {
            this.type = TYPE_OPAQUE;
            return this;
        }

        /**
         * Create a TLS secret with certificate and key.
         *
         * @param tlsCert the TLS certificate (base64-encoded)
         * @param tlsKey the TLS private key (base64-encoded)
         * @return this builder
         */
        public Builder tls(final String tlsCert, final String tlsKey) {
            this.type = TYPE_TLS;
            if (this.data == null) {
                this.data = new HashMap<>();
            }
            this.data.put("tls.crt", tlsCert);
            this.data.put("tls.key", tlsKey);
            return this;
        }

        /**
         * Create a basic authentication secret.
         *
         * @param username the username
         * @param password the password
         * @return this builder
         */
        public Builder basicAuth(final String username, final String password) {
            this.type = TYPE_BASIC_AUTH;
            if (this.stringData == null) {
                this.stringData = new HashMap<>();
            }
            this.stringData.put("username", username);
            this.stringData.put("password", password);
            return this;
        }

        /**
         * Create a Docker config JSON secret for pulling images from private registries.
         *
         * @param dockerConfigJson the Docker config JSON (base64-encoded)
         * @return this builder
         */
        public Builder dockerConfigJson(final String dockerConfigJson) {
            this.type = TYPE_DOCKERCONFIGJSON;
            if (this.data == null) {
                this.data = new HashMap<>();
            }
            this.data.put(".dockerconfigjson", dockerConfigJson);
            return this;
        }

        /**
         * Create an SSH authentication secret.
         *
         * @param sshPrivateKey the SSH private key
         * @return this builder
         */
        public Builder sshAuth(final String sshPrivateKey) {
            this.type = TYPE_SSH_AUTH;
            if (this.stringData == null) {
                this.stringData = new HashMap<>();
            }
            this.stringData.put("ssh-privatekey", sshPrivateKey);
            return this;
        }

        public Secret build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("Secret name is required");
            }
            return new Secret(this);
        }
    }
}
