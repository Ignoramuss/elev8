package io.elev8.reactor;

import io.elev8.core.list.ListOptions;
import io.elev8.core.patch.ApplyOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.watch.WatchEvent;
import io.elev8.core.watch.WatchOptions;
import io.elev8.core.watch.Watcher;
import io.elev8.resources.KubernetesResource;
import io.elev8.resources.ResourceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * Abstract base implementation of ReactiveResourceManager that wraps a synchronous ResourceManager.
 * Uses Project Reactor's Mono and Flux types with boundedElastic scheduler for non-blocking execution.
 *
 * @param <T> the resource type
 */
@Slf4j
@RequiredArgsConstructor
public class AbstractReactiveResourceManager<T extends KubernetesResource> implements ReactiveResourceManager<T> {

    private final ResourceManager<T> delegate;

    @Override
    public Mono<List<T>> list(final String namespace) {
        return Mono.fromCallable(() -> delegate.list(namespace))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<List<T>> list(final String namespace, final ListOptions options) {
        return Mono.fromCallable(() -> delegate.list(namespace, options))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<List<T>> listAllNamespaces() {
        return Mono.fromCallable(delegate::listAllNamespaces)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<List<T>> listAllNamespaces(final ListOptions options) {
        return Mono.fromCallable(() -> delegate.listAllNamespaces(options))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<T> get(final String namespace, final String name) {
        return Mono.fromCallable(() -> delegate.get(namespace, name))
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
    public Mono<Void> delete(final String namespace, final String name) {
        return Mono.fromCallable(() -> {
            delegate.delete(namespace, name);
            return null;
        }).then().subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<T> patch(final String namespace, final String name, final PatchOptions options, final String patchBody) {
        return Mono.fromCallable(() -> delegate.patch(namespace, name, options, patchBody))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<T> apply(final String namespace, final String name, final ApplyOptions options, final String manifest) {
        return Mono.fromCallable(() -> delegate.apply(namespace, name, options, manifest))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<WatchEvent<T>> watch(final String namespace, final WatchOptions options) {
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
                delegate.watch(namespace, options, watcher);
            } catch (Exception e) {
                sink.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<WatchEvent<T>> watchAllNamespaces(final WatchOptions options) {
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
                delegate.watchAllNamespaces(options, watcher);
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
     * Get the underlying synchronous resource manager.
     *
     * @return the delegate resource manager
     */
    public ResourceManager<T> getDelegate() {
        return delegate;
    }
}
