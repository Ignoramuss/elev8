package io.elev8.core.watch;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the type of watch event received from the Kubernetes API server.
 * These event types indicate how a resource has changed.
 */
public enum WatchEventType {
    /**
     * A new resource has been added.
     */
    ADDED("ADDED"),

    /**
     * An existing resource has been modified.
     */
    MODIFIED("MODIFIED"),

    /**
     * A resource has been deleted.
     */
    DELETED("DELETED"),

    /**
     * A bookmark event for resuming watches efficiently.
     * Contains only resourceVersion, no object data.
     */
    BOOKMARK("BOOKMARK"),

    /**
     * An error occurred during the watch operation.
     */
    ERROR("ERROR");

    private final String value;

    WatchEventType(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static WatchEventType fromValue(final String value) {
        for (final WatchEventType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown watch event type: " + value);
    }
}
