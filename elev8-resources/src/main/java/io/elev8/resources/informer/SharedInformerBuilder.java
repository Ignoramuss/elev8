package io.elev8.resources.informer;

import io.elev8.resources.ClusterResourceManager;
import io.elev8.resources.KubernetesResource;
import io.elev8.resources.ResourceManager;

import java.time.Duration;

/**
 * Builder for configuring and creating SharedIndexInformers.
 * If an informer with the same configuration already exists, it will be returned
 * instead of creating a new one.
 *
 * @param <T> the resource type
 */
public interface SharedInformerBuilder<T extends KubernetesResource> {

    /**
     * Configures the informer to watch a specific namespace.
     *
     * @param namespace the namespace to watch
     * @return this builder
     */
    SharedInformerBuilder<T> inNamespace(String namespace);

    /**
     * Configures the informer to watch all namespaces.
     * This is the default behavior for namespace-scoped resources.
     *
     * @return this builder
     */
    SharedInformerBuilder<T> inAllNamespaces();

    /**
     * Configures the informer with a label selector.
     *
     * @param labelSelector the label selector (e.g., "app=nginx,env=prod")
     * @return this builder
     */
    SharedInformerBuilder<T> withLabelSelector(String labelSelector);

    /**
     * Configures the informer with a field selector.
     *
     * @param fieldSelector the field selector (e.g., "status.phase=Running")
     * @return this builder
     */
    SharedInformerBuilder<T> withFieldSelector(String fieldSelector);

    /**
     * Configures the default resync period for handlers that don't specify one.
     *
     * @param resyncPeriod the resync period (zero or null to disable)
     * @return this builder
     */
    SharedInformerBuilder<T> withResyncPeriod(Duration resyncPeriod);

    /**
     * Uses a custom ResourceManager for namespace-scoped API operations.
     *
     * @param manager the resource manager
     * @return this builder
     */
    SharedInformerBuilder<T> withResourceManager(ResourceManager<T> manager);

    /**
     * Uses a custom ClusterResourceManager for cluster-scoped resources.
     *
     * @param manager the cluster resource manager
     * @return this builder
     */
    SharedInformerBuilder<T> withClusterResourceManager(ClusterResourceManager<T> manager);

    /**
     * Builds or retrieves the SharedIndexInformer.
     * If an informer already exists for this configuration, it is returned.
     * Otherwise, a new one is created and registered with the factory.
     *
     * @return the shared index informer
     */
    SharedIndexInformer<T> build();
}
