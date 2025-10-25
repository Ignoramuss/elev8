package io.elev8.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base class for Kubernetes resources with JSON serialization support.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractResource implements KubernetesResource {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    String apiVersion;
    String kind;
    Metadata metadata;

    protected AbstractResource() {
    }

    protected AbstractResource(final String apiVersion, final String kind, final Metadata metadata) {
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.metadata = metadata;
    }

    @Override
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize resource to JSON", e);
        }
    }

    /**
     * Parse a resource from JSON.
     *
     * @param json JSON string
     * @param clazz resource class
     * @param <T> resource type
     * @return parsed resource
     */
    public static <T extends KubernetesResource> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse resource from JSON", e);
        }
    }

    /**
     * Get the shared ObjectMapper instance for JSON operations.
     *
     * @return the ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
