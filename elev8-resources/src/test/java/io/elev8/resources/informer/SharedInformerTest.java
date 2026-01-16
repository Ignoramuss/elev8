package io.elev8.resources.informer;

import io.elev8.core.watch.ResourceChangeEvent;
import io.elev8.core.watch.ResourceChangeStream;
import io.elev8.core.watch.ResourceChangeType;
import io.elev8.resources.KubernetesResource;
import io.elev8.resources.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SharedInformerTest {

    private List<TestResource> initialResources;
    private List<ResourceChangeEvent<TestResource>> streamEvents;
    private DefaultSharedIndexInformer<TestResource> informer;

    @BeforeEach
    void setUp() {
        initialResources = new ArrayList<>();
        streamEvents = new ArrayList<>();
    }

    @Nested
    class Lifecycle {
        @Test
        void shouldStartAndShutdown() throws InterruptedException {
            initialResources.add(createResource("default", "pod-1"));

            informer = createInformer();
            informer.run();

            waitForSync();

            assertThat(informer.isRunning()).isTrue();
            assertThat(informer.hasSynced()).isTrue();

            informer.shutdown();

            assertThat(informer.isRunning()).isFalse();
        }

        @Test
        void shouldIgnoreDoubleStart() throws InterruptedException {
            informer = createInformer();
            informer.run();
            waitForSync();

            informer.run();

            assertThat(informer.isRunning()).isTrue();

            informer.shutdown();
        }

        @Test
        void shouldIgnoreDoubleShutdown() {
            informer = createInformer();
            informer.run();

            informer.shutdown();
            informer.shutdown();

            assertThat(informer.isRunning()).isFalse();
        }
    }

    @Nested
    class IndexManagement {
        @Test
        void shouldAllowAddingIndexBeforeStart() {
            informer = createInformer();
            informer.addIndex("byLabel", r -> {
                final String label = r.getMetadata().getName();
                return label != null ? List.of(label) : List.of();
            });

            assertThat(informer.getIndexer().getIndexNames()).contains("byLabel");
        }

        @Test
        void shouldRejectAddingIndexAfterStart() throws InterruptedException {
            informer = createInformer();
            informer.run();
            waitForSync();

            assertThatThrownBy(() -> informer.addIndex("byLabel", r -> List.of()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("after informer has started");

            informer.shutdown();
        }

        @Test
        void shouldSupportIndexQueries() throws InterruptedException {
            initialResources.add(createResourceWithLabel("default", "pod-1", "web"));
            initialResources.add(createResourceWithLabel("default", "pod-2", "api"));
            initialResources.add(createResourceWithLabel("default", "pod-3", "web"));

            informer = createInformer();
            informer.addIndex("byLabel", TestResource::getLabel);

            informer.run();
            waitForSync();

            final List<TestResource> webPods = informer.getIndexer().getByIndex("byLabel", "web");
            assertThat(webPods).hasSize(2);

            final List<TestResource> apiPods = informer.getIndexer().getByIndex("byLabel", "api");
            assertThat(apiPods).hasSize(1);

            informer.shutdown();
        }
    }

    @Nested
    class HandlerRegistration {
        @Test
        void shouldReturnRegistrationHandle() {
            informer = createInformer();

            final ResourceEventHandler<TestResource> handler = createNoOpHandler();
            final EventHandlerRegistration<TestResource> registration = informer.addEventHandler(handler);

            assertThat(registration).isNotNull();
            assertThat(registration.getHandler()).isSameAs(handler);
            assertThat(registration.isActive()).isTrue();
        }

        @Test
        void shouldAcceptHandlerOptions() {
            informer = createInformer();

            final ResourceEventHandler<TestResource> handler = createNoOpHandler();
            final EventHandlerRegistration<TestResource> registration = informer.addEventHandler(
                    handler,
                    ResourceEventHandlerOptions.withResyncPeriod(Duration.ofMinutes(5))
            );

            assertThat(registration.getResyncPeriod()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        void shouldRejectNullHandler() {
            informer = createInformer();

            assertThatThrownBy(() -> informer.addEventHandler(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        void shouldDeactivateRegistrationOnRemoval() {
            informer = createInformer();

            final EventHandlerRegistration<TestResource> registration =
                    informer.addEventHandler(createNoOpHandler());

            assertThat(registration.isActive()).isTrue();

            informer.removeEventHandler(registration);

            assertThat(registration.isActive()).isFalse();
        }
    }

    @Nested
    class MultipleHandlers {
        @Test
        void shouldDispatchEventsToAllHandlers() throws InterruptedException {
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

            informer.run();
            latch.await(5, TimeUnit.SECONDS);

            assertThat(handler1Count.get()).isEqualTo(1);
            assertThat(handler2Count.get()).isEqualTo(1);

            informer.shutdown();
        }

        @Test
        void shouldNotDispatchToRemovedHandler() throws InterruptedException {
            initialResources.add(createResource("default", "pod-1"));

            final AtomicInteger callCount = new AtomicInteger(0);

            informer = createInformer();
            final EventHandlerRegistration<TestResource> registration = informer.addEventHandler(
                    new ResourceEventHandler<>() {
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
                    }
            );

            informer.removeEventHandler(registration);

            informer.run();
            waitForSync();

            assertThat(callCount.get()).isEqualTo(0);

            informer.shutdown();
        }
    }

    @Nested
    class LateHandlerRegistration {
        @Test
        void shouldReplayExistingResourcesToLateHandler() throws InterruptedException {
            initialResources.add(createResource("default", "pod-1"));
            initialResources.add(createResource("default", "pod-2"));

            informer = createInformer();
            informer.run();
            waitForSync();

            final CountDownLatch latch = new CountDownLatch(2);
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

            assertThat(addedResources).hasSize(2);

            informer.shutdown();
        }
    }

    @Nested
    class EventDispatching {
        @Test
        void shouldDispatchCreateEvents() throws InterruptedException {
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
                public void onUpdate(final TestResource o, final TestResource n) {
                }

                @Override
                public void onDelete(final TestResource r) {
                }
            });

            informer.run();
            latch.await(5, TimeUnit.SECONDS);

            assertThat(addedResources).contains(newResource);

            informer.shutdown();
        }

        @Test
        void shouldDispatchUpdateEvents() throws InterruptedException {
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
                public void onDelete(final TestResource r) {
                }
            });

            informer.run();
            latch.await(5, TimeUnit.SECONDS);

            assertThat(updates).hasSize(1);

            informer.shutdown();
        }

        @Test
        void shouldDispatchDeleteEvents() throws InterruptedException {
            final TestResource resource = createResource("default", "pod-1");
            initialResources.add(resource);
            streamEvents.add(createEvent(ResourceChangeType.DELETED, null, resource));

            final CountDownLatch latch = new CountDownLatch(1);
            final List<TestResource> deletedResources = new ArrayList<>();

            informer = createInformer();
            informer.addEventHandler(new ResourceEventHandler<>() {
                @Override
                public void onAdd(final TestResource r) {
                }

                @Override
                public void onUpdate(final TestResource o, final TestResource n) {
                }

                @Override
                public void onDelete(final TestResource r) {
                    deletedResources.add(r);
                    latch.countDown();
                }
            });

            informer.run();
            latch.await(5, TimeUnit.SECONDS);

            assertThat(deletedResources).contains(resource);

            informer.shutdown();
        }

        @Test
        void shouldContinueDispatchingWhenHandlerThrows() throws InterruptedException {
            initialResources.add(createResource("default", "pod-1"));

            final AtomicInteger successCount = new AtomicInteger(0);
            final CountDownLatch latch = new CountDownLatch(1);

            informer = createInformer();

            informer.addEventHandler(new ResourceEventHandler<>() {
                @Override
                public void onAdd(final TestResource resource) {
                    throw new RuntimeException("Handler error");
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
                    successCount.incrementAndGet();
                    latch.countDown();
                }

                @Override
                public void onUpdate(final TestResource o, final TestResource n) {
                }

                @Override
                public void onDelete(final TestResource r) {
                }
            });

            informer.run();
            latch.await(5, TimeUnit.SECONDS);

            assertThat(successCount.get()).isEqualTo(1);

            informer.shutdown();
        }
    }

    private DefaultSharedIndexInformer<TestResource> createInformer() {
        return new DefaultSharedIndexInformer<>(
                () -> new ArrayList<>(initialResources),
                () -> new TestResourceChangeStream(streamEvents),
                Duration.ZERO
        );
    }

    private void waitForSync() throws InterruptedException {
        for (int i = 0; i < 50 && !informer.hasSynced(); i++) {
            Thread.sleep(100);
        }
    }

    private TestResource createResource(final String namespace, final String name) {
        return new TestResource(namespace, name, null);
    }

    private TestResource createResourceWithLabel(final String namespace, final String name, final String label) {
        return new TestResource(namespace, name, label);
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

    private ResourceEventHandler<TestResource> createNoOpHandler() {
        return new ResourceEventHandler<>() {
            @Override
            public void onAdd(final TestResource resource) {
            }

            @Override
            public void onUpdate(final TestResource o, final TestResource n) {
            }

            @Override
            public void onDelete(final TestResource r) {
            }
        };
    }

    static class TestResource implements KubernetesResource {
        private final Metadata metadata;
        private final String label;

        TestResource(final String namespace, final String name, final String label) {
            this.metadata = Metadata.builder()
                    .namespace(namespace)
                    .name(name)
                    .build();
            this.label = label;
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

        List<String> getLabel() {
            return label != null ? List.of(label) : List.of();
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
