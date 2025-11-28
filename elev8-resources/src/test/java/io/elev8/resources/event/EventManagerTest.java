package io.elev8.resources.event;

import io.elev8.core.client.KubernetesClient;
import io.elev8.core.watch.WatchOptions;
import io.elev8.core.watch.Watcher;
import io.elev8.resources.ObjectReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

class EventManagerTest {

    private KubernetesClient client;
    private EventManager manager;

    @BeforeEach
    void setUp() {
        client = Mockito.mock(KubernetesClient.class);
        manager = new EventManager(client);
    }

    @Test
    void shouldInitializeWithCorrectApiPath() {
        assertThat(manager).isNotNull();
        assertThat(manager.getApiPath()).isEqualTo("/api/v1");
    }

    @Test
    void shouldReturnCorrectResourceTypePlural() {
        assertThat(manager.getResourceTypePlural()).isEqualTo("events");
    }

    @Test
    void shouldWatchWithEventWatchOptions() throws Exception {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);
        final EventWatchOptions options = EventWatchOptions.builder()
                .eventType(Event.TYPE_WARNING)
                .build();

        manager.watch("default", options, watcher);

        final ArgumentCaptor<WatchOptions> optionsCaptor = ArgumentCaptor.forClass(WatchOptions.class);
        verify(client).watch(anyString(), optionsCaptor.capture(), any());

        final WatchOptions capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions.getFieldSelector()).isEqualTo("type=Warning");
    }

    @Test
    void shouldWatchAllNamespacesWithEventWatchOptions() throws Exception {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);
        final EventWatchOptions options = EventWatchOptions.builder()
                .eventType(Event.TYPE_NORMAL)
                .reason("Created")
                .build();

        manager.watchAllNamespaces(options, watcher);

        final ArgumentCaptor<WatchOptions> optionsCaptor = ArgumentCaptor.forClass(WatchOptions.class);
        verify(client).watch(anyString(), optionsCaptor.capture(), any());

        final WatchOptions capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions.getFieldSelector())
                .contains("type=Normal")
                .contains("reason=Created");
    }

    @Test
    void shouldWatchForObject() throws Exception {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);
        final ObjectReference ref = ObjectReference.builder()
                .kind("Deployment")
                .name("my-deployment")
                .namespace("production")
                .build();

        manager.watchForObject("production", ref, watcher);

        final ArgumentCaptor<WatchOptions> optionsCaptor = ArgumentCaptor.forClass(WatchOptions.class);
        verify(client).watch(anyString(), optionsCaptor.capture(), any());

        final WatchOptions capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions.getFieldSelector())
                .contains("involvedObject.kind=Deployment")
                .contains("involvedObject.name=my-deployment")
                .contains("involvedObject.namespace=production");
    }

    @Test
    void shouldWatchForPod() throws Exception {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);

        manager.watchForPod("default", "my-pod", watcher);

        final ArgumentCaptor<WatchOptions> optionsCaptor = ArgumentCaptor.forClass(WatchOptions.class);
        verify(client).watch(anyString(), optionsCaptor.capture(), any());

        final WatchOptions capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions.getFieldSelector())
                .contains("involvedObject.kind=Pod")
                .contains("involvedObject.name=my-pod")
                .contains("involvedObject.namespace=default");
    }

    @Test
    void shouldWatchWarnings() throws Exception {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);

        manager.watchWarnings("production", watcher);

        final ArgumentCaptor<WatchOptions> optionsCaptor = ArgumentCaptor.forClass(WatchOptions.class);
        verify(client).watch(anyString(), optionsCaptor.capture(), any());

        final WatchOptions capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions.getFieldSelector()).isEqualTo("type=Warning");
    }

    @Test
    void shouldWatchByReason() throws Exception {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);

        manager.watchByReason("default", "FailedScheduling", watcher);

        final ArgumentCaptor<WatchOptions> optionsCaptor = ArgumentCaptor.forClass(WatchOptions.class);
        verify(client).watch(anyString(), optionsCaptor.capture(), any());

        final WatchOptions capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions.getFieldSelector()).isEqualTo("reason=FailedScheduling");
    }

    @Test
    void shouldWatchByController() throws Exception {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);

        manager.watchByController("kube-system", "kubelet", watcher);

        final ArgumentCaptor<WatchOptions> optionsCaptor = ArgumentCaptor.forClass(WatchOptions.class);
        verify(client).watch(anyString(), optionsCaptor.capture(), any());

        final WatchOptions capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions.getFieldSelector()).isEqualTo("source.component=kubelet");
    }

    @Test
    void shouldBuildCorrectWatchPathForNamespace() throws Exception {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);

        manager.watchWarnings("my-namespace", watcher);

        final ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).watch(pathCaptor.capture(), any(), any());

        assertThat(pathCaptor.getValue()).isEqualTo("/api/v1/namespaces/my-namespace/events");
    }

    @Test
    void shouldThrowExceptionForNullNamespaceInWatch() {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);
        final EventWatchOptions options = EventWatchOptions.defaults();

        assertThatThrownBy(() -> manager.watch(null, options, watcher))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Namespace cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullOptionsInWatch() {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);

        assertThatThrownBy(() -> manager.watch("default", (EventWatchOptions) null, watcher))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EventWatchOptions cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullWatcherInWatch() {
        final EventWatchOptions options = EventWatchOptions.defaults();

        assertThatThrownBy(() -> manager.watch("default", options, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Watcher cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullOptionsInWatchAllNamespaces() {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);

        assertThatThrownBy(() -> manager.watchAllNamespaces((EventWatchOptions) null, watcher))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EventWatchOptions cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullWatcherInWatchAllNamespaces() {
        final EventWatchOptions options = EventWatchOptions.defaults();

        assertThatThrownBy(() -> manager.watchAllNamespaces(options, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Watcher cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullNamespaceInWatchForPod() {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);

        assertThatThrownBy(() -> manager.watchForPod(null, "my-pod", watcher))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Namespace cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullPodNameInWatchForPod() {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);

        assertThatThrownBy(() -> manager.watchForPod("default", null, watcher))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pod name cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullWatcherInWatchForPod() {
        assertThatThrownBy(() -> manager.watchForPod("default", "my-pod", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Watcher cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullNamespaceInWatchWarnings() {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);

        assertThatThrownBy(() -> manager.watchWarnings(null, watcher))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Namespace cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullWatcherInWatchWarnings() {
        assertThatThrownBy(() -> manager.watchWarnings("default", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Watcher cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullNamespaceInWatchByReason() {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);

        assertThatThrownBy(() -> manager.watchByReason(null, "Created", watcher))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Namespace cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullWatcherInWatchByReason() {
        assertThatThrownBy(() -> manager.watchByReason("default", "Created", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Watcher cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullNamespaceInWatchByController() {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);

        assertThatThrownBy(() -> manager.watchByController(null, "kubelet", watcher))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Namespace cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullControllerInWatchByController() {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);

        assertThatThrownBy(() -> manager.watchByController("default", null, watcher))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Controller cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullWatcherInWatchByController() {
        assertThatThrownBy(() -> manager.watchByController("default", "kubelet", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Watcher cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullNamespaceInWatchForObject() {
        @SuppressWarnings("unchecked")
        final Watcher<Event> watcher = Mockito.mock(Watcher.class);
        final ObjectReference ref = ObjectReference.builder()
                .kind("Pod")
                .name("my-pod")
                .build();

        assertThatThrownBy(() -> manager.watchForObject(null, ref, watcher))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Namespace cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullWatcherInWatchForObject() {
        final ObjectReference ref = ObjectReference.builder()
                .kind("Pod")
                .name("my-pod")
                .build();

        assertThatThrownBy(() -> manager.watchForObject("default", ref, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Watcher cannot be null");
    }
}
