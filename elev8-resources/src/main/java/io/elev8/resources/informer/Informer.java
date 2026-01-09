package io.elev8.resources.informer;

import io.elev8.core.watch.ResourceChangeEvent;
import io.elev8.core.watch.ResourceChangeStream;
import io.elev8.resources.KubernetesResource;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * An Informer watches Kubernetes resources and maintains an in-memory cache (Store).
 * It provides event handlers for reacting to resource changes.
 *
 * <p>The Informer pattern follows a list-then-watch approach:</p>
 * <ol>
 *   <li>Initial LIST to populate the cache with existing resources</li>
 *   <li>Fire onAdd() for each resource in the initial list</li>
 *   <li>Set hasSynced = true</li>
 *   <li>WATCH for changes and dispatch events to handlers</li>
 * </ol>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * Informer<Pod> informer = Informers.forNamespace(podManager, "default", InformerOptions.defaults());
 * informer.addEventHandler(new ResourceEventHandler<Pod>() {
 *     public void onAdd(Pod pod) { ... }
 *     public void onUpdate(Pod oldPod, Pod newPod) { ... }
 *     public void onDelete(Pod pod) { ... }
 * });
 * informer.start();
 * }</pre>
 *
 * @param <T> the type of Kubernetes resource
 */
@Slf4j
public class Informer<T extends KubernetesResource> implements AutoCloseable {

    private final Store<T> store;
    private final List<ResourceEventHandler<T>> handlers = new CopyOnWriteArrayList<>();
    private final Supplier<List<T>> listSupplier;
    private final Supplier<ResourceChangeStream<T>> streamSupplier;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean synced = new AtomicBoolean(false);

    private volatile Thread eventProcessorThread;
    private volatile ResourceChangeStream<T> currentStream;

    /**
     * Creates a new Informer with the given suppliers for list and stream operations.
     *
     * @param listSupplier supplies the initial list of resources
     * @param streamSupplier supplies the resource change stream for watching
     */
    public Informer(final Supplier<List<T>> listSupplier,
                    final Supplier<ResourceChangeStream<T>> streamSupplier) {
        this(new InMemoryStore<>(), listSupplier, streamSupplier);
    }

    /**
     * Creates a new Informer with a custom store.
     *
     * @param store the store to use for caching resources
     * @param listSupplier supplies the initial list of resources
     * @param streamSupplier supplies the resource change stream for watching
     */
    public Informer(final Store<T> store,
                    final Supplier<List<T>> listSupplier,
                    final Supplier<ResourceChangeStream<T>> streamSupplier) {
        this.store = store;
        this.listSupplier = listSupplier;
        this.streamSupplier = streamSupplier;
    }

    /**
     * Starts the informer. This will:
     * <ol>
     *   <li>Perform an initial list to populate the cache</li>
     *   <li>Fire onAdd for each resource</li>
     *   <li>Start watching for changes</li>
     * </ol>
     *
     * @throws IllegalStateException if the informer is already running
     */
    public void start() {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Informer is already running");
        }

        synced.set(false);
        eventProcessorThread = new Thread(this::runEventLoop, "informer-event-processor");
        eventProcessorThread.setDaemon(true);
        eventProcessorThread.start();

        log.info("Informer started");
    }

    /**
     * Stops the informer gracefully.
     */
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        log.info("Stopping informer...");

        if (currentStream != null) {
            try {
                currentStream.close();
            } catch (Exception e) {
                log.warn("Error closing stream", e);
            }
        }

        if (eventProcessorThread != null) {
            eventProcessorThread.interrupt();
            try {
                eventProcessorThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        log.info("Informer stopped");
    }

    @Override
    public void close() {
        stop();
    }

    /**
     * Returns whether the informer has completed its initial list operation.
     *
     * @return true if the initial sync is complete
     */
    public boolean hasSynced() {
        return synced.get();
    }

    /**
     * Returns whether the informer is currently running.
     *
     * @return true if the informer is running
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Returns the store containing cached resources.
     *
     * @return the resource store
     */
    public Store<T> getStore() {
        return store;
    }

    /**
     * Adds an event handler to receive notifications about resource changes.
     * If the informer has already synced, the handler will receive onAdd
     * events for all existing resources in the cache.
     *
     * @param handler the event handler to add
     */
    public void addEventHandler(final ResourceEventHandler<T> handler) {
        if (handler == null) {
            return;
        }
        handlers.add(handler);

        if (synced.get()) {
            for (final T resource : store.list()) {
                try {
                    handler.onAdd(resource);
                } catch (Exception e) {
                    log.error("Error in handler onAdd for existing resource", e);
                }
            }
        }
    }

    /**
     * Removes an event handler.
     *
     * @param handler the event handler to remove
     */
    public void removeEventHandler(final ResourceEventHandler<T> handler) {
        handlers.remove(handler);
    }

    private void runEventLoop() {
        try {
            performInitialList();
            synced.set(true);
            log.info("Initial sync complete, {} resources cached", store.size());

            watchLoop();
        } catch (Exception e) {
            if (running.get()) {
                log.error("Error in informer event loop", e);
            }
        }
    }

    private void performInitialList() {
        log.debug("Performing initial list...");
        final List<T> resources = listSupplier.get();

        if (resources != null) {
            store.replace(resources);

            for (final T resource : resources) {
                dispatchOnAdd(resource);
            }
        }
    }

    private void watchLoop() {
        while (running.get()) {
            try {
                currentStream = streamSupplier.get();
                log.debug("Started watching for changes");

                for (final ResourceChangeEvent<T> event : currentStream) {
                    if (!running.get()) {
                        break;
                    }
                    processEvent(event);
                }
            } catch (Exception e) {
                if (running.get()) {
                    log.warn("Watch stream error, will reconnect: {}", e.getMessage());
                    sleepBeforeReconnect();
                }
            } finally {
                if (currentStream != null) {
                    try {
                        currentStream.close();
                    } catch (Exception e) {
                        log.debug("Error closing stream", e);
                    }
                }
            }
        }
    }

    private void processEvent(final ResourceChangeEvent<T> event) {
        if (event == null) {
            return;
        }

        switch (event.getType()) {
            case CREATED -> {
                final T resource = event.getResource();
                store.add(resource);
                dispatchOnAdd(resource);
            }
            case UPDATED -> {
                final T newResource = event.getResource();
                final T oldResource = event.getPreviousResource();
                store.update(newResource);
                dispatchOnUpdate(oldResource, newResource);
            }
            case DELETED -> {
                final T resource = event.getPreviousResource();
                store.delete(resource);
                dispatchOnDelete(resource);
            }
            case SYNC -> {
                log.debug("Received sync/bookmark event");
            }
        }
    }

    private void dispatchOnAdd(final T resource) {
        for (final ResourceEventHandler<T> handler : handlers) {
            try {
                handler.onAdd(resource);
            } catch (Exception e) {
                log.error("Error in handler onAdd", e);
            }
        }
    }

    private void dispatchOnUpdate(final T oldResource, final T newResource) {
        for (final ResourceEventHandler<T> handler : handlers) {
            try {
                handler.onUpdate(oldResource, newResource);
            } catch (Exception e) {
                log.error("Error in handler onUpdate", e);
            }
        }
    }

    private void dispatchOnDelete(final T resource) {
        for (final ResourceEventHandler<T> handler : handlers) {
            try {
                handler.onDelete(resource);
            } catch (Exception e) {
                log.error("Error in handler onDelete", e);
            }
        }
    }

    private void sleepBeforeReconnect() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
