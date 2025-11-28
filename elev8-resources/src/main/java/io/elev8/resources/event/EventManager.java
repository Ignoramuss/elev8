package io.elev8.resources.event;

import io.elev8.core.client.KubernetesClient;
import io.elev8.core.watch.Watcher;
import io.elev8.resources.AbstractResourceManager;
import io.elev8.resources.ObjectReference;
import io.elev8.resources.ResourceException;

/**
 * Resource manager for Kubernetes Event resources with specialized watch and filtering capabilities.
 */
public final class EventManager extends AbstractResourceManager<Event> {

    public EventManager(final KubernetesClient client) {
        super(client, Event.class, "/api/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "events";
    }

    /**
     * Watch events in a namespace with event-specific filtering options.
     *
     * @param namespace the namespace to watch events in
     * @param options event-specific watch options for filtering
     * @param watcher the callback to handle watch events
     * @throws ResourceException if the watch operation fails
     * @throws IllegalArgumentException if namespace, options, or watcher is null
     */
    public void watch(final String namespace, final EventWatchOptions options, final Watcher<Event> watcher)
            throws ResourceException {
        if (namespace == null) {
            throw new IllegalArgumentException("Namespace cannot be null");
        }
        if (options == null) {
            throw new IllegalArgumentException("EventWatchOptions cannot be null");
        }
        if (watcher == null) {
            throw new IllegalArgumentException("Watcher cannot be null");
        }
        watch(namespace, options.toWatchOptions(), watcher);
    }

    /**
     * Watch events across all namespaces with event-specific filtering options.
     *
     * @param options event-specific watch options for filtering
     * @param watcher the callback to handle watch events
     * @throws ResourceException if the watch operation fails
     * @throws IllegalArgumentException if options or watcher is null
     */
    public void watchAllNamespaces(final EventWatchOptions options, final Watcher<Event> watcher)
            throws ResourceException {
        if (options == null) {
            throw new IllegalArgumentException("EventWatchOptions cannot be null");
        }
        if (watcher == null) {
            throw new IllegalArgumentException("Watcher cannot be null");
        }
        watchAllNamespaces(options.toWatchOptions(), watcher);
    }

    /**
     * Watch events related to a specific Kubernetes object.
     *
     * @param namespace the namespace to watch events in
     * @param involvedObject the object reference to filter events by
     * @param watcher the callback to handle watch events
     * @throws ResourceException if the watch operation fails
     * @throws IllegalArgumentException if namespace or watcher is null
     */
    public void watchForObject(final String namespace, final ObjectReference involvedObject,
            final Watcher<Event> watcher) throws ResourceException {
        if (namespace == null) {
            throw new IllegalArgumentException("Namespace cannot be null");
        }
        if (watcher == null) {
            throw new IllegalArgumentException("Watcher cannot be null");
        }
        final EventWatchOptions options = EventWatchOptions.forObject(involvedObject);
        watch(namespace, options, watcher);
    }

    /**
     * Watch events related to a specific Pod by name.
     *
     * @param namespace the namespace containing the pod
     * @param podName the name of the pod to filter events by
     * @param watcher the callback to handle watch events
     * @throws ResourceException if the watch operation fails
     * @throws IllegalArgumentException if namespace, podName, or watcher is null
     */
    public void watchForPod(final String namespace, final String podName, final Watcher<Event> watcher)
            throws ResourceException {
        if (namespace == null) {
            throw new IllegalArgumentException("Namespace cannot be null");
        }
        if (podName == null) {
            throw new IllegalArgumentException("Pod name cannot be null");
        }
        if (watcher == null) {
            throw new IllegalArgumentException("Watcher cannot be null");
        }
        final EventWatchOptions options = EventWatchOptions.builder()
                .involvedObjectKind("Pod")
                .involvedObjectName(podName)
                .involvedObjectNamespace(namespace)
                .build();
        watch(namespace, options, watcher);
    }

    /**
     * Watch only Warning type events in a namespace.
     *
     * @param namespace the namespace to watch warning events in
     * @param watcher the callback to handle watch events
     * @throws ResourceException if the watch operation fails
     * @throws IllegalArgumentException if namespace or watcher is null
     */
    public void watchWarnings(final String namespace, final Watcher<Event> watcher) throws ResourceException {
        if (namespace == null) {
            throw new IllegalArgumentException("Namespace cannot be null");
        }
        if (watcher == null) {
            throw new IllegalArgumentException("Watcher cannot be null");
        }
        final EventWatchOptions options = EventWatchOptions.forWarnings();
        watch(namespace, options, watcher);
    }

    /**
     * Watch events with a specific reason in a namespace.
     *
     * @param namespace the namespace to watch events in
     * @param reason the event reason to filter by (e.g., "Created", "Scheduled", "FailedMount")
     * @param watcher the callback to handle watch events
     * @throws ResourceException if the watch operation fails
     * @throws IllegalArgumentException if namespace or watcher is null
     */
    public void watchByReason(final String namespace, final String reason, final Watcher<Event> watcher)
            throws ResourceException {
        if (namespace == null) {
            throw new IllegalArgumentException("Namespace cannot be null");
        }
        if (watcher == null) {
            throw new IllegalArgumentException("Watcher cannot be null");
        }
        final EventWatchOptions options = EventWatchOptions.forReason(reason);
        watch(namespace, options, watcher);
    }

    /**
     * Watch events from a specific reporting controller in a namespace.
     *
     * @param namespace the namespace to watch events in
     * @param controller the reporting controller to filter by (e.g., "kubelet", "kube-scheduler")
     * @param watcher the callback to handle watch events
     * @throws ResourceException if the watch operation fails
     * @throws IllegalArgumentException if namespace, controller, or watcher is null
     */
    public void watchByController(final String namespace, final String controller, final Watcher<Event> watcher)
            throws ResourceException {
        if (namespace == null) {
            throw new IllegalArgumentException("Namespace cannot be null");
        }
        if (controller == null) {
            throw new IllegalArgumentException("Controller cannot be null");
        }
        if (watcher == null) {
            throw new IllegalArgumentException("Watcher cannot be null");
        }
        final EventWatchOptions options = EventWatchOptions.builder()
                .reportingController(controller)
                .build();
        watch(namespace, options, watcher);
    }
}
