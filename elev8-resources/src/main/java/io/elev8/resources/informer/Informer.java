package io.elev8.resources.informer;

import io.elev8.core.watch.ResourceChangeEvent;
import io.elev8.core.watch.ResourceChangeStream;
import io.elev8.resources.KubernetesResource;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
    private final long resyncPeriodMillis;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean synced = new AtomicBoolean(false);

    private volatile Thread eventProcessorThread;
    private volatile ResourceChangeStream<T> currentStream;
    private volatile ScheduledExecutorService resyncExecutor;
    private volatile ScheduledFuture<?> resyncFuture;

    /**
     * Creates a new Informer with the given suppliers for list and stream operations.
     * Resync is disabled by default.
     *
     * @param listSupplier supplies the initial list of resources
     * @param streamSupplier supplies the resource change stream for watching
     */
    public Informer(final Supplier<List<T>> listSupplier,
                    final Supplier<ResourceChangeStream<T>> streamSupplier) {
        this(new InMemoryStore<>(), listSupplier, streamSupplier, 0);
    }

    /**
     * Creates a new Informer with a custom store.
     * Resync is disabled by default.
     *
     * @param store the store to use for caching resources
     * @param listSupplier supplies the initial list of resources
     * @param streamSupplier supplies the resource change stream for watching
     */
    public Informer(final Store<T> store,
                    final Supplier<List<T>> listSupplier,
                    final Supplier<ResourceChangeStream<T>> streamSupplier) {
        this(store, listSupplier, streamSupplier, 0);
    }

    /**
     * Creates a new Informer with the given suppliers and resync period.
     *
     * @param listSupplier supplies the initial list of resources
     * @param streamSupplier supplies the resource change stream for watching
     * @param resyncPeriodMillis resync period in milliseconds (0 to disable)
     */
    public Informer(final Supplier<List<T>> listSupplier,
                    final Supplier<ResourceChangeStream<T>> streamSupplier,
                    final long resyncPeriodMillis) {
        this(new InMemoryStore<>(), listSupplier, streamSupplier, resyncPeriodMillis);
    }

    /**
     * Creates a new Informer with a custom store and resync period.
     *
     * @param store the store to use for caching resources
     * @param listSupplier supplies the initial list of resources
     * @param streamSupplier supplies the resource change stream for watching
     * @param resyncPeriodMillis resync period in milliseconds (0 to disable)
     */
    public Informer(final Store<T> store,
                    final Supplier<List<T>> listSupplier,
                    final Supplier<ResourceChangeStream<T>> streamSupplier,
                    final long resyncPeriodMillis) {
        this.store = store;
        this.listSupplier = listSupplier;
        this.streamSupplier = streamSupplier;
        this.resyncPeriodMillis = resyncPeriodMillis;
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

        stopResyncLoop();

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

            if (resyncPeriodMillis > 0) {
                startResyncLoop();
            }

            watchLoop();
        } catch (Exception e) {
            if (running.get()) {
                log.error("Error in informer event loop", e);
            }
        } finally {
            stopResyncLoop();
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

    private void startResyncLoop() {
        log.info("Starting resync loop with period {} ms", resyncPeriodMillis);
        resyncExecutor = Executors.newScheduledThreadPool(1, r -> {
            final Thread t = new Thread(r, "informer-resync");
            t.setDaemon(true);
            return t;
        });

        resyncFuture = resyncExecutor.scheduleAtFixedRate(
                this::performResync,
                resyncPeriodMillis,
                resyncPeriodMillis,
                TimeUnit.MILLISECONDS
        );
    }

    private void performResync() {
        if (!running.get() || !synced.get()) {
            return;
        }

        log.debug("Performing resync...");
        try {
            final List<T> oldResources = store.list();
            final Map<String, T> oldState = new HashMap<>();
            for (final T resource : oldResources) {
                oldState.put(InMemoryStore.keyFor(resource), resource);
            }

            final List<T> newResources = listSupplier.get();
            if (newResources == null) {
                log.warn("Resync returned null resource list, skipping");
                return;
            }

            final Map<String, T> newState = new HashMap<>();
            for (final T resource : newResources) {
                newState.put(InMemoryStore.keyFor(resource), resource);
            }

            store.replace(newResources);

            for (final T newResource : newResources) {
                final String key = InMemoryStore.keyFor(newResource);
                final T oldResource = oldState.get(key);
                if (oldResource != null) {
                    dispatchOnUpdate(oldResource, newResource);
                } else {
                    dispatchOnAdd(newResource);
                }
            }

            for (final T oldResource : oldResources) {
                final String key = InMemoryStore.keyFor(oldResource);
                if (!newState.containsKey(key)) {
                    dispatchOnDelete(oldResource);
                }
            }

            log.debug("Resync complete, {} resources in cache", store.size());
        } catch (Exception e) {
            log.warn("Resync failed, will retry at next interval: {}", e.getMessage());
        }
    }

    private void stopResyncLoop() {
        if (resyncFuture != null) {
            resyncFuture.cancel(false);
            resyncFuture = null;
        }
        if (resyncExecutor != null) {
            resyncExecutor.shutdownNow();
            try {
                resyncExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            resyncExecutor = null;
        }
    }
}
