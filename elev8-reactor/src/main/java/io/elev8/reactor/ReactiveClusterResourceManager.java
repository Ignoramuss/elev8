package io.elev8.reactor;

import io.elev8.core.patch.ApplyOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.watch.WatchEvent;
import io.elev8.core.watch.WatchOptions;
import io.elev8.resources.KubernetesResource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Reactive interface for managing cluster-scoped Kubernetes resources with non-blocking operations.
 * Cluster-scoped resources (like PersistentVolume, Namespace, ClusterRole) exist at the cluster level.
 *
 * @param <T> the resource type
 */
public interface ReactiveClusterResourceManager<T extends KubernetesResource> {

    /**
     * List all resources in the cluster.
     *
     * @return Mono emitting the list of resources
     */
    Mono<List<T>> list();

    /**
     * Get a specific resource by name.
     *
     * @param name the resource name
     * @return Mono emitting the resource
     */
    Mono<T> get(String name);

    /**
     * Create a new resource.
     *
     * @param resource the resource to create
     * @return Mono emitting the created resource
     */
    Mono<T> create(T resource);

    /**
     * Update an existing resource.
     *
     * @param resource the resource to update
     * @return Mono emitting the updated resource
     */
    Mono<T> update(T resource);

    /**
     * Delete a resource.
     *
     * @param name the resource name
     * @return Mono completing when the resource is deleted
     */
    Mono<Void> delete(String name);

    /**
     * Patch an existing resource with partial updates.
     *
     * @param name the resource name
     * @param options patch options specifying patch type and behavior
     * @param patchBody the patch content
     * @return Mono emitting the patched resource
     */
    Mono<T> patch(String name, PatchOptions options, String patchBody);

    /**
     * Apply a resource configuration declaratively using Server-side Apply.
     *
     * @param name the resource name
     * @param options apply options with required fieldManager
     * @param manifest the resource manifest (JSON or YAML format)
     * @return Mono emitting the applied resource
     */
    Mono<T> apply(String name, ApplyOptions options, String manifest);

    /**
     * Watch cluster-scoped resources for changes.
     * Returns a Flux that emits watch events as resources change.
     *
     * @param options watch options for configuring the watch behavior
     * @return Flux emitting watch events
     */
    Flux<WatchEvent<T>> watch(WatchOptions options);

    /**
     * Get the API path for this resource type.
     *
     * @return the API path
     */
    String getApiPath();
}
