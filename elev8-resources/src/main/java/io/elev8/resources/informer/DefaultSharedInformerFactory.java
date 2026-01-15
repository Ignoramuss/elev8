package io.elev8.resources.informer;

import io.elev8.resources.KubernetesResource;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default implementation of SharedInformerFactory.
 * Manages SharedInformers and ensures deduplication based on SharedInformerKey.
 */
@Slf4j
public class DefaultSharedInformerFactory implements SharedInformerFactory {

    private final Map<SharedInformerKey, SharedIndexInformer<?>> informers;
    private final Duration defaultResyncPeriod;
    private final AtomicBoolean started;
    private final AtomicBoolean stopped;
    private volatile ExecutorService executor;
    private volatile boolean ownsExecutor;

    /**
     * Creates a new DefaultSharedInformerFactory with no default resync.
     */
    public DefaultSharedInformerFactory() {
        this(Duration.ZERO);
    }

    /**
     * Creates a new DefaultSharedInformerFactory with the specified default resync period.
     *
     * @param defaultResyncPeriod default resync period for informers (null or zero to disable)
     */
    public DefaultSharedInformerFactory(final Duration defaultResyncPeriod) {
        this.informers = new ConcurrentHashMap<>();
        this.defaultResyncPeriod = defaultResyncPeriod != null ? defaultResyncPeriod : Duration.ZERO;
        this.started = new AtomicBoolean(false);
        this.stopped = new AtomicBoolean(false);
    }

    @Override
    public <T extends KubernetesResource> SharedInformerBuilder<T> forResource(final Class<T> resourceClass) {
        if (resourceClass == null) {
            throw new IllegalArgumentException("resourceClass cannot be null");
        }
        return new DefaultSharedInformerBuilder<>(this, resourceClass, defaultResyncPeriod);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends KubernetesResource> SharedIndexInformer<T> getExistingInformer(
            final Class<T> resourceClass,
            final String namespace) {

        final SharedInformerKey key = new SharedInformerKey(resourceClass, namespace, null, null);
        return (SharedIndexInformer<T>) informers.get(key);
    }

    @Override
    public void start() {
        start(null);
    }

    @Override
    public void start(final ExecutorService providedExecutor) {
        if (!started.compareAndSet(false, true)) {
            log.warn("SharedInformerFactory already started");
            return;
        }

        if (stopped.get()) {
            throw new IllegalStateException("Cannot restart a stopped SharedInformerFactory");
        }

        if (providedExecutor != null) {
            this.executor = providedExecutor;
            this.ownsExecutor = false;
        } else {
            this.executor = Executors.newCachedThreadPool(r -> {
                final Thread thread = new Thread(r, "shared-informer-" + System.currentTimeMillis());
                thread.setDaemon(true);
                return thread;
            });
            this.ownsExecutor = true;
        }

        log.info("Starting {} shared informers", informers.size());

        for (final SharedIndexInformer<?> informer : informers.values()) {
            executor.submit(informer::run);
        }
    }

    @Override
    public boolean waitForCacheSync(final Duration timeout) {
        if (!started.get()) {
            log.warn("Cannot wait for cache sync: factory not started");
            return false;
        }

        final Instant deadline = Instant.now().plus(timeout);

        for (final Map.Entry<SharedInformerKey, SharedIndexInformer<?>> entry : informers.entrySet()) {
            final SharedIndexInformer<?> informer = entry.getValue();

            while (!informer.hasSynced()) {
                if (Instant.now().isAfter(deadline)) {
                    log.warn("Timeout waiting for cache sync for {}", entry.getKey());
                    return false;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        log.info("All {} caches synced", informers.size());
        return true;
    }

    @Override
    public Map<SharedInformerKey, SharedIndexInformer<?>> getInformers() {
        return Collections.unmodifiableMap(informers);
    }

    @Override
    public void shutdown() {
        if (!stopped.compareAndSet(false, true)) {
            return;
        }

        log.info("Shutting down SharedInformerFactory with {} informers", informers.size());

        for (final SharedIndexInformer<?> informer : informers.values()) {
            try {
                informer.shutdown();
            } catch (Exception e) {
                log.error("Error shutting down informer", e);
            }
        }

        if (executor != null && ownsExecutor) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        informers.clear();
        log.info("SharedInformerFactory shutdown complete");
    }

    /**
     * Registers or retrieves a SharedIndexInformer for the given key.
     * If an informer with the same key already exists, it is returned.
     * Otherwise, the provided informer is registered and returned.
     *
     * @param key the informer key
     * @param informer the informer to register if none exists
     * @param <T> the resource type
     * @return the registered informer (may be a previously existing one)
     */
    @SuppressWarnings("unchecked")
    <T extends KubernetesResource> SharedIndexInformer<T> registerInformer(
            final SharedInformerKey key,
            final SharedIndexInformer<T> informer) {

        if (started.get()) {
            throw new IllegalStateException(
                    "Cannot register new informers after factory has started. " +
                    "Register all informers before calling start().");
        }

        final SharedIndexInformer<?> existing = informers.putIfAbsent(key, informer);
        if (existing != null) {
            log.debug("Returning existing informer for {}", key);
            return (SharedIndexInformer<T>) existing;
        }

        log.debug("Registered new informer for {}", key);
        return informer;
    }
}
