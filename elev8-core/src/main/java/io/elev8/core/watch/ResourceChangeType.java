package io.elev8.core.watch;

/**
 * Represents the semantic type of a resource change event.
 * Maps from Kubernetes watch event types to more descriptive change types.
 */
public enum ResourceChangeType {
    /**
     * A new resource has been created.
     * Maps from ADDED watch event.
     */
    CREATED,

    /**
     * An existing resource has been updated.
     * Maps from MODIFIED watch event.
     */
    UPDATED,

    /**
     * A resource has been deleted.
     * Maps from DELETED watch event.
     */
    DELETED,

    /**
     * A synchronization marker event.
     * Maps from BOOKMARK watch event.
     * Contains only resource version, no object data.
     */
    SYNC;

    /**
     * Maps a WatchEventType to the corresponding ResourceChangeType.
     *
     * @param eventType the watch event type to map
     * @return the corresponding resource change type
     * @throws IllegalArgumentException if the event type is ERROR or cannot be mapped
     */
    public static ResourceChangeType fromWatchEventType(final WatchEventType eventType) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        return switch (eventType) {
            case ADDED -> CREATED;
            case MODIFIED -> UPDATED;
            case DELETED -> DELETED;
            case BOOKMARK -> SYNC;
            case ERROR -> throw new IllegalArgumentException("ERROR events cannot be converted to ResourceChangeType");
        };
    }
}
