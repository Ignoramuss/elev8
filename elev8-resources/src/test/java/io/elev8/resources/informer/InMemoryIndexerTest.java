package io.elev8.resources.informer;

import io.elev8.resources.KubernetesResource;
import io.elev8.resources.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryIndexerTest {

    private InMemoryIndexer<TestResource> indexer;

    @BeforeEach
    void setUp() {
        indexer = new InMemoryIndexer<>();
    }

    @Nested
    class IndexManagement {
        @Test
        void shouldAddIndex() {
            indexer.addIndex("byLabel", r -> List.of(r.getLabel()));

            assertThat(indexer.getIndexNames()).containsExactly("byLabel");
        }

        @Test
        void shouldAddMultipleIndices() {
            indexer.addIndex("byLabel", r -> List.of(r.getLabel()));
            indexer.addIndex("byNamespace", r -> List.of(r.getNamespace()));

            assertThat(indexer.getIndexNames()).containsExactlyInAnyOrder("byLabel", "byNamespace");
        }

        @Test
        void shouldThrowOnDuplicateIndex() {
            indexer.addIndex("byLabel", r -> List.of(r.getLabel()));

            assertThatThrownBy(() -> indexer.addIndex("byLabel", r -> List.of(r.getLabel())))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        void shouldThrowOnNullIndexName() {
            assertThatThrownBy(() -> indexer.addIndex(null, r -> List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        void shouldThrowOnEmptyIndexName() {
            assertThatThrownBy(() -> indexer.addIndex("", r -> List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null or empty");
        }

        @Test
        void shouldThrowOnNullIndexFunc() {
            assertThatThrownBy(() -> indexer.addIndex("byLabel", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        void shouldIndexExistingResourcesWhenIndexAdded() {
            final TestResource r1 = createResource("default", "pod-1", "web");
            final TestResource r2 = createResource("default", "pod-2", "api");
            indexer.add(r1);
            indexer.add(r2);

            indexer.addIndex("byLabel", r -> List.of(r.getLabel()));

            assertThat(indexer.getByIndex("byLabel", "web")).containsExactly(r1);
            assertThat(indexer.getByIndex("byLabel", "api")).containsExactly(r2);
        }
    }

    @Nested
    class IndexQueries {
        @BeforeEach
        void setUpIndex() {
            indexer.addIndex("byLabel", r -> List.of(r.getLabel()));
        }

        @Test
        void shouldGetByIndex() {
            final TestResource r1 = createResource("default", "pod-1", "web");
            final TestResource r2 = createResource("default", "pod-2", "web");
            final TestResource r3 = createResource("default", "pod-3", "api");
            indexer.add(r1);
            indexer.add(r2);
            indexer.add(r3);

            final List<TestResource> webResources = indexer.getByIndex("byLabel", "web");

            assertThat(webResources).containsExactlyInAnyOrder(r1, r2);
        }

        @Test
        void shouldReturnEmptyListForNoMatches() {
            indexer.add(createResource("default", "pod-1", "web"));

            assertThat(indexer.getByIndex("byLabel", "nonexistent")).isEmpty();
        }

        @Test
        void shouldGetIndexKeys() {
            final TestResource r1 = createResource("default", "pod-1", "web");
            final TestResource r2 = createResource("kube-system", "pod-2", "web");
            indexer.add(r1);
            indexer.add(r2);

            final List<String> keys = indexer.getIndexKeys("byLabel", "web");

            assertThat(keys).containsExactlyInAnyOrder("default/pod-1", "kube-system/pod-2");
        }

        @Test
        void shouldGetAllIndexValues() {
            indexer.add(createResource("default", "pod-1", "web"));
            indexer.add(createResource("default", "pod-2", "api"));
            indexer.add(createResource("default", "pod-3", "db"));

            final Set<String> values = indexer.getAllIndexValues("byLabel");

            assertThat(values).containsExactlyInAnyOrder("web", "api", "db");
        }

        @Test
        void shouldThrowOnQueryNonexistentIndex() {
            assertThatThrownBy(() -> indexer.getByIndex("nonexistent", "value"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not exist");
        }

        @Test
        void shouldThrowOnGetIndexKeysNonexistentIndex() {
            assertThatThrownBy(() -> indexer.getIndexKeys("nonexistent", "value"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not exist");
        }

        @Test
        void shouldThrowOnGetAllIndexValuesNonexistentIndex() {
            assertThatThrownBy(() -> indexer.getAllIndexValues("nonexistent"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not exist");
        }
    }

    @Nested
    class Mutations {
        @BeforeEach
        void setUpIndex() {
            indexer.addIndex("byLabel", r -> List.of(r.getLabel()));
        }

        @Test
        void shouldUpdateIndexOnAdd() {
            final TestResource resource = createResource("default", "pod-1", "web");
            indexer.add(resource);

            assertThat(indexer.getByIndex("byLabel", "web")).containsExactly(resource);
        }

        @Test
        void shouldUpdateIndexOnUpdate() {
            final TestResource original = createResource("default", "pod-1", "web");
            indexer.add(original);

            final TestResource updated = createResource("default", "pod-1", "api");
            indexer.update(updated);

            assertThat(indexer.getByIndex("byLabel", "web")).isEmpty();
            assertThat(indexer.getByIndex("byLabel", "api")).containsExactly(updated);
        }

        @Test
        void shouldUpdateIndexOnDelete() {
            final TestResource resource = createResource("default", "pod-1", "web");
            indexer.add(resource);
            indexer.delete(resource);

            assertThat(indexer.getByIndex("byLabel", "web")).isEmpty();
        }

        @Test
        void shouldHandleNullResourceOnAdd() {
            indexer.add(null);

            assertThat(indexer.size()).isEqualTo(0);
        }

        @Test
        void shouldHandleNullResourceOnDelete() {
            indexer.delete(null);

            assertThat(indexer.size()).isEqualTo(0);
        }

        @Test
        void shouldCleanupEmptyIndexValues() {
            final TestResource r1 = createResource("default", "pod-1", "web");
            indexer.add(r1);
            indexer.delete(r1);

            assertThat(indexer.getAllIndexValues("byLabel")).isEmpty();
        }
    }

    @Nested
    class BulkOperations {
        @BeforeEach
        void setUpIndex() {
            indexer.addIndex("byLabel", r -> List.of(r.getLabel()));
        }

        @Test
        void shouldRebuildIndicesOnReplace() {
            indexer.add(createResource("default", "pod-1", "old"));
            indexer.add(createResource("default", "pod-2", "old"));

            final List<TestResource> newResources = List.of(
                    createResource("default", "pod-3", "new"),
                    createResource("default", "pod-4", "new")
            );
            indexer.replace(newResources);

            assertThat(indexer.getByIndex("byLabel", "old")).isEmpty();
            assertThat(indexer.getByIndex("byLabel", "new")).hasSize(2);
        }

        @Test
        void shouldClearIndicesOnClear() {
            indexer.add(createResource("default", "pod-1", "web"));
            indexer.add(createResource("default", "pod-2", "api"));
            indexer.clear();

            assertThat(indexer.getAllIndexValues("byLabel")).isEmpty();
        }

        @Test
        void shouldHandleNullListOnReplace() {
            indexer.add(createResource("default", "pod-1", "web"));
            indexer.replace(null);

            assertThat(indexer.size()).isEqualTo(0);
            assertThat(indexer.getAllIndexValues("byLabel")).isEmpty();
        }
    }

    @Nested
    class MultiValueIndices {
        @Test
        void shouldHandleMultipleIndexValues() {
            indexer.addIndex("byTags", r -> r.getTags());

            final TestResource resource = createResourceWithTags("default", "pod-1", List.of("tag1", "tag2", "tag3"));
            indexer.add(resource);

            assertThat(indexer.getByIndex("byTags", "tag1")).containsExactly(resource);
            assertThat(indexer.getByIndex("byTags", "tag2")).containsExactly(resource);
            assertThat(indexer.getByIndex("byTags", "tag3")).containsExactly(resource);
        }

        @Test
        void shouldHandleEmptyIndexValues() {
            indexer.addIndex("byTags", r -> r.getTags());

            final TestResource resource = createResourceWithTags("default", "pod-1", List.of());
            indexer.add(resource);

            assertThat(indexer.getAllIndexValues("byTags")).isEmpty();
        }

        @Test
        void shouldHandleNullIndexValues() {
            indexer.addIndex("byOptional", r -> {
                if (r.getLabel().equals("special")) {
                    return null;
                }
                return List.of(r.getLabel());
            });

            indexer.add(createResource("default", "pod-1", "special"));
            indexer.add(createResource("default", "pod-2", "normal"));

            assertThat(indexer.getByIndex("byOptional", "normal")).hasSize(1);
            assertThat(indexer.getAllIndexValues("byOptional")).containsExactly("normal");
        }

        @Test
        void shouldUpdateMultiValueIndexOnDelete() {
            indexer.addIndex("byTags", r -> r.getTags());

            final TestResource resource = createResourceWithTags("default", "pod-1", List.of("tag1", "tag2"));
            indexer.add(resource);
            indexer.delete(resource);

            assertThat(indexer.getByIndex("byTags", "tag1")).isEmpty();
            assertThat(indexer.getByIndex("byTags", "tag2")).isEmpty();
        }
    }

    @Nested
    class ConcurrentAccess {
        @Test
        void shouldHandleConcurrentAdditions() throws InterruptedException {
            indexer.addIndex("byLabel", r -> List.of(r.getLabel()));

            final int threadCount = 10;
            final int operationsPerThread = 100;
            final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            final CountDownLatch latch = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                executor.submit(() -> {
                    try {
                        for (int i = 0; i < operationsPerThread; i++) {
                            indexer.add(createResource("ns-" + threadId, "pod-" + i, "label-" + (i % 10)));
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(indexer.size()).isEqualTo(threadCount * operationsPerThread);
        }

        @Test
        void shouldHandleConcurrentReadsAndWrites() throws InterruptedException {
            indexer.addIndex("byLabel", r -> List.of(r.getLabel()));

            for (int i = 0; i < 100; i++) {
                indexer.add(createResource("default", "pod-" + i, "label-" + (i % 5)));
            }

            final int threadCount = 10;
            final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            final CountDownLatch latch = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                executor.submit(() -> {
                    try {
                        for (int i = 0; i < 50; i++) {
                            if (threadId % 2 == 0) {
                                indexer.add(createResource("ns-" + threadId, "new-pod-" + i, "new-label"));
                            } else {
                                indexer.getByIndex("byLabel", "label-" + (i % 5));
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(indexer.size()).isGreaterThan(100);
        }
    }

    @Nested
    class MultipleIndices {
        @Test
        void shouldMaintainMultipleIndicesSimultaneously() {
            indexer.addIndex("byLabel", r -> List.of(r.getLabel()));
            indexer.addIndex("byNamespace", r -> List.of(r.getNamespace()));

            final TestResource r1 = createResource("ns1", "pod-1", "web");
            final TestResource r2 = createResource("ns1", "pod-2", "api");
            final TestResource r3 = createResource("ns2", "pod-3", "web");

            indexer.add(r1);
            indexer.add(r2);
            indexer.add(r3);

            assertThat(indexer.getByIndex("byLabel", "web")).containsExactlyInAnyOrder(r1, r3);
            assertThat(indexer.getByIndex("byNamespace", "ns1")).containsExactlyInAnyOrder(r1, r2);
        }

        @Test
        void shouldUpdateAllIndicesOnMutation() {
            indexer.addIndex("byLabel", r -> List.of(r.getLabel()));
            indexer.addIndex("byNamespace", r -> List.of(r.getNamespace()));

            final TestResource resource = createResource("ns1", "pod-1", "web");
            indexer.add(resource);
            indexer.delete(resource);

            assertThat(indexer.getByIndex("byLabel", "web")).isEmpty();
            assertThat(indexer.getByIndex("byNamespace", "ns1")).isEmpty();
        }
    }

    private TestResource createResource(final String namespace, final String name, final String label) {
        return new TestResource(namespace, name, label, List.of());
    }

    private TestResource createResourceWithTags(final String namespace, final String name, final List<String> tags) {
        return new TestResource(namespace, name, "default", tags);
    }

    static class TestResource implements KubernetesResource {
        private final Metadata metadata;
        private final String label;
        private final List<String> tags;

        TestResource(final String namespace, final String name, final String label, final List<String> tags) {
            this.metadata = Metadata.builder()
                    .namespace(namespace)
                    .name(name)
                    .build();
            this.label = label;
            this.tags = new ArrayList<>(tags);
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

        public String getLabel() {
            return label;
        }

        public List<String> getTags() {
            return tags;
        }
    }
}
