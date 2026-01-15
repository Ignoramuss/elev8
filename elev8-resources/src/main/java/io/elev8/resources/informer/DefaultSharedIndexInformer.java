package io.elev8.resources.informer;

import io.elev8.core.watch.ResourceChangeStream;
import io.elev8.resources.KubernetesResource;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Default implementation of SharedIndexInformer.
 * Wraps an Informer and provides shared access with per-handler resync support.
 *
 * <p>This implementation uses composition to wrap the existing Informer class,
 * adding handler registration tracking and periodic resync capabilities.</p>
 *
 * @param <T> the type of Kubernetes resource
 */
@Slf4j
public class DefaultSharedIndexInformer<T extends KubernetesResource> implements SharedIndexInformer<T> {

    private final InMemoryIndexer<T> indexer;
    private final Informer<T> delegate;
    private final Set<DefaultEventHandlerRegistration<T>> handlers;
    private final Map<DefaultEventHandlerRegistration<T>, ScheduledFuture<?>> resyncFutures;
    private final ReentrantReadWriteLock handlersLock;
    private final AtomicBoolean started;
    private final AtomicBoolean stopped;
    private final AtomicReference<String> lastResourceVersion;
    private final Duration defaultResyncPeriod;

    private volatile ScheduledExecutorService resyncExecutor;

    /**
     * Creates a new DefaultSharedIndexInformer.
     *
     * @param listSupplier supplies the initial list of resources
     * @param streamSupplier supplies the resource change stream for watching
     * @param defaultResyncPeriod default resync period for handlers that don't specify one
     */
    public DefaultSharedIndexInformer(
            final Supplier<List<T>> listSupplier,
            final Supplier<ResourceChangeStream<T>> streamSupplier,
            final Duration defaultResyncPeriod) {
        this.indexer = new InMemoryIndexer<>();
        this.delegate = new Informer<>(indexer, listSupplier, streamSupplier);
        this.handlers = ConcurrentHashMap.newKeySet();
        this.resyncFutures = new ConcurrentHashMap<>();
        this.handlersLock = new ReentrantReadWriteLock();
        this.started = new AtomicBoolean(false);
        this.stopped = new AtomicBoolean(false);
        this.lastResourceVersion = new AtomicReference<>("");
        this.defaultResyncPeriod = defaultResyncPeriod != null ? defaultResyncPeriod : Duration.ZERO;

        registerDelegateHandler();
    }

    private void registerDelegateHandler() {
        delegate.addEventHandler(new ResourceEventHandler<T>() {
            @Override
            public void onAdd(final T resource) {
                dispatchOnAdd(resource);
            }

            @Override
            public void onUpdate(final T oldResource, final T newResource) {
                dispatchOnUpdate(oldResource, newResource);
            }

            @Override
            public void onDelete(final T resource) {
                dispatchOnDelete(resource);
            }
        });
    }

    @Override
    public Indexer<T> getIndexer() {
        return indexer;
    }

    @Override
    public void addIndex(final String indexName, final IndexFunc<T> indexFunc) {
        if (started.get()) {
            throw new IllegalStateException("Cannot add index after informer has started");
        }
        indexer.addIndex(indexName, indexFunc);
    }

    @Override
    public boolean hasSynced() {
        return delegate.hasSynced();
    }

    @Override
    public boolean isRunning() {
        return delegate.isRunning();
    }

    @Override
    public EventHandlerRegistration<T> addEventHandler(final ResourceEventHandler<T> handler) {
        return addEventHandler(handler, ResourceEventHandlerOptions.defaults());
    }

    @Override
    public EventHandlerRegistration<T> addEventHandler(
            final ResourceEventHandler<T> handler,
            final ResourceEventHandlerOptions options) {

        if (handler == null) {
            throw new IllegalArgumentException("Handler cannot be null");
        }

        final ResourceEventHandlerOptions effectiveOptions = options != null
                ? options
                : ResourceEventHandlerOptions.defaults();

        final DefaultEventHandlerRegistration<T> registration =
                new DefaultEventHandlerRegistration<>(handler, effectiveOptions);

        handlersLock.writeLock().lock();
        try {
            handlers.add(registration);

            if (delegate.hasSynced()) {
                replayExistingResources(registration);
            }

            scheduleResyncIfNeeded(registration);
        } finally {
            handlersLock.writeLock().unlock();
        }

        log.debug("Added event handler, total handlers: {}", handlers.size());
        return registration;
    }

    @Override
    public void removeEventHandler(final EventHandlerRegistration<T> registration) {
        if (!(registration instanceof DefaultEventHandlerRegistration)) {
            return;
        }

        final DefaultEventHandlerRegistration<T> reg = (DefaultEventHandlerRegistration<T>) registration;

        handlersLock.writeLock().lock();
        try {
            handlers.remove(reg);
            final ScheduledFuture<?> future = resyncFutures.remove(reg);
            if (future != null) {
                future.cancel(false);
            }
            reg.deactivate();
        } finally {
            handlersLock.writeLock().unlock();
        }

        log.debug("Removed event handler, remaining handlers: {}", handlers.size());
    }

    @Override
    public String getLastSyncResourceVersion() {
        return lastResourceVersion.get();
    }

    @Override
    public void run() {
        if (!started.compareAndSet(false, true)) {
            log.warn("SharedInformer already started");
            return;
        }

        if (stopped.get()) {
            throw new IllegalStateException("Cannot restart a stopped SharedInformer");
        }

        initResyncExecutor();
        delegate.start();
        log.info("SharedInformer started");
    }

    @Override
    public void shutdown() {
        if (!stopped.compareAndSet(false, true)) {
            return;
        }

        log.info("Shutting down SharedInformer...");

        delegate.stop();

        if (resyncExecutor != null) {
            resyncExecutor.shutdownNow();
            try {
                resyncExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        handlersLock.writeLock().lock();
        try {
            for (final DefaultEventHandlerRegistration<T> reg : handlers) {
                reg.deactivate();
            }
            handlers.clear();
            resyncFutures.clear();
        } finally {
            handlersLock.writeLock().unlock();
        }

        log.info("SharedInformer shutdown complete");
    }

    private void initResyncExecutor() {
        if (resyncExecutor == null) {
            resyncExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                final Thread thread = new Thread(r, "shared-informer-resync");
                thread.setDaemon(true);
                return thread;
            });
        }
    }

    private void replayExistingResources(final DefaultEventHandlerRegistration<T> registration) {
        final ResourceEventHandler<T> handler = registration.getHandler();
        for (final T resource : indexer.list()) {
            try {
                handler.onAdd(resource);
            } catch (Exception e) {
                log.error("Error replaying existing resource to handler", e);
            }
        }
    }

    private void scheduleResyncIfNeeded(final DefaultEventHandlerRegistration<T> registration) {
        Duration resyncPeriod = registration.getResyncPeriod();
        if (resyncPeriod.isZero() || resyncPeriod.isNegative()) {
            resyncPeriod = defaultResyncPeriod;
        }

        if (resyncPeriod.isZero() || resyncPeriod.isNegative()) {
            return;
        }

        if (resyncExecutor == null) {
            return;
        }

        final long periodMillis = resyncPeriod.toMillis();
        final ScheduledFuture<?> future = resyncExecutor.scheduleAtFixedRate(
                () -> performResync(registration),
                periodMillis,
                periodMillis,
                TimeUnit.MILLISECONDS
        );
        resyncFutures.put(registration, future);
    }

    private void performResync(final DefaultEventHandlerRegistration<T> registration) {
        if (!registration.isActive() || !delegate.hasSynced()) {
            return;
        }

        final ResourceEventHandler<T> handler = registration.getHandler();
        log.debug("Performing resync for handler");

        for (final T resource : indexer.list()) {
            try {
                handler.onUpdate(resource, resource);
            } catch (Exception e) {
                log.error("Error during resync", e);
            }
        }
    }

    private void dispatchOnAdd(final T resource) {
        handlersLock.readLock().lock();
        try {
            for (final DefaultEventHandlerRegistration<T> registration : handlers) {
                if (registration.isActive()) {
                    try {
                        registration.getHandler().onAdd(resource);
                    } catch (Exception e) {
                        log.error("Error in handler onAdd", e);
                    }
                }
            }
        } finally {
            handlersLock.readLock().unlock();
        }
    }

    private void dispatchOnUpdate(final T oldResource, final T newResource) {
        handlersLock.readLock().lock();
        try {
            for (final DefaultEventHandlerRegistration<T> registration : handlers) {
                if (registration.isActive()) {
                    try {
                        registration.getHandler().onUpdate(oldResource, newResource);
                    } catch (Exception e) {
                        log.error("Error in handler onUpdate", e);
                    }
                }
            }
        } finally {
            handlersLock.readLock().unlock();
        }
    }

    private void dispatchOnDelete(final T resource) {
        handlersLock.readLock().lock();
        try {
            for (final DefaultEventHandlerRegistration<T> registration : handlers) {
                if (registration.isActive()) {
                    try {
                        registration.getHandler().onDelete(resource);
                    } catch (Exception e) {
                        log.error("Error in handler onDelete", e);
                    }
                }
            }
        } finally {
            handlersLock.readLock().unlock();
        }
    }
}
