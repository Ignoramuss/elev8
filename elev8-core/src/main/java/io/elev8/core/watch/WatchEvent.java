package io.elev8.core.watch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a watch event from the Kubernetes API server.
 * Watch events are sent when resources are added, modified, deleted, or when bookmarks occur.
 *
 * @param <T> the type of Kubernetes resource being watched
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WatchEvent<T> {
    /**
     * The type of watch event (ADDED, MODIFIED, DELETED, BOOKMARK, ERROR).
     */
    @JsonProperty("type")
    private WatchEventType type;

    /**
     * The Kubernetes resource object associated with this event.
     * May be null for ERROR or BOOKMARK events.
     */
    @JsonProperty("object")
    private T object;

    /**
     * Creates a new watch event with the specified type and object.
     *
     * @param type the event type
     * @param object the resource object
     * @param <T> the resource type
     * @return a new WatchEvent instance
     */
    public static <T> WatchEvent<T> of(final WatchEventType type, final T object) {
        return WatchEvent.<T>builder()
                .type(type)
                .object(object)
                .build();
    }

    /**
     * Checks if this is an ADDED event.
     *
     * @return true if the event type is ADDED
     */
    public boolean isAdded() {
        return type == WatchEventType.ADDED;
    }

    /**
     * Checks if this is a MODIFIED event.
     *
     * @return true if the event type is MODIFIED
     */
    public boolean isModified() {
        return type == WatchEventType.MODIFIED;
    }

    /**
     * Checks if this is a DELETED event.
     *
     * @return true if the event type is DELETED
     */
    public boolean isDeleted() {
        return type == WatchEventType.DELETED;
    }

    /**
     * Checks if this is a BOOKMARK event.
     *
     * @return true if the event type is BOOKMARK
     */
    public boolean isBookmark() {
        return type == WatchEventType.BOOKMARK;
    }

    /**
     * Checks if this is an ERROR event.
     *
     * @return true if the event type is ERROR
     */
    public boolean isError() {
        return type == WatchEventType.ERROR;
    }
}
