package io.elev8.resources;

import io.elev8.resources.informer.Informer;
import io.elev8.resources.informer.InformerOptions;
import io.elev8.core.watch.ResourceChangeStream;
import io.elev8.core.watch.StreamOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InformersTest {

    @Nested
    class ForNamespace {
        private ResourceManager<TestResource> manager;

        @BeforeEach
        @SuppressWarnings("unchecked")
        void setUp() {
            manager = mock(ResourceManager.class);
        }

        @Test
        void shouldCreateInformerForNamespace() throws ResourceException {
            when(manager.list("default")).thenReturn(List.of());
            when(manager.stream(eq("default"), any(StreamOptions.class)))
                    .thenReturn(mock(ResourceChangeStream.class));

            final Informer<TestResource> informer = Informers.forNamespace(
                    manager, "default", InformerOptions.defaults()
            );

            assertThat(informer).isNotNull();
        }

        @Test
        void shouldThrowOnNullManager() {
            assertThatThrownBy(() -> Informers.forNamespace(null, "default", InformerOptions.defaults()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ResourceManager cannot be null");
        }

        @Test
        void shouldThrowOnNullNamespace() {
            assertThatThrownBy(() -> Informers.forNamespace(manager, null, InformerOptions.defaults()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Namespace cannot be null");
        }

        @Test
        void shouldThrowOnEmptyNamespace() {
            assertThatThrownBy(() -> Informers.forNamespace(manager, "", InformerOptions.defaults()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Namespace cannot be null or empty");
        }

        @Test
        void shouldHandleNullOptions() throws ResourceException {
            when(manager.list("default")).thenReturn(List.of());
            when(manager.stream(eq("default"), any(StreamOptions.class)))
                    .thenReturn(mock(ResourceChangeStream.class));

            final Informer<TestResource> informer = Informers.forNamespace(manager, "default", null);

            assertThat(informer).isNotNull();
        }
    }

    @Nested
    class ForAllNamespaces {
        private ResourceManager<TestResource> manager;

        @BeforeEach
        @SuppressWarnings("unchecked")
        void setUp() {
            manager = mock(ResourceManager.class);
        }

        @Test
        void shouldCreateInformerForAllNamespaces() throws ResourceException {
            when(manager.listAllNamespaces()).thenReturn(List.of());
            when(manager.streamAllNamespaces(any(StreamOptions.class)))
                    .thenReturn(mock(ResourceChangeStream.class));

            final Informer<TestResource> informer = Informers.forAllNamespaces(
                    manager, InformerOptions.defaults()
            );

            assertThat(informer).isNotNull();
        }

        @Test
        void shouldThrowOnNullManager() {
            assertThatThrownBy(() -> Informers.forAllNamespaces(null, InformerOptions.defaults()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ResourceManager cannot be null");
        }
    }

    @Nested
    class ForClusterResource {
        private ClusterResourceManager<TestResource> manager;

        @BeforeEach
        @SuppressWarnings("unchecked")
        void setUp() {
            manager = mock(ClusterResourceManager.class);
        }

        @Test
        void shouldCreateInformerForClusterResource() throws ResourceException {
            when(manager.list()).thenReturn(List.of());
            when(manager.stream(any(StreamOptions.class)))
                    .thenReturn(mock(ResourceChangeStream.class));

            final Informer<TestResource> informer = Informers.forClusterResource(
                    manager, InformerOptions.defaults()
            );

            assertThat(informer).isNotNull();
        }

        @Test
        void shouldThrowOnNullManager() {
            assertThatThrownBy(() -> Informers.forClusterResource(null, InformerOptions.defaults()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ClusterResourceManager cannot be null");
        }
    }

    @Nested
    class ExceptionHandling {
        @Test
        @SuppressWarnings("unchecked")
        void shouldFailToSyncWhenListThrowsResourceException() throws ResourceException, InterruptedException {
            final ResourceManager<TestResource> manager = mock(ResourceManager.class);
            when(manager.list("default")).thenThrow(new ResourceException("API error"));

            final Informer<TestResource> informer = Informers.forNamespace(
                    manager, "default", InformerOptions.defaults()
            );

            informer.addEventHandler(new io.elev8.resources.informer.ResourceEventHandler<>() {
                @Override public void onAdd(TestResource r) {}
                @Override public void onUpdate(TestResource o, TestResource n) {}
                @Override public void onDelete(TestResource r) {}
            });

            try {
                informer.start();

                for (int i = 0; i < 50 && informer.isRunning(); i++) {
                    Thread.sleep(100);
                }

                assertThat(informer.hasSynced()).isFalse();
                assertThat(informer.getStore().size()).isEqualTo(0);
            } finally {
                informer.stop();
            }
        }
    }

    static class TestResource extends AbstractResource {
        TestResource() {
            super("v1", "TestResource", null);
        }
    }
}
