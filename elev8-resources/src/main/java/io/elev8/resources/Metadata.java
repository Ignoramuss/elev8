package io.elev8.resources;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Kubernetes resource metadata.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metadata {

    private String name;
    private String namespace;
    private String uid;
    private String resourceVersion;
    private Instant creationTimestamp;
    private Map<String, String> labels;
    private Map<String, String> annotations;

    public Metadata() {
    }

    private Metadata(Builder builder) {
        this.name = builder.name;
        this.namespace = builder.namespace;
        this.uid = builder.uid;
        this.resourceVersion = builder.resourceVersion;
        this.creationTimestamp = builder.creationTimestamp;
        this.labels = builder.labels;
        this.annotations = builder.annotations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public Instant getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Instant creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String namespace;
        private String uid;
        private String resourceVersion;
        private Instant creationTimestamp;
        private Map<String, String> labels;
        private Map<String, String> annotations;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder uid(String uid) {
            this.uid = uid;
            return this;
        }

        public Builder resourceVersion(String resourceVersion) {
            this.resourceVersion = resourceVersion;
            return this;
        }

        public Builder creationTimestamp(Instant creationTimestamp) {
            this.creationTimestamp = creationTimestamp;
            return this;
        }

        public Builder labels(Map<String, String> labels) {
            this.labels = labels;
            return this;
        }

        public Builder label(String key, String value) {
            if (this.labels == null) {
                this.labels = new HashMap<>();
            }
            this.labels.put(key, value);
            return this;
        }

        public Builder annotations(Map<String, String> annotations) {
            this.annotations = annotations;
            return this;
        }

        public Builder annotation(String key, String value) {
            if (this.annotations == null) {
                this.annotations = new HashMap<>();
            }
            this.annotations.put(key, value);
            return this;
        }

        public Metadata build() {
            return new Metadata(this);
        }
    }
}
