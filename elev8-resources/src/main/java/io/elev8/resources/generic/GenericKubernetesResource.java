package io.elev8.resources.generic;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Flexible resource class for working with custom resources without compile-time type information.
 * Uses Map-based storage for spec and status to accommodate any custom resource schema.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericKubernetesResource extends AbstractResource {

    private Map<String, Object> spec;
    private Map<String, Object> status;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public GenericKubernetesResource() {
        super();
    }

    /**
     * Get additional properties that don't map to known fields.
     *
     * @return the additional properties
     */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Set an additional property that doesn't map to a known field.
     *
     * @param name the property name
     * @param value the property value
     */
    @JsonAnySetter
    public void setAdditionalProperty(final String name, final Object value) {
        additionalProperties.put(name, value);
    }

    /**
     * Get a value from the spec using dot-notation path (e.g., "nested.field").
     *
     * @param path the dot-separated path to the value
     * @return the value at the path, or null if not found
     */
    public Object getSpec(final String path) {
        return getNestedValue(spec, path);
    }

    /**
     * Set a value in the spec using dot-notation path (e.g., "nested.field").
     *
     * @param path the dot-separated path
     * @param value the value to set
     * @return this resource for chaining
     */
    public GenericKubernetesResource setSpec(final String path, final Object value) {
        if (spec == null) {
            spec = new HashMap<>();
        }
        setNestedValue(spec, path, value);
        return this;
    }

    /**
     * Get a value from the status using dot-notation path (e.g., "nested.field").
     *
     * @param path the dot-separated path to the value
     * @return the value at the path, or null if not found
     */
    public Object getStatus(final String path) {
        return getNestedValue(status, path);
    }

    /**
     * Set a value in the status using dot-notation path (e.g., "nested.field").
     *
     * @param path the dot-separated path
     * @param value the value to set
     * @return this resource for chaining
     */
    public GenericKubernetesResource setStatus(final String path, final Object value) {
        if (status == null) {
            status = new HashMap<>();
        }
        setNestedValue(status, path, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    private Object getNestedValue(final Map<String, Object> map, final String path) {
        if (map == null || path == null || path.isEmpty()) {
            return null;
        }

        final String[] parts = path.split("\\.");
        Object current = map;

        for (final String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }

        return current;
    }

    @SuppressWarnings("unchecked")
    private void setNestedValue(final Map<String, Object> map, final String path, final Object value) {
        if (map == null || path == null || path.isEmpty()) {
            return;
        }

        final String[] parts = path.split("\\.");
        Map<String, Object> current = map;

        for (int i = 0; i < parts.length - 1; i++) {
            final Object next = current.get(parts[i]);
            if (next instanceof Map) {
                current = (Map<String, Object>) next;
            } else {
                final Map<String, Object> newMap = new HashMap<>();
                current.put(parts[i], newMap);
                current = newMap;
            }
        }

        current.put(parts[parts.length - 1], value);
    }

    /**
     * Create a new builder for GenericKubernetesResource.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder initialized from a GenericResourceContext.
     *
     * @param context the context to initialize from
     * @return a new builder
     */
    public static Builder fromContext(final GenericResourceContext context) {
        return new Builder()
                .apiVersion(context.getApiVersion())
                .kind(context.getKind());
    }

    /**
     * Builder for GenericKubernetesResource.
     */
    public static class Builder {
        private String apiVersion;
        private String kind;
        private String name;
        private String namespace;
        private String uid;
        private String resourceVersion;
        private Map<String, String> labels;
        private Map<String, String> annotations;
        private Map<String, Object> spec;
        private Map<String, Object> status;
        private final Map<String, Object> additionalProperties = new HashMap<>();

        public Builder apiVersion(final String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public Builder kind(final String kind) {
            this.kind = kind;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder namespace(final String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder uid(final String uid) {
            this.uid = uid;
            return this;
        }

        public Builder resourceVersion(final String resourceVersion) {
            this.resourceVersion = resourceVersion;
            return this;
        }

        public Builder labels(final Map<String, String> labels) {
            this.labels = labels;
            return this;
        }

        public Builder label(final String key, final String value) {
            if (this.labels == null) {
                this.labels = new HashMap<>();
            }
            this.labels.put(key, value);
            return this;
        }

        public Builder annotations(final Map<String, String> annotations) {
            this.annotations = annotations;
            return this;
        }

        public Builder annotation(final String key, final String value) {
            if (this.annotations == null) {
                this.annotations = new HashMap<>();
            }
            this.annotations.put(key, value);
            return this;
        }

        public Builder spec(final Map<String, Object> spec) {
            this.spec = spec;
            return this;
        }

        public Builder specField(final String key, final Object value) {
            if (this.spec == null) {
                this.spec = new HashMap<>();
            }
            this.spec.put(key, value);
            return this;
        }

        public Builder status(final Map<String, Object> status) {
            this.status = status;
            return this;
        }

        public Builder statusField(final String key, final Object value) {
            if (this.status == null) {
                this.status = new HashMap<>();
            }
            this.status.put(key, value);
            return this;
        }

        public Builder additionalProperty(final String key, final Object value) {
            this.additionalProperties.put(key, value);
            return this;
        }

        public GenericKubernetesResource build() {
            final GenericKubernetesResource resource = new GenericKubernetesResource();
            resource.setApiVersion(apiVersion);
            resource.setKind(kind);

            final Metadata.MetadataBuilder metadataBuilder = Metadata.builder();
            metadataBuilder.name(name);
            metadataBuilder.namespace(namespace);
            metadataBuilder.uid(uid);
            metadataBuilder.resourceVersion(resourceVersion);
            if (labels != null) {
                metadataBuilder.labels(labels);
            }
            if (annotations != null) {
                metadataBuilder.annotations(annotations);
            }
            resource.setMetadata(metadataBuilder.build());

            resource.setSpec(spec);
            resource.setStatus(status);

            for (final Map.Entry<String, Object> entry : additionalProperties.entrySet()) {
                resource.setAdditionalProperty(entry.getKey(), entry.getValue());
            }

            return resource;
        }
    }
}
