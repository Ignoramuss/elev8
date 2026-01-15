package io.elev8.resources.informer;

import io.elev8.resources.KubernetesResource;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Factory for creating and managing SharedInformers.
 * Ensures that only one informer exists per resource type/namespace/selector combination,
 * sharing the cache and watch connection across all consumers.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * SharedInformerFactory factory = new DefaultSharedInformerFactory(eksClient);
 *
 * // Get or create a shared informer for Pods in default namespace
 * SharedIndexInformer<Pod> podInformer = factory.forResource(Pod.class)
 *     .inNamespace("default")
 *     .build();
 *
 * // Add handlers (multiple consumers can add handlers to the same informer)
 * podInformer.addEventHandler(new ResourceEventHandler<Pod>() {...});
 *
 * // Start all informers
 * factory.start();
 *
 * // Wait for caches to sync
 * factory.waitForCacheSync(Duration.ofSeconds(30));
 * }</pre>
 */
public interface SharedInformerFactory extends AutoCloseable {

    /**
     * Creates a builder for a SharedIndexInformer for the given resource type.
     *
     * @param resourceClass the resource class
     * @param <T> the resource type
     * @return a builder for configuring the informer
     */
    <T extends KubernetesResource> SharedInformerBuilder<T> forResource(Class<T> resourceClass);

    /**
     * Returns an existing SharedIndexInformer if one exists for the given parameters.
     *
     * @param resourceClass the resource class
     * @param namespace the namespace (null for all namespaces or cluster-scoped)
     * @param <T> the resource type
     * @return the existing informer, or null if none exists
     */
    <T extends KubernetesResource> SharedIndexInformer<T> getExistingInformer(
            Class<T> resourceClass,
            String namespace);

    /**
     * Starts all registered informers using a default executor.
     * Informers that are already running will not be restarted.
     */
    void start();

    /**
     * Starts all registered informers using the provided executor.
     *
     * @param executor the executor service to use for running informers
     */
    void start(ExecutorService executor);

    /**
     * Waits for all registered informers to sync their caches.
     *
     * @param timeout maximum time to wait
     * @return true if all caches synced within the timeout
     */
    boolean waitForCacheSync(Duration timeout);

    /**
     * Returns a map of all registered informers.
     *
     * @return unmodifiable map of informer keys to informers
     */
    Map<SharedInformerKey, SharedIndexInformer<?>> getInformers();

    /**
     * Shuts down all informers gracefully.
     */
    void shutdown();

    /**
     * {@inheritDoc}
     */
    @Override
    default void close() {
        shutdown();
    }
}
