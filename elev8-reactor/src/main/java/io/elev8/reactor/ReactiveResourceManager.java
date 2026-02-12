package io.elev8.reactor;

import io.elev8.core.list.ListOptions;
import io.elev8.core.patch.ApplyOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.watch.WatchEvent;
import io.elev8.core.watch.WatchOptions;
import io.elev8.resources.KubernetesResource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Reactive interface for managing Kubernetes resources with non-blocking operations.
 * All operations return reactive types (Mono or Flux) for async/non-blocking execution.
 *
 * @param <T> the resource type
 */
public interface ReactiveResourceManager<T extends KubernetesResource> {

    /**
     * List all resources in the namespace.
     *
     * @param namespace the namespace to list resources from
     * @return Mono emitting the list of resources
     */
    Mono<List<T>> list(String namespace);

    /**
     * List all resources across all namespaces.
     *
     * @return Mono emitting the list of resources
     */
    /**
     * List all resources in the namespace with filtering and pagination options.
     *
     * @param namespace the namespace to list resources from
     * @param options list options for filtering and pagination
     * @return Mono emitting the list of resources
     */
    Mono<List<T>> list(String namespace, ListOptions options);

    Mono<List<T>> listAllNamespaces();

    /**
     * List all resources across all namespaces with filtering and pagination options.
     *
     * @param options list options for filtering and pagination
     * @return Mono emitting the list of resources
     */
    Mono<List<T>> listAllNamespaces(ListOptions options);

    /**
     * Get a specific resource by name.
     *
     * @param namespace the namespace
     * @param name the resource name
     * @return Mono emitting the resource
     */
    Mono<T> get(String namespace, String name);

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
     * @param namespace the namespace
     * @param name the resource name
     * @return Mono completing when the resource is deleted
     */
    Mono<Void> delete(String namespace, String name);

    /**
     * Patch an existing resource with partial updates.
     *
     * @param namespace the namespace
     * @param name the resource name
     * @param options patch options specifying patch type and behavior
     * @param patchBody the patch content
     * @return Mono emitting the patched resource
     */
    Mono<T> patch(String namespace, String name, PatchOptions options, String patchBody);

    /**
     * Apply a resource configuration declaratively using Server-side Apply.
     *
     * @param namespace the namespace
     * @param name the resource name
     * @param options apply options with required fieldManager
     * @param manifest the resource manifest (JSON or YAML format)
     * @return Mono emitting the applied resource
     */
    Mono<T> apply(String namespace, String name, ApplyOptions options, String manifest);

    /**
     * Watch resources in a specific namespace for changes.
     * Returns a Flux that emits watch events as resources change.
     *
     * @param namespace the namespace to watch resources in
     * @param options watch options for configuring the watch behavior
     * @return Flux emitting watch events
     */
    Flux<WatchEvent<T>> watch(String namespace, WatchOptions options);

    /**
     * Watch resources across all namespaces for changes.
     * Returns a Flux that emits watch events as resources change.
     *
     * @param options watch options for configuring the watch behavior
     * @return Flux emitting watch events
     */
    Flux<WatchEvent<T>> watchAllNamespaces(WatchOptions options);

    /**
     * Get the API path for this resource type.
     *
     * @return the API path
     */
    String getApiPath();
}
