package io.elev8.reactor;

import io.elev8.core.patch.ApplyOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.patch.PatchType;
import io.elev8.core.watch.WatchEvent;
import io.elev8.core.watch.WatchEventType;
import io.elev8.core.watch.WatchOptions;
import io.elev8.core.watch.Watcher;
import io.elev8.resources.KubernetesResource;
import io.elev8.resources.Metadata;
import io.elev8.resources.ResourceException;
import io.elev8.resources.ResourceManager;
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
class AbstractReactiveResourceManagerTest {

    @Mock
    private ResourceManager<TestResource> delegate;

    @Mock
    private TestResource resource1;

    @Mock
    private TestResource resource2;

    private AbstractReactiveResourceManager<TestResource> manager;

    @BeforeEach
    void setUp() {
        manager = new AbstractReactiveResourceManager<>(delegate);
    }

    @Test
    void shouldListResourcesInNamespace() throws ResourceException {
        doReturn(List.of(resource1, resource2)).when(delegate).list("default");

        StepVerifier.create(manager.list("default"))
                .assertNext(resources -> {
                    assertThat(resources).hasSize(2);
                    assertThat(resources).containsExactly(resource1, resource2);
                })
                .verifyComplete();

        verify(delegate).list("default");
    }

    @Test
    void shouldListAllNamespaces() throws ResourceException {
        doReturn(List.of(resource1, resource2)).when(delegate).listAllNamespaces();

        StepVerifier.create(manager.listAllNamespaces())
                .assertNext(resources -> {
                    assertThat(resources).hasSize(2);
                })
                .verifyComplete();

        verify(delegate).listAllNamespaces();
    }

    @Test
    void shouldGetResource() throws ResourceException {
        when(resource1.getName()).thenReturn("my-resource");
        doReturn(resource1).when(delegate).get("default", "my-resource");

        StepVerifier.create(manager.get("default", "my-resource"))
                .assertNext(r -> {
                    assertThat(r.getName()).isEqualTo("my-resource");
                })
                .verifyComplete();

        verify(delegate).get("default", "my-resource");
    }

    @Test
    void shouldCreateResource() throws ResourceException {
        when(resource1.getName()).thenReturn("new-resource");
        doReturn(resource1).when(delegate).create(resource1);

        StepVerifier.create(manager.create(resource1))
                .assertNext(r -> {
                    assertThat(r.getName()).isEqualTo("new-resource");
                })
                .verifyComplete();

        verify(delegate).create(resource1);
    }

    @Test
    void shouldUpdateResource() throws ResourceException {
        when(resource1.getName()).thenReturn("existing-resource");
        doReturn(resource1).when(delegate).update(resource1);

        StepVerifier.create(manager.update(resource1))
                .assertNext(r -> {
                    assertThat(r.getName()).isEqualTo("existing-resource");
                })
                .verifyComplete();

        verify(delegate).update(resource1);
    }

    @Test
    void shouldDeleteResource() throws ResourceException {
        doNothing().when(delegate).delete("default", "my-resource");

        StepVerifier.create(manager.delete("default", "my-resource"))
                .verifyComplete();

        verify(delegate).delete("default", "my-resource");
    }

    @Test
    void shouldPatchResource() throws ResourceException {
        when(resource1.getName()).thenReturn("patched-resource");
        final PatchOptions options = PatchOptions.builder().patchType(PatchType.MERGE_PATCH).build();
        final String patchBody = "{\"metadata\":{\"labels\":{\"env\":\"prod\"}}}";

        doReturn(resource1).when(delegate).patch("default", "my-resource", options, patchBody);

        StepVerifier.create(manager.patch("default", "my-resource", options, patchBody))
                .assertNext(r -> {
                    assertThat(r.getName()).isEqualTo("patched-resource");
                })
                .verifyComplete();

        verify(delegate).patch("default", "my-resource", options, patchBody);
    }

    @Test
    void shouldApplyResource() throws ResourceException {
        when(resource1.getName()).thenReturn("applied-resource");
        final ApplyOptions options = ApplyOptions.of("my-controller");
        final String manifest = "apiVersion: v1\nkind: Pod\nmetadata:\n  name: applied-resource";

        doReturn(resource1).when(delegate).apply("default", "applied-resource", options, manifest);

        StepVerifier.create(manager.apply("default", "applied-resource", options, manifest))
                .assertNext(r -> {
                    assertThat(r.getName()).isEqualTo("applied-resource");
                })
                .verifyComplete();

        verify(delegate).apply("default", "applied-resource", options, manifest);
    }

    @Test
    void shouldPropagateErrorOnList() throws ResourceException {
        doThrow(new ResourceException("Connection failed")).when(delegate).list("default");

        StepVerifier.create(manager.list("default"))
                .expectErrorMatches(e -> e instanceof ResourceException && e.getMessage().contains("Connection failed"))
                .verify();
    }

    @Test
    void shouldPropagateErrorOnGet() throws ResourceException {
        doThrow(new ResourceException("Not found", 404)).when(delegate).get("default", "missing-resource");

        StepVerifier.create(manager.get("default", "missing-resource"))
                .expectError(ResourceException.class)
                .verify();
    }

    @Test
    void shouldWatchResources() throws ResourceException {
        when(resource1.getName()).thenReturn("watched-resource");
        final WatchOptions options = WatchOptions.defaults();

        doAnswer(invocation -> {
            final Watcher<TestResource> watcher = invocation.getArgument(2);
            watcher.onEvent(WatchEvent.of(WatchEventType.ADDED, resource1));
            watcher.onEvent(WatchEvent.of(WatchEventType.MODIFIED, resource1));
            watcher.onClose();
            return null;
        }).when(delegate).watch(eq("default"), eq(options), any());

        StepVerifier.create(manager.watch("default", options))
                .assertNext(event -> {
                    assertThat(event.getType()).isEqualTo(WatchEventType.ADDED);
                    assertThat(event.getObject().getName()).isEqualTo("watched-resource");
                })
                .assertNext(event -> {
                    assertThat(event.getType()).isEqualTo(WatchEventType.MODIFIED);
                })
                .verifyComplete();
    }

    @Test
    void shouldWatchAllNamespaces() throws ResourceException {
        when(resource1.getNamespace()).thenReturn("ns1");
        when(resource2.getNamespace()).thenReturn("ns2");
        final WatchOptions options = WatchOptions.defaults();

        doAnswer(invocation -> {
            final Watcher<TestResource> watcher = invocation.getArgument(1);
            watcher.onEvent(WatchEvent.of(WatchEventType.ADDED, resource1));
            watcher.onEvent(WatchEvent.of(WatchEventType.ADDED, resource2));
            watcher.onClose();
            return null;
        }).when(delegate).watchAllNamespaces(eq(options), any());

        StepVerifier.create(manager.watchAllNamespaces(options))
                .assertNext(event -> {
                    assertThat(event.getObject().getNamespace()).isEqualTo("ns1");
                })
                .assertNext(event -> {
                    assertThat(event.getObject().getNamespace()).isEqualTo("ns2");
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateWatchError() throws ResourceException {
        final WatchOptions options = WatchOptions.defaults();

        doAnswer(invocation -> {
            final Watcher<TestResource> watcher = invocation.getArgument(2);
            watcher.onError(new ResourceException("Watch connection lost"));
            return null;
        }).when(delegate).watch(eq("default"), eq(options), any());

        StepVerifier.create(manager.watch("default", options))
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

    interface TestResource extends KubernetesResource {
    }
}
