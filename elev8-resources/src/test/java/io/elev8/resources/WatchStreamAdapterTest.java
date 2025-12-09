package io.elev8.resources;

import io.elev8.core.watch.ResourceChangeEvent;
import io.elev8.core.watch.ResourceChangeStream;
import io.elev8.core.watch.ResourceChangeType;
import io.elev8.core.watch.WatchEvent;
import io.elev8.core.watch.WatchEventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class WatchStreamAdapterTest {

    private ResourceChangeStream<TestResource> stream;
    private WatchStreamAdapter<TestResource> adapter;

    @BeforeEach
    void setUp() {
        stream = new ResourceChangeStream<>(null);
    }

    @AfterEach
    void tearDown() {
        if (stream != null && !stream.isClosed()) {
            stream.close();
        }
    }

    @Nested
    class WithStateTracking {

        @BeforeEach
        void setUp() {
            adapter = new WatchStreamAdapter<>(stream, true);
        }

        @Test
        void shouldConvertAddedEventToCreated() throws InterruptedException {
            final TestResource resource = new TestResource("default", "pod-1");
            final WatchEvent<TestResource> event = WatchEvent.of(WatchEventType.ADDED, resource);

            adapter.onEvent(event);

            final ResourceChangeEvent<TestResource> result = stream.poll(1, TimeUnit.SECONDS);
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(ResourceChangeType.CREATED);
            assertThat(result.getResource()).isSameAs(resource);
            assertThat(result.getPreviousResource()).isNull();
        }

        @Test
        void shouldConvertModifiedEventToUpdated() throws InterruptedException {
            final TestResource original = new TestResource("default", "pod-1");
            final TestResource updated = new TestResource("default", "pod-1");
            adapter.onEvent(WatchEvent.of(WatchEventType.ADDED, original));
            stream.poll(1, TimeUnit.SECONDS);

            adapter.onEvent(WatchEvent.of(WatchEventType.MODIFIED, updated));

            final ResourceChangeEvent<TestResource> result = stream.poll(1, TimeUnit.SECONDS);
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(ResourceChangeType.UPDATED);
            assertThat(result.getResource()).isSameAs(updated);
            assertThat(result.getPreviousResource()).isSameAs(original);
        }

        @Test
        void shouldConvertDeletedEventToDeleted() throws InterruptedException {
            final TestResource resource = new TestResource("default", "pod-1");
            adapter.onEvent(WatchEvent.of(WatchEventType.ADDED, resource));
            stream.poll(1, TimeUnit.SECONDS);

            adapter.onEvent(WatchEvent.of(WatchEventType.DELETED, resource));

            final ResourceChangeEvent<TestResource> result = stream.poll(1, TimeUnit.SECONDS);
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(ResourceChangeType.DELETED);
            assertThat(result.getResource()).isNull();
            assertThat(result.getPreviousResource()).isSameAs(resource);
        }

        @Test
        void shouldConvertBookmarkEventToSync() throws InterruptedException {
            final WatchEvent<TestResource> event = WatchEvent.of(WatchEventType.BOOKMARK, null);

            adapter.onEvent(event);

            final ResourceChangeEvent<TestResource> result = stream.poll(1, TimeUnit.SECONDS);
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(ResourceChangeType.SYNC);
        }

        @Test
        void shouldTrackMultipleResources() throws InterruptedException {
            final TestResource pod1Original = new TestResource("default", "pod-1");
            final TestResource pod2Original = new TestResource("default", "pod-2");
            final TestResource pod1Updated = new TestResource("default", "pod-1");

            adapter.onEvent(WatchEvent.of(WatchEventType.ADDED, pod1Original));
            adapter.onEvent(WatchEvent.of(WatchEventType.ADDED, pod2Original));

            stream.poll(1, TimeUnit.SECONDS);
            stream.poll(1, TimeUnit.SECONDS);

            adapter.onEvent(WatchEvent.of(WatchEventType.MODIFIED, pod1Updated));

            final ResourceChangeEvent<TestResource> result = stream.poll(1, TimeUnit.SECONDS);
            assertThat(result.getPreviousResource()).isSameAs(pod1Original);
        }

        @Test
        void shouldTrackResourcesAcrossNamespaces() throws InterruptedException {
            final TestResource podNs1 = new TestResource("ns1", "pod-1");
            final TestResource podNs2 = new TestResource("ns2", "pod-1");
            final TestResource podNs1Updated = new TestResource("ns1", "pod-1");

            adapter.onEvent(WatchEvent.of(WatchEventType.ADDED, podNs1));
            adapter.onEvent(WatchEvent.of(WatchEventType.ADDED, podNs2));

            stream.poll(1, TimeUnit.SECONDS);
            stream.poll(1, TimeUnit.SECONDS);

            adapter.onEvent(WatchEvent.of(WatchEventType.MODIFIED, podNs1Updated));

            final ResourceChangeEvent<TestResource> result = stream.poll(1, TimeUnit.SECONDS);
            assertThat(result.getPreviousResource()).isSameAs(podNs1);
        }
    }

    @Nested
    class WithoutStateTracking {

        @BeforeEach
        void setUp() {
            adapter = new WatchStreamAdapter<>(stream, false);
        }

        @Test
        void shouldNotTrackPreviousState() throws InterruptedException {
            final TestResource original = new TestResource("default", "pod-1");
            final TestResource updated = new TestResource("default", "pod-1");

            adapter.onEvent(WatchEvent.of(WatchEventType.ADDED, original));
            stream.poll(1, TimeUnit.SECONDS);

            adapter.onEvent(WatchEvent.of(WatchEventType.MODIFIED, updated));

            final ResourceChangeEvent<TestResource> result = stream.poll(1, TimeUnit.SECONDS);
            assertThat(result.getType()).isEqualTo(ResourceChangeType.UPDATED);
            assertThat(result.getResource()).isSameAs(updated);
            assertThat(result.getPreviousResource()).isNull();
        }
    }

    @Nested
    class ErrorHandling {

        @BeforeEach
        void setUp() {
            adapter = new WatchStreamAdapter<>(stream, true);
        }

        @Test
        void shouldSetErrorOnErrorEvent() throws InterruptedException {
            adapter.onEvent(WatchEvent.of(WatchEventType.ERROR, null));

            assertThat(stream.isClosed()).isTrue();
            assertThat(stream.getError()).isNotNull();
        }

        @Test
        void shouldSetErrorOnException() {
            final Exception error = new RuntimeException("test error");

            adapter.onError(error);

            assertThat(stream.isClosed()).isTrue();
            assertThat(stream.getError()).isSameAs(error);
        }

        @Test
        void shouldHandleNullEvent() throws InterruptedException {
            adapter.onEvent(null);

            assertThat(stream.getQueueSize()).isEqualTo(0);
        }
    }

    @Nested
    class Lifecycle {

        @BeforeEach
        void setUp() {
            adapter = new WatchStreamAdapter<>(stream, true);
        }

        @Test
        void shouldCloseStreamOnClose() {
            adapter.close();

            assertThat(stream.isClosed()).isTrue();
        }

        @Test
        void shouldSignalCloseOnWatchClose() {
            adapter.onClose();

            assertThat(stream.isClosed()).isTrue();
        }
    }

    @Nested
    class ClusterScopedResources {

        @BeforeEach
        void setUp() {
            adapter = new WatchStreamAdapter<>(stream, true);
        }

        @Test
        void shouldTrackClusterScopedResources() throws InterruptedException {
            final TestResource original = new TestResource(null, "cluster-resource");
            final TestResource updated = new TestResource(null, "cluster-resource");

            adapter.onEvent(WatchEvent.of(WatchEventType.ADDED, original));
            stream.poll(1, TimeUnit.SECONDS);

            adapter.onEvent(WatchEvent.of(WatchEventType.MODIFIED, updated));

            final ResourceChangeEvent<TestResource> result = stream.poll(1, TimeUnit.SECONDS);
            assertThat(result.getPreviousResource()).isSameAs(original);
        }
    }

    private static class TestResource implements KubernetesResource {
        private final String namespace;
        private final String name;

        TestResource(final String namespace, final String name) {
            this.namespace = namespace;
            this.name = name;
        }

        @Override
        public String getApiVersion() {
            return "v1";
        }

        @Override
        public String getKind() {
            return "TestResource";
        }

        @Override
        public Metadata getMetadata() {
            return Metadata.builder()
                    .namespace(namespace)
                    .name(name)
                    .build();
        }

        @Override
        public String toJson() {
            return "{}";
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getNamespace() {
            return namespace;
        }
    }
}
