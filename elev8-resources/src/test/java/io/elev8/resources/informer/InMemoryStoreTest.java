package io.elev8.resources.informer;

import io.elev8.resources.KubernetesResource;
import io.elev8.resources.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryStoreTest {

    private InMemoryStore<TestResource> store;

    @BeforeEach
    void setUp() {
        store = new InMemoryStore<>();
    }

    @Nested
    class AddOperations {
        @Test
        void shouldAddResource() {
            final TestResource resource = createResource("default", "test-pod");
            store.add(resource);

            assertThat(store.size()).isEqualTo(1);
            assertThat(store.get("default", "test-pod")).isEqualTo(resource);
        }

        @Test
        void shouldReplaceExistingResource() {
            final TestResource resource1 = createResource("default", "test-pod");
            final TestResource resource2 = createResource("default", "test-pod");

            store.add(resource1);
            store.add(resource2);

            assertThat(store.size()).isEqualTo(1);
            assertThat(store.get("default", "test-pod")).isEqualTo(resource2);
        }

        @Test
        void shouldHandleNullResource() {
            store.add(null);
            assertThat(store.size()).isEqualTo(0);
        }
    }

    @Nested
    class GetOperations {
        @Test
        void shouldGetByNamespaceAndName() {
            final TestResource resource = createResource("default", "test-pod");
            store.add(resource);

            assertThat(store.get("default", "test-pod")).isEqualTo(resource);
        }

        @Test
        void shouldReturnNullForNonexistentResource() {
            assertThat(store.get("default", "nonexistent")).isNull();
        }

        @Test
        void shouldGetByKey() {
            final TestResource resource = createResource("default", "test-pod");
            store.add(resource);

            assertThat(store.getByKey("default/test-pod")).isEqualTo(resource);
        }

        @Test
        void shouldReturnNullForNullKey() {
            assertThat(store.getByKey(null)).isNull();
        }

        @Test
        void shouldReturnNullForNullName() {
            assertThat(store.get("default", null)).isNull();
        }
    }

    @Nested
    class ClusterScopedResources {
        @Test
        void shouldHandleClusterScopedResource() {
            final TestResource resource = createClusterResource("my-namespace");
            store.add(resource);

            assertThat(store.size()).isEqualTo(1);
            assertThat(store.get(null, "my-namespace")).isEqualTo(resource);
            assertThat(store.getByKey("my-namespace")).isEqualTo(resource);
        }
    }

    @Nested
    class DeleteOperations {
        @Test
        void shouldDeleteResource() {
            final TestResource resource = createResource("default", "test-pod");
            store.add(resource);
            store.delete(resource);

            assertThat(store.size()).isEqualTo(0);
            assertThat(store.get("default", "test-pod")).isNull();
        }

        @Test
        void shouldHandleDeleteNonexistent() {
            final TestResource resource = createResource("default", "nonexistent");
            store.delete(resource);

            assertThat(store.size()).isEqualTo(0);
        }

        @Test
        void shouldHandleDeleteNull() {
            store.delete(null);
            assertThat(store.size()).isEqualTo(0);
        }
    }

    @Nested
    class ListOperations {
        @Test
        void shouldListAllResources() {
            store.add(createResource("default", "pod-1"));
            store.add(createResource("default", "pod-2"));
            store.add(createResource("kube-system", "pod-3"));

            final List<TestResource> resources = store.list();

            assertThat(resources).hasSize(3);
        }

        @Test
        void shouldListAllKeys() {
            store.add(createResource("default", "pod-1"));
            store.add(createResource("kube-system", "pod-2"));

            final List<String> keys = store.listKeys();

            assertThat(keys).containsExactlyInAnyOrder("default/pod-1", "kube-system/pod-2");
        }

        @Test
        void shouldReturnEmptyListWhenEmpty() {
            assertThat(store.list()).isEmpty();
            assertThat(store.listKeys()).isEmpty();
        }
    }

    @Nested
    class ReplaceOperations {
        @Test
        void shouldReplaceAllResources() {
            store.add(createResource("default", "old-pod"));

            final List<TestResource> newResources = List.of(
                    createResource("default", "new-pod-1"),
                    createResource("default", "new-pod-2")
            );
            store.replace(newResources);

            assertThat(store.size()).isEqualTo(2);
            assertThat(store.get("default", "old-pod")).isNull();
            assertThat(store.get("default", "new-pod-1")).isNotNull();
            assertThat(store.get("default", "new-pod-2")).isNotNull();
        }

        @Test
        void shouldHandleReplaceWithNull() {
            store.add(createResource("default", "pod"));
            store.replace(null);

            assertThat(store.size()).isEqualTo(0);
        }

        @Test
        void shouldHandleReplaceWithEmptyList() {
            store.add(createResource("default", "pod"));
            store.replace(List.of());

            assertThat(store.size()).isEqualTo(0);
        }
    }

    @Nested
    class ContainsKeyOperations {
        @Test
        void shouldReturnTrueForExistingKey() {
            store.add(createResource("default", "pod"));
            assertThat(store.containsKey("default/pod")).isTrue();
        }

        @Test
        void shouldReturnFalseForNonexistentKey() {
            assertThat(store.containsKey("default/nonexistent")).isFalse();
        }

        @Test
        void shouldReturnFalseForNullKey() {
            assertThat(store.containsKey(null)).isFalse();
        }
    }

    @Nested
    class ClearOperations {
        @Test
        void shouldClearAllResources() {
            store.add(createResource("default", "pod-1"));
            store.add(createResource("default", "pod-2"));
            store.clear();

            assertThat(store.size()).isEqualTo(0);
            assertThat(store.list()).isEmpty();
        }
    }

    @Nested
    class ConcurrencyTests {
        @Test
        void shouldHandleConcurrentAccess() throws InterruptedException {
            final int threads = 10;
            final int operationsPerThread = 100;
            final ExecutorService executor = Executors.newFixedThreadPool(threads);
            final CountDownLatch latch = new CountDownLatch(threads);

            for (int t = 0; t < threads; t++) {
                final int threadId = t;
                executor.submit(() -> {
                    try {
                        for (int i = 0; i < operationsPerThread; i++) {
                            final TestResource resource = createResource("ns-" + threadId, "pod-" + i);
                            store.add(resource);
                            store.get("ns-" + threadId, "pod-" + i);
                            store.list();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(store.size()).isEqualTo(threads * operationsPerThread);
        }
    }

    @Nested
    class KeyForStaticMethod {
        @Test
        void shouldGenerateKeyForNamespacedResource() {
            assertThat(InMemoryStore.keyFor("default", "pod")).isEqualTo("default/pod");
        }

        @Test
        void shouldGenerateKeyForClusterResource() {
            assertThat(InMemoryStore.keyFor(null, "namespace")).isEqualTo("namespace");
        }
    }

    private TestResource createResource(final String namespace, final String name) {
        return new TestResource(namespace, name);
    }

    private TestResource createClusterResource(final String name) {
        return new TestResource(null, name);
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
}
