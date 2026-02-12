package io.elev8.reactor;

import io.elev8.core.list.ListOptions;
import io.elev8.core.patch.ApplyOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.watch.WatchEvent;
import io.elev8.core.watch.WatchOptions;
import io.elev8.core.watch.Watcher;
import io.elev8.resources.ClusterResourceManager;
import io.elev8.resources.KubernetesResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * Abstract base implementation of ReactiveClusterResourceManager that wraps a synchronous ClusterResourceManager.
 * Uses Project Reactor's Mono and Flux types with boundedElastic scheduler for non-blocking execution.
 *
 * @param <T> the resource type
 */
@Slf4j
@RequiredArgsConstructor
public class AbstractReactiveClusterResourceManager<T extends KubernetesResource>
        implements ReactiveClusterResourceManager<T> {

    private final ClusterResourceManager<T> delegate;

    @Override
    public Mono<List<T>> list() {
        return Mono.fromCallable(delegate::list)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<List<T>> list(final ListOptions options) {
        return Mono.fromCallable(() -> delegate.list(options))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<T> get(final String name) {
        return Mono.fromCallable(() -> delegate.get(name))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<T> create(final T resource) {
        return Mono.fromCallable(() -> delegate.create(resource))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<T> update(final T resource) {
        return Mono.fromCallable(() -> delegate.update(resource))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> delete(final String name) {
        return Mono.fromCallable(() -> {
            delegate.delete(name);
            return null;
        }).then().subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<T> patch(final String name, final PatchOptions options, final String patchBody) {
        return Mono.fromCallable(() -> delegate.patch(name, options, patchBody))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<T> apply(final String name, final ApplyOptions options, final String manifest) {
        return Mono.fromCallable(() -> delegate.apply(name, options, manifest))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<WatchEvent<T>> watch(final WatchOptions options) {
        return Flux.<WatchEvent<T>>create(sink -> {
            final Watcher<T> watcher = new Watcher<>() {
                @Override
                public void onEvent(final WatchEvent<T> event) {
                    if (!sink.isCancelled()) {
                        sink.next(event);
                    }
                }

                @Override
                public void onError(final Exception exception) {
                    if (!sink.isCancelled()) {
                        sink.error(exception);
                    }
                }

                @Override
                public void onClose() {
                    if (!sink.isCancelled()) {
                        sink.complete();
                    }
                }
            };

            sink.onCancel(watcher::close);
            sink.onDispose(watcher::close);

            try {
                delegate.watch(options, watcher);
            } catch (Exception e) {
                sink.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public String getApiPath() {
        return delegate.getApiPath();
    }

    /**
     * Get the underlying synchronous cluster resource manager.
     *
     * @return the delegate cluster resource manager
     */
    public ClusterResourceManager<T> getDelegate() {
        return delegate;
    }
}
