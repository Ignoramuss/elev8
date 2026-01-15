package io.elev8.resources.informer;

import io.elev8.core.watch.ResourceChangeEvent;
import io.elev8.core.watch.ResourceChangeStream;
import io.elev8.core.watch.StreamOptions;
import io.elev8.resources.KubernetesResource;
import io.elev8.resources.Metadata;
import io.elev8.resources.ResourceException;
import io.elev8.resources.ResourceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SharedInformerFactoryTest {

    private DefaultSharedInformerFactory factory;
    private ResourceManager<TestResource> mockManager;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws ResourceException {
        factory = new DefaultSharedInformerFactory();
        mockManager = mock(ResourceManager.class);
        when(mockManager.list(any())).thenReturn(List.of());
        when(mockManager.listAllNamespaces()).thenReturn(List.of());
        when(mockManager.stream(any(), any())).thenReturn(new EmptyStream());
        when(mockManager.streamAllNamespaces(any())).thenReturn(new EmptyStream());
    }

    @Nested
    class InformerCreation {
        @Test
        void shouldCreateInformerForNamespace() {
            final SharedIndexInformer<TestResource> informer = factory.forResource(TestResource.class)
                    .inNamespace("default")
                    .withResourceManager(mockManager)
                    .build();

            assertThat(informer).isNotNull();
            assertThat(factory.getInformers()).hasSize(1);
        }

        @Test
        void shouldCreateInformerForAllNamespaces() {
            final SharedIndexInformer<TestResource> informer = factory.forResource(TestResource.class)
                    .inAllNamespaces()
                    .withResourceManager(mockManager)
                    .build();

            assertThat(informer).isNotNull();
            assertThat(factory.getInformers()).hasSize(1);
        }

        @Test
        void shouldRejectNullResourceClass() {
            assertThatThrownBy(() -> factory.forResource(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        void shouldRejectEmptyNamespace() {
            assertThatThrownBy(() ->
                    factory.forResource(TestResource.class)
                            .inNamespace("")
                            .withResourceManager(mockManager)
                            .build()
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldRejectMissingManager() {
            assertThatThrownBy(() ->
                    factory.forResource(TestResource.class)
                            .inNamespace("default")
                            .build()
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ResourceManager");
        }
    }

    @Nested
    class InformerDeduplication {
        @Test
        void shouldReturnSameInformerForSameKey() {
            final SharedIndexInformer<TestResource> informer1 = factory.forResource(TestResource.class)
                    .inNamespace("default")
                    .withResourceManager(mockManager)
                    .build();

            final SharedIndexInformer<TestResource> informer2 = factory.forResource(TestResource.class)
                    .inNamespace("default")
                    .withResourceManager(mockManager)
                    .build();

            assertThat(informer1).isSameAs(informer2);
            assertThat(factory.getInformers()).hasSize(1);
        }

        @Test
        void shouldCreateDifferentInformersForDifferentNamespaces() {
            final SharedIndexInformer<TestResource> informer1 = factory.forResource(TestResource.class)
                    .inNamespace("default")
                    .withResourceManager(mockManager)
                    .build();

            final SharedIndexInformer<TestResource> informer2 = factory.forResource(TestResource.class)
                    .inNamespace("kube-system")
                    .withResourceManager(mockManager)
                    .build();

            assertThat(informer1).isNotSameAs(informer2);
            assertThat(factory.getInformers()).hasSize(2);
        }

        @Test
        void shouldCreateDifferentInformersForDifferentSelectors() {
            final SharedIndexInformer<TestResource> informer1 = factory.forResource(TestResource.class)
                    .inNamespace("default")
                    .withLabelSelector("app=web")
                    .withResourceManager(mockManager)
                    .build();

            final SharedIndexInformer<TestResource> informer2 = factory.forResource(TestResource.class)
                    .inNamespace("default")
                    .withLabelSelector("app=api")
                    .withResourceManager(mockManager)
                    .build();

            assertThat(informer1).isNotSameAs(informer2);
            assertThat(factory.getInformers()).hasSize(2);
        }

        @Test
        void shouldReturnExistingInformer() {
            factory.forResource(TestResource.class)
                    .inNamespace("default")
                    .withResourceManager(mockManager)
                    .build();

            final SharedIndexInformer<TestResource> existing =
                    factory.getExistingInformer(TestResource.class, "default");

            assertThat(existing).isNotNull();
        }

        @Test
        void shouldReturnNullForNonExistingInformer() {
            final SharedIndexInformer<TestResource> existing =
                    factory.getExistingInformer(TestResource.class, "non-existent");

            assertThat(existing).isNull();
        }
    }

    @Nested
    class FactoryLifecycle {
        @Test
        void shouldStartAllInformers() throws InterruptedException {
            factory.forResource(TestResource.class)
                    .inNamespace("default")
                    .withResourceManager(mockManager)
                    .build();

            factory.forResource(TestResource.class)
                    .inNamespace("kube-system")
                    .withResourceManager(mockManager)
                    .build();

            factory.start();

            Thread.sleep(200);

            for (final SharedIndexInformer<?> informer : factory.getInformers().values()) {
                assertThat(informer.isRunning()).isTrue();
            }

            factory.shutdown();
        }

        @Test
        void shouldStartWithCustomExecutor() throws InterruptedException {
            final ExecutorService executor = Executors.newFixedThreadPool(2);

            factory.forResource(TestResource.class)
                    .inNamespace("default")
                    .withResourceManager(mockManager)
                    .build();

            factory.start(executor);

            Thread.sleep(200);

            assertThat(factory.getInformers().values().iterator().next().isRunning()).isTrue();

            factory.shutdown();
            executor.shutdownNow();
        }

        @Test
        void shouldWaitForCacheSync() throws InterruptedException {
            factory.forResource(TestResource.class)
                    .inNamespace("default")
                    .withResourceManager(mockManager)
                    .build();

            factory.start();

            final boolean synced = factory.waitForCacheSync(Duration.ofSeconds(5));

            assertThat(synced).isTrue();

            factory.shutdown();
        }

        @Test
        void shouldShutdownAllInformers() throws InterruptedException {
            factory.forResource(TestResource.class)
                    .inNamespace("default")
                    .withResourceManager(mockManager)
                    .build();

            factory.start();
            Thread.sleep(200);
            factory.shutdown();

            for (final SharedIndexInformer<?> informer : factory.getInformers().values()) {
                assertThat(informer.isRunning()).isFalse();
            }
        }

        @Test
        void shouldRejectNewInformersAfterStart() throws InterruptedException {
            factory.forResource(TestResource.class)
                    .inNamespace("default")
                    .withResourceManager(mockManager)
                    .build();

            factory.start();
            Thread.sleep(100);

            assertThatThrownBy(() ->
                    factory.forResource(TestResource.class)
                            .inNamespace("new-namespace")
                            .withResourceManager(mockManager)
                            .build()
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("after factory has started");

            factory.shutdown();
        }
    }

    @Nested
    class SharedInformerKeyTests {
        @Test
        void keysWithSameParametersShouldBeEqual() {
            final SharedInformerKey key1 = new SharedInformerKey(
                    TestResource.class, "default", "app=web", null);
            final SharedInformerKey key2 = new SharedInformerKey(
                    TestResource.class, "default", "app=web", null);

            assertThat(key1).isEqualTo(key2);
            assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
        }

        @Test
        void keysWithDifferentNamespacesShouldNotBeEqual() {
            final SharedInformerKey key1 = new SharedInformerKey(
                    TestResource.class, "default", null, null);
            final SharedInformerKey key2 = new SharedInformerKey(
                    TestResource.class, "kube-system", null, null);

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        void keyToStringShouldBeReadable() {
            final SharedInformerKey key = new SharedInformerKey(
                    TestResource.class, "default", "app=web", "status.phase=Running");

            final String str = key.toString();
            assertThat(str).contains("TestResource");
            assertThat(str).contains("default");
            assertThat(str).contains("app=web");
        }
    }

    @Nested
    class ResourceEventHandlerOptionsTests {
        @Test
        void defaultsShouldHaveNoResync() {
            final ResourceEventHandlerOptions options = ResourceEventHandlerOptions.defaults();

            assertThat(options.getResyncPeriod()).isEqualTo(Duration.ZERO);
            assertThat(options.hasResync()).isFalse();
        }

        @Test
        void shouldConfigureResyncPeriod() {
            final ResourceEventHandlerOptions options =
                    ResourceEventHandlerOptions.withResyncPeriod(Duration.ofMinutes(5));

            assertThat(options.getResyncPeriod()).isEqualTo(Duration.ofMinutes(5));
            assertThat(options.hasResync()).isTrue();
        }

        @Test
        void nullResyncPeriodShouldDefaultToZero() {
            final ResourceEventHandlerOptions options =
                    ResourceEventHandlerOptions.withResyncPeriod(null);

            assertThat(options.getResyncPeriod()).isEqualTo(Duration.ZERO);
        }
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

    static class EmptyStream extends ResourceChangeStream<TestResource> {
        private volatile boolean closed = false;

        EmptyStream() {
            super(() -> {});
        }

        @Override
        public boolean hasNext() {
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
            return null;
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
