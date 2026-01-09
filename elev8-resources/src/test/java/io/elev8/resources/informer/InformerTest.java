package io.elev8.resources.informer;

import io.elev8.core.watch.ResourceChangeEvent;
import io.elev8.core.watch.ResourceChangeStream;
import io.elev8.core.watch.ResourceChangeType;
import io.elev8.resources.KubernetesResource;
import io.elev8.resources.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InformerTest {

    private List<TestResource> initialResources;
    private List<ResourceChangeEvent<TestResource>> streamEvents;
    private Informer<TestResource> informer;

    @BeforeEach
    void setUp() {
        initialResources = new ArrayList<>();
        streamEvents = new ArrayList<>();
    }

    @Nested
    class Lifecycle {
        @Test
        void shouldStartAndStop() throws InterruptedException {
            initialResources.add(createResource("default", "pod-1"));

            informer = createInformer();
            informer.start();

            waitForSync();

            assertThat(informer.isRunning()).isTrue();
            assertThat(informer.hasSynced()).isTrue();

            informer.stop();

            assertThat(informer.isRunning()).isFalse();
        }

        @Test
        void shouldThrowWhenStartedTwice() throws InterruptedException {
            informer = createInformer();
            informer.start();
            waitForSync();

            assertThatThrownBy(() -> informer.start())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already running");

            informer.stop();
        }

        @Test
        void shouldAllowCloseMultipleTimes() {
            informer = createInformer();
            informer.start();

            informer.close();
            informer.close();

            assertThat(informer.isRunning()).isFalse();
        }
    }

    @Nested
    class InitialSync {
        @Test
        void shouldPopulateStoreWithInitialList() throws InterruptedException {
            initialResources.add(createResource("default", "pod-1"));
            initialResources.add(createResource("default", "pod-2"));

            informer = createInformer();
            informer.start();
            waitForSync();

            assertThat(informer.getStore().size()).isEqualTo(2);
            assertThat(informer.getStore().get("default", "pod-1")).isNotNull();
            assertThat(informer.getStore().get("default", "pod-2")).isNotNull();

            informer.stop();
        }

        @Test
        void shouldFireOnAddForInitialResources() throws InterruptedException {
            initialResources.add(createResource("default", "pod-1"));
            initialResources.add(createResource("default", "pod-2"));

            final List<TestResource> addedResources = new ArrayList<>();
            final CountDownLatch latch = new CountDownLatch(2);

            informer = createInformer();
            informer.addEventHandler(new ResourceEventHandler<>() {
                @Override
                public void onAdd(final TestResource resource) {
                    addedResources.add(resource);
                    latch.countDown();
                }

                @Override
                public void onUpdate(final TestResource oldResource, final TestResource newResource) {
                }

                @Override
                public void onDelete(final TestResource resource) {
                }
            });

            informer.start();
            latch.await(5, TimeUnit.SECONDS);

            assertThat(addedResources).hasSize(2);

            informer.stop();
        }

        @Test
        void shouldSetHasSyncedAfterInitialList() throws InterruptedException {
            informer = createInformer();

            assertThat(informer.hasSynced()).isFalse();

            informer.start();
            waitForSync();

            assertThat(informer.hasSynced()).isTrue();

            informer.stop();
        }
    }

    @Nested
    class EventHandling {
        @Test
        void shouldHandleCreatedEvent() throws InterruptedException {
            final TestResource newResource = createResource("default", "new-pod");
            streamEvents.add(createEvent(ResourceChangeType.CREATED, newResource, null));

            final CountDownLatch latch = new CountDownLatch(1);
            final List<TestResource> addedResources = new ArrayList<>();

            informer = createInformer();
            informer.addEventHandler(new ResourceEventHandler<>() {
                @Override
                public void onAdd(final TestResource resource) {
                    addedResources.add(resource);
                    latch.countDown();
                }

                @Override
                public void onUpdate(final TestResource oldResource, final TestResource newResource) {
                }

                @Override
                public void onDelete(final TestResource resource) {
                }
            });

            informer.start();
            latch.await(5, TimeUnit.SECONDS);

            assertThat(addedResources).contains(newResource);
            assertThat(informer.getStore().get("default", "new-pod")).isNotNull();

            informer.stop();
        }

        @Test
        void shouldHandleUpdatedEvent() throws InterruptedException {
            final TestResource oldResource = createResource("default", "pod-1");
            final TestResource newResource = createResource("default", "pod-1");
            initialResources.add(oldResource);
            streamEvents.add(createEvent(ResourceChangeType.UPDATED, newResource, oldResource));

            final CountDownLatch latch = new CountDownLatch(1);
            final List<TestResource[]> updates = new ArrayList<>();

            informer = createInformer();
            informer.addEventHandler(new ResourceEventHandler<>() {
                @Override
                public void onAdd(final TestResource resource) {
                }

                @Override
                public void onUpdate(final TestResource old, final TestResource newRes) {
                    updates.add(new TestResource[]{old, newRes});
                    latch.countDown();
                }

                @Override
                public void onDelete(final TestResource resource) {
                }
            });

            informer.start();
            latch.await(5, TimeUnit.SECONDS);

            assertThat(updates).hasSize(1);

            informer.stop();
        }

        @Test
        void shouldHandleDeletedEvent() throws InterruptedException {
            final TestResource resource = createResource("default", "pod-1");
            initialResources.add(resource);
            streamEvents.add(createEvent(ResourceChangeType.DELETED, null, resource));

            final CountDownLatch deleteLatch = new CountDownLatch(1);
            final List<TestResource> deletedResources = new ArrayList<>();

            informer = createInformer();
            informer.addEventHandler(new ResourceEventHandler<>() {
                @Override
                public void onAdd(final TestResource res) {
                }

                @Override
                public void onUpdate(final TestResource old, final TestResource newRes) {
                }

                @Override
                public void onDelete(final TestResource res) {
                    deletedResources.add(res);
                    deleteLatch.countDown();
                }
            });

            informer.start();
            deleteLatch.await(5, TimeUnit.SECONDS);

            assertThat(deletedResources).contains(resource);
            assertThat(informer.getStore().get("default", "pod-1")).isNull();

            informer.stop();
        }
    }

    @Nested
    class MultipleHandlers {
        @Test
        void shouldNotifyAllHandlers() throws InterruptedException {
            initialResources.add(createResource("default", "pod-1"));

            final AtomicInteger handler1Count = new AtomicInteger(0);
            final AtomicInteger handler2Count = new AtomicInteger(0);
            final CountDownLatch latch = new CountDownLatch(2);

            informer = createInformer();
            informer.addEventHandler(new ResourceEventHandler<>() {
                @Override
                public void onAdd(final TestResource resource) {
                    handler1Count.incrementAndGet();
                    latch.countDown();
                }

                @Override
                public void onUpdate(final TestResource o, final TestResource n) {
                }

                @Override
                public void onDelete(final TestResource r) {
                }
            });
            informer.addEventHandler(new ResourceEventHandler<>() {
                @Override
                public void onAdd(final TestResource resource) {
                    handler2Count.incrementAndGet();
                    latch.countDown();
                }

                @Override
                public void onUpdate(final TestResource o, final TestResource n) {
                }

                @Override
                public void onDelete(final TestResource r) {
                }
            });

            informer.start();
            latch.await(5, TimeUnit.SECONDS);

            assertThat(handler1Count.get()).isEqualTo(1);
            assertThat(handler2Count.get()).isEqualTo(1);

            informer.stop();
        }

        @Test
        void shouldRemoveHandler() throws InterruptedException {
            initialResources.add(createResource("default", "pod-1"));

            final AtomicInteger callCount = new AtomicInteger(0);

            final ResourceEventHandler<TestResource> handler = new ResourceEventHandler<>() {
                @Override
                public void onAdd(final TestResource resource) {
                    callCount.incrementAndGet();
                }

                @Override
                public void onUpdate(final TestResource o, final TestResource n) {
                }

                @Override
                public void onDelete(final TestResource r) {
                }
            };

            informer = createInformer();
            informer.addEventHandler(handler);
            informer.removeEventHandler(handler);

            informer.start();
            waitForSync();

            assertThat(callCount.get()).isEqualTo(0);

            informer.stop();
        }
    }

    @Nested
    class LateHandlerRegistration {
        @Test
        void shouldFireOnAddForExistingResourcesWhenHandlerAddedAfterSync() throws InterruptedException {
            initialResources.add(createResource("default", "pod-1"));

            informer = createInformer();
            informer.start();
            waitForSync();

            final CountDownLatch latch = new CountDownLatch(1);
            final List<TestResource> addedResources = new ArrayList<>();

            informer.addEventHandler(new ResourceEventHandler<>() {
                @Override
                public void onAdd(final TestResource resource) {
                    addedResources.add(resource);
                    latch.countDown();
                }

                @Override
                public void onUpdate(final TestResource o, final TestResource n) {
                }

                @Override
                public void onDelete(final TestResource r) {
                }
            });

            latch.await(2, TimeUnit.SECONDS);

            assertThat(addedResources).hasSize(1);

            informer.stop();
        }
    }

    private Informer<TestResource> createInformer() {
        return new Informer<>(
                () -> new ArrayList<>(initialResources),
                () -> new TestResourceChangeStream(streamEvents)
        );
    }

    private void waitForSync() throws InterruptedException {
        for (int i = 0; i < 50 && !informer.hasSynced(); i++) {
            Thread.sleep(100);
        }
    }

    private TestResource createResource(final String namespace, final String name) {
        return new TestResource(namespace, name);
    }

    private ResourceChangeEvent<TestResource> createEvent(
            final ResourceChangeType type,
            final TestResource resource,
            final TestResource previousResource) {
        return ResourceChangeEvent.<TestResource>builder()
                .type(type)
                .resource(resource)
                .previousResource(previousResource)
                .timestamp(Instant.now())
                .build();
    }

    static class TestResource implements KubernetesResource {
        private final Metadata metadata;

        TestResource(final String namespace, final String name) {
            this.metadata = Metadata.builder()
                    .namespace(namespace)
                    .name(name)
                    .build();
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
            return metadata;
        }

        @Override
        public String toJson() {
            return "{}";
        }
    }

    static class TestResourceChangeStream extends ResourceChangeStream<TestResource> {
        private final Iterator<ResourceChangeEvent<TestResource>> eventIterator;
        private volatile boolean closed = false;

        TestResourceChangeStream(final List<ResourceChangeEvent<TestResource>> events) {
            super(() -> {});
            this.eventIterator = new ArrayList<>(events).iterator();
        }

        @Override
        public boolean hasNext() {
            if (closed) {
                return false;
            }
            if (eventIterator.hasNext()) {
                return true;
            }
            while (!closed) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return false;
        }

        @Override
        public ResourceChangeEvent<TestResource> next() {
            return eventIterator.next();
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
