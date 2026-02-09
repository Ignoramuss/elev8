package io.elev8.reactor;

import io.elev8.core.patch.ApplyOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.patch.PatchType;
import io.elev8.core.watch.WatchEvent;
import io.elev8.core.watch.WatchEventType;
import io.elev8.core.watch.WatchOptions;
import io.elev8.core.watch.Watcher;
import io.elev8.resources.ClusterResourceManager;
import io.elev8.resources.ResourceException;
import io.elev8.resources.namespace.Namespace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractReactiveClusterResourceManagerTest {

    @Mock
    private ClusterResourceManager<Namespace> delegate;

    private AbstractReactiveClusterResourceManager<Namespace> manager;

    @BeforeEach
    void setUp() {
        manager = new AbstractReactiveClusterResourceManager<>(delegate);
    }

    @Test
    void shouldListClusterResources() throws ResourceException {
        final Namespace ns1 = Namespace.builder().name("ns1").build();
        final Namespace ns2 = Namespace.builder().name("ns2").build();
        doReturn(List.of(ns1, ns2)).when(delegate).list();

        StepVerifier.create(manager.list())
                .assertNext(namespaces -> {
                    assertThat(namespaces).hasSize(2);
                    assertThat(namespaces).containsExactly(ns1, ns2);
                })
                .verifyComplete();

        verify(delegate).list();
    }

    @Test
    void shouldGetClusterResource() throws ResourceException {
        final Namespace namespace = Namespace.builder().name("production").build();
        doReturn(namespace).when(delegate).get("production");

        StepVerifier.create(manager.get("production"))
                .assertNext(ns -> {
                    assertThat(ns.getName()).isEqualTo("production");
                })
                .verifyComplete();

        verify(delegate).get("production");
    }

    @Test
    void shouldCreateClusterResource() throws ResourceException {
        final Namespace namespace = Namespace.builder().name("new-ns").build();
        doReturn(namespace).when(delegate).create(namespace);

        StepVerifier.create(manager.create(namespace))
                .assertNext(ns -> {
                    assertThat(ns.getName()).isEqualTo("new-ns");
                })
                .verifyComplete();

        verify(delegate).create(namespace);
    }

    @Test
    void shouldUpdateClusterResource() throws ResourceException {
        final Namespace namespace = Namespace.builder().name("existing-ns").build();
        doReturn(namespace).when(delegate).update(namespace);

        StepVerifier.create(manager.update(namespace))
                .assertNext(ns -> {
                    assertThat(ns.getName()).isEqualTo("existing-ns");
                })
                .verifyComplete();

        verify(delegate).update(namespace);
    }

    @Test
    void shouldDeleteClusterResource() throws ResourceException {
        doNothing().when(delegate).delete("my-namespace");

        StepVerifier.create(manager.delete("my-namespace"))
                .verifyComplete();

        verify(delegate).delete("my-namespace");
    }

    @Test
    void shouldPatchClusterResource() throws ResourceException {
        final Namespace namespace = Namespace.builder().name("patched-ns").build();
        final PatchOptions options = PatchOptions.builder().patchType(PatchType.MERGE_PATCH).build();
        final String patchBody = "{\"metadata\":{\"labels\":{\"env\":\"prod\"}}}";

        doReturn(namespace).when(delegate).patch("my-namespace", options, patchBody);

        StepVerifier.create(manager.patch("my-namespace", options, patchBody))
                .assertNext(ns -> {
                    assertThat(ns.getName()).isEqualTo("patched-ns");
                })
                .verifyComplete();

        verify(delegate).patch("my-namespace", options, patchBody);
    }

    @Test
    void shouldApplyClusterResource() throws ResourceException {
        final Namespace namespace = Namespace.builder().name("applied-ns").build();
        final ApplyOptions options = ApplyOptions.of("my-controller");
        final String manifest = "apiVersion: v1\nkind: Namespace\nmetadata:\n  name: applied-ns";

        doReturn(namespace).when(delegate).apply("applied-ns", options, manifest);

        StepVerifier.create(manager.apply("applied-ns", options, manifest))
                .assertNext(ns -> {
                    assertThat(ns.getName()).isEqualTo("applied-ns");
                })
                .verifyComplete();

        verify(delegate).apply("applied-ns", options, manifest);
    }

    @Test
    void shouldPropagateErrorOnList() throws ResourceException {
        doThrow(new ResourceException("Connection failed")).when(delegate).list();

        StepVerifier.create(manager.list())
                .expectErrorMatches(e -> e instanceof ResourceException && e.getMessage().contains("Connection failed"))
                .verify();
    }

    @Test
    void shouldPropagateErrorOnGet() throws ResourceException {
        doThrow(new ResourceException("Not found", 404)).when(delegate).get("missing-ns");

        StepVerifier.create(manager.get("missing-ns"))
                .expectError(ResourceException.class)
                .verify();
    }

    @Test
    void shouldWatchClusterResources() throws ResourceException {
        final Namespace ns = Namespace.builder().name("watched-ns").build();
        final WatchOptions options = WatchOptions.defaults();

        doAnswer(invocation -> {
            final Watcher<Namespace> watcher = invocation.getArgument(1);
            watcher.onEvent(WatchEvent.of(WatchEventType.ADDED, ns));
            watcher.onEvent(WatchEvent.of(WatchEventType.DELETED, ns));
            watcher.onClose();
            return null;
        }).when(delegate).watch(eq(options), any());

        StepVerifier.create(manager.watch(options))
                .assertNext(event -> {
                    assertThat(event.getType()).isEqualTo(WatchEventType.ADDED);
                    assertThat(event.getObject().getName()).isEqualTo("watched-ns");
                })
                .assertNext(event -> {
                    assertThat(event.getType()).isEqualTo(WatchEventType.DELETED);
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateWatchError() throws ResourceException {
        final WatchOptions options = WatchOptions.defaults();

        doAnswer(invocation -> {
            final Watcher<Namespace> watcher = invocation.getArgument(1);
            watcher.onError(new ResourceException("Watch connection lost"));
            return null;
        }).when(delegate).watch(eq(options), any());

        StepVerifier.create(manager.watch(options))
                .expectError(ResourceException.class)
                .verify();
    }

    @Test
    void shouldReturnApiPath() {
        when(delegate.getApiPath()).thenReturn("/api/v1");

        assertThat(manager.getApiPath()).isEqualTo("/api/v1");
    }

    @Test
    void shouldReturnDelegate() {
        assertThat(manager.getDelegate()).isSameAs(delegate);
    }
}
