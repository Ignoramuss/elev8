package io.elev8.resources.event;

import io.elev8.core.watch.WatchOptions;
import io.elev8.resources.ObjectReference;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration options for watching Kubernetes Event resources with specialized filtering.
 * Provides a fluent builder for constructing event-specific field selectors.
 */
@Getter
@Builder
public class EventWatchOptions {

    private final String resourceVersion;

    @Builder.Default
    private final Integer timeoutSeconds = 300;

    @Builder.Default
    private final Boolean allowWatchBookmarks = true;

    private final String labelSelector;

    private final String involvedObjectName;

    private final String involvedObjectNamespace;

    private final String involvedObjectKind;

    private final String involvedObjectUid;

    private final String eventType;

    private final String reason;

    private final String reportingController;

    /**
     * Converts this EventWatchOptions to a WatchOptions with the appropriate field selector.
     *
     * @return a WatchOptions instance with field selectors built from the event filters
     */
    public WatchOptions toWatchOptions() {
        final List<String> selectors = new ArrayList<>();

        if (involvedObjectName != null) {
            selectors.add("involvedObject.name=" + involvedObjectName);
        }
        if (involvedObjectNamespace != null) {
            selectors.add("involvedObject.namespace=" + involvedObjectNamespace);
        }
        if (involvedObjectKind != null) {
            selectors.add("involvedObject.kind=" + involvedObjectKind);
        }
        if (involvedObjectUid != null) {
            selectors.add("involvedObject.uid=" + involvedObjectUid);
        }
        if (eventType != null) {
            selectors.add("type=" + eventType);
        }
        if (reason != null) {
            selectors.add("reason=" + reason);
        }
        if (reportingController != null) {
            selectors.add("source.component=" + reportingController);
        }

        final String fieldSelector = selectors.isEmpty() ? null : String.join(",", selectors);

        return WatchOptions.builder()
                .resourceVersion(resourceVersion)
                .timeoutSeconds(timeoutSeconds)
                .allowWatchBookmarks(allowWatchBookmarks)
                .labelSelector(labelSelector)
                .fieldSelector(fieldSelector)
                .build();
    }

    /**
     * Creates EventWatchOptions for watching events related to a specific object.
     *
     * @param ref the ObjectReference of the involved object
     * @return EventWatchOptions configured to filter by the object reference
     */
    public static EventWatchOptions forObject(final ObjectReference ref) {
        if (ref == null) {
            throw new IllegalArgumentException("ObjectReference cannot be null");
        }

        return EventWatchOptions.builder()
                .involvedObjectName(ref.getName())
                .involvedObjectNamespace(ref.getNamespace())
                .involvedObjectKind(ref.getKind())
                .involvedObjectUid(ref.getUid())
                .build();
    }

    /**
     * Creates EventWatchOptions for watching warning events only.
     *
     * @return EventWatchOptions configured to filter for Warning type events
     */
    public static EventWatchOptions forWarnings() {
        return EventWatchOptions.builder()
                .eventType(Event.TYPE_WARNING)
                .build();
    }

    /**
     * Creates EventWatchOptions for watching events of a specific type.
     *
     * @param type the event type to filter for (e.g., "Normal" or "Warning")
     * @return EventWatchOptions configured to filter by the event type
     * @throws IllegalArgumentException if type is null
     */
    public static EventWatchOptions forType(final String type) {
        if (type == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        return EventWatchOptions.builder()
                .eventType(type)
                .build();
    }

    /**
     * Creates EventWatchOptions for watching events with a specific reason.
     *
     * @param reason the event reason to filter for (e.g., "Created", "Scheduled")
     * @return EventWatchOptions configured to filter by the reason
     * @throws IllegalArgumentException if reason is null
     */
    public static EventWatchOptions forReason(final String reason) {
        if (reason == null) {
            throw new IllegalArgumentException("Event reason cannot be null");
        }
        return EventWatchOptions.builder()
                .reason(reason)
                .build();
    }

    /**
     * Creates a default EventWatchOptions instance with standard settings.
     *
     * @return a new EventWatchOptions with default values
     */
    public static EventWatchOptions defaults() {
        return EventWatchOptions.builder().build();
    }
}
