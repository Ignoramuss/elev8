package io.elev8.core.watch;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

/**
 * Represents a resource change event with rich context including previous state.
 * This is a higher-level abstraction over WatchEvent that provides:
 * - Semantic change types (CREATED, UPDATED, DELETED, SYNC)
 * - Previous resource state for change detection
 * - Timestamp of when the event was received
 *
 * @param <T> the type of Kubernetes resource
 */
@Slf4j
@Getter
@Builder
public class ResourceChangeEvent<T> {

    /**
     * The type of change that occurred.
     */
    private final ResourceChangeType type;

    /**
     * The current state of the resource.
     * For DELETED events, this is null and the previous state contains the last known state.
     */
    private final T resource;

    /**
     * The previous state of the resource.
     * For CREATED events, this is null.
     * For UPDATED events, this contains the state before the update (if tracking is enabled).
     * For DELETED events, this contains the last known state before deletion.
     */
    private final T previousResource;

    /**
     * The resource version from the Kubernetes API.
     * Can be used to resume watches from a specific point.
     */
    private final String resourceVersion;

    /**
     * The timestamp when this event was received by the client.
     * Note: This is the local receipt time, not the server-side event time.
     */
    private final Instant timestamp;

    /**
     * Checks if this is a CREATED event.
     *
     * @return true if the resource was newly created
     */
    public boolean isCreated() {
        return type == ResourceChangeType.CREATED;
    }

    /**
     * Checks if this is an UPDATED event.
     *
     * @return true if the resource was modified
     */
    public boolean isUpdated() {
        return type == ResourceChangeType.UPDATED;
    }

    /**
     * Checks if this is a DELETED event.
     *
     * @return true if the resource was deleted
     */
    public boolean isDeleted() {
        return type == ResourceChangeType.DELETED;
    }

    /**
     * Checks if this is a SYNC event.
     *
     * @return true if this is a synchronization marker (bookmark)
     */
    public boolean isSync() {
        return type == ResourceChangeType.SYNC;
    }

    /**
     * Creates a ResourceChangeEvent from a WatchEvent.
     *
     * @param event the watch event to convert
     * @param previousState the previous state of the resource (may be null)
     * @param <T> the resource type
     * @return a new ResourceChangeEvent
     * @throws IllegalArgumentException if the event type is ERROR
     */
    public static <T> ResourceChangeEvent<T> from(final WatchEvent<T> event, final T previousState) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (event.isError()) {
            throw new IllegalArgumentException("ERROR events cannot be converted to ResourceChangeEvent");
        }

        final ResourceChangeType changeType = ResourceChangeType.fromWatchEventType(event.getType());

        final T currentResource;
        final T previous;

        if (event.isDeleted()) {
            currentResource = null;
            previous = previousState != null ? previousState : event.getObject();
        } else {
            currentResource = event.getObject();
            previous = previousState;
        }

        return ResourceChangeEvent.<T>builder()
                .type(changeType)
                .resource(currentResource)
                .previousResource(previous)
                .resourceVersion(extractResourceVersion(event))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Extracts the resource version from a watch event's object if available.
     */
    private static <T> String extractResourceVersion(final WatchEvent<T> event) {
        final T object = event.getObject();
        if (object == null) {
            return null;
        }
        try {
            final java.lang.reflect.Method getMetadata = object.getClass().getMethod("getMetadata");
            final Object metadata = getMetadata.invoke(object);
            if (metadata != null) {
                final java.lang.reflect.Method getResourceVersion = metadata.getClass().getMethod("getResourceVersion");
                final Object rv = getResourceVersion.invoke(metadata);
                return rv != null ? rv.toString() : null;
            }
        } catch (Exception e) {
            log.debug("Unable to extract resource version from {} using reflection",
                    object.getClass().getSimpleName());
        }
        return null;
    }
}
