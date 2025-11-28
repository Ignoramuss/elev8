package io.elev8.resources.event;

import io.elev8.core.watch.WatchOptions;
import io.elev8.resources.ObjectReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventWatchOptionsTest {

    @Test
    void shouldBuildEmptyFieldSelector() {
        final EventWatchOptions options = EventWatchOptions.builder().build();
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getFieldSelector()).isNull();
    }

    @Test
    void shouldBuildSingleFieldSelectorForInvolvedObjectName() {
        final EventWatchOptions options = EventWatchOptions.builder()
                .involvedObjectName("my-pod")
                .build();
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getFieldSelector()).isEqualTo("involvedObject.name=my-pod");
    }

    @Test
    void shouldBuildSingleFieldSelectorForInvolvedObjectKind() {
        final EventWatchOptions options = EventWatchOptions.builder()
                .involvedObjectKind("Pod")
                .build();
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getFieldSelector()).isEqualTo("involvedObject.kind=Pod");
    }

    @Test
    void shouldBuildSingleFieldSelectorForEventType() {
        final EventWatchOptions options = EventWatchOptions.builder()
                .eventType(Event.TYPE_WARNING)
                .build();
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getFieldSelector()).isEqualTo("type=Warning");
    }

    @Test
    void shouldBuildSingleFieldSelectorForReason() {
        final EventWatchOptions options = EventWatchOptions.builder()
                .reason("Created")
                .build();
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getFieldSelector()).isEqualTo("reason=Created");
    }

    @Test
    void shouldBuildSingleFieldSelectorForReportingController() {
        final EventWatchOptions options = EventWatchOptions.builder()
                .reportingController("kubelet")
                .build();
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getFieldSelector()).isEqualTo("source.component=kubelet");
    }

    @Test
    void shouldBuildMultipleFieldSelectorsJoinedWithComma() {
        final EventWatchOptions options = EventWatchOptions.builder()
                .involvedObjectKind("Pod")
                .involvedObjectName("my-pod")
                .eventType(Event.TYPE_WARNING)
                .build();
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getFieldSelector())
                .contains("involvedObject.name=my-pod")
                .contains("involvedObject.kind=Pod")
                .contains("type=Warning")
                .contains(",");
    }

    @Test
    void shouldBuildAllFieldSelectors() {
        final EventWatchOptions options = EventWatchOptions.builder()
                .involvedObjectName("my-pod")
                .involvedObjectNamespace("default")
                .involvedObjectKind("Pod")
                .involvedObjectUid("abc-123")
                .eventType(Event.TYPE_NORMAL)
                .reason("Started")
                .reportingController("kubelet")
                .build();
        final WatchOptions watchOptions = options.toWatchOptions();

        final String fieldSelector = watchOptions.getFieldSelector();
        assertThat(fieldSelector).contains("involvedObject.name=my-pod");
        assertThat(fieldSelector).contains("involvedObject.namespace=default");
        assertThat(fieldSelector).contains("involvedObject.kind=Pod");
        assertThat(fieldSelector).contains("involvedObject.uid=abc-123");
        assertThat(fieldSelector).contains("type=Normal");
        assertThat(fieldSelector).contains("reason=Started");
        assertThat(fieldSelector).contains("source.component=kubelet");
    }

    @Test
    void shouldCreateOptionsForObjectReference() {
        final ObjectReference ref = ObjectReference.builder()
                .kind("Pod")
                .name("my-pod")
                .namespace("default")
                .uid("abc-123")
                .build();

        final EventWatchOptions options = EventWatchOptions.forObject(ref);
        final WatchOptions watchOptions = options.toWatchOptions();

        final String fieldSelector = watchOptions.getFieldSelector();
        assertThat(fieldSelector).contains("involvedObject.name=my-pod");
        assertThat(fieldSelector).contains("involvedObject.namespace=default");
        assertThat(fieldSelector).contains("involvedObject.kind=Pod");
        assertThat(fieldSelector).contains("involvedObject.uid=abc-123");
    }

    @Test
    void shouldCreateOptionsForObjectReferenceWithPartialFields() {
        final ObjectReference ref = ObjectReference.builder()
                .kind("Deployment")
                .name("my-deployment")
                .build();

        final EventWatchOptions options = EventWatchOptions.forObject(ref);
        final WatchOptions watchOptions = options.toWatchOptions();

        final String fieldSelector = watchOptions.getFieldSelector();
        assertThat(fieldSelector).contains("involvedObject.name=my-deployment");
        assertThat(fieldSelector).contains("involvedObject.kind=Deployment");
        assertThat(fieldSelector).doesNotContain("involvedObject.namespace");
        assertThat(fieldSelector).doesNotContain("involvedObject.uid");
    }

    @Test
    void shouldThrowExceptionForNullObjectReference() {
        assertThatThrownBy(() -> EventWatchOptions.forObject(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ObjectReference cannot be null");
    }

    @Test
    void shouldCreateOptionsForWarnings() {
        final EventWatchOptions options = EventWatchOptions.forWarnings();
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getFieldSelector()).isEqualTo("type=Warning");
    }

    @Test
    void shouldCreateOptionsForType() {
        final EventWatchOptions options = EventWatchOptions.forType(Event.TYPE_NORMAL);
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getFieldSelector()).isEqualTo("type=Normal");
    }

    @Test
    void shouldCreateOptionsForReason() {
        final EventWatchOptions options = EventWatchOptions.forReason("FailedScheduling");
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getFieldSelector()).isEqualTo("reason=FailedScheduling");
    }

    @Test
    void shouldCreateDefaultOptions() {
        final EventWatchOptions options = EventWatchOptions.defaults();
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(options.getTimeoutSeconds()).isEqualTo(300);
        assertThat(options.getAllowWatchBookmarks()).isTrue();
        assertThat(watchOptions.getFieldSelector()).isNull();
        assertThat(watchOptions.getLabelSelector()).isNull();
    }

    @Test
    void shouldPreserveWatchOptionsDefaults() {
        final EventWatchOptions options = EventWatchOptions.builder()
                .eventType(Event.TYPE_WARNING)
                .build();
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getTimeoutSeconds()).isEqualTo(300);
        assertThat(watchOptions.getAllowWatchBookmarks()).isTrue();
    }

    @Test
    void shouldOverrideWatchOptionsDefaults() {
        final EventWatchOptions options = EventWatchOptions.builder()
                .timeoutSeconds(600)
                .allowWatchBookmarks(false)
                .build();
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getTimeoutSeconds()).isEqualTo(600);
        assertThat(watchOptions.getAllowWatchBookmarks()).isFalse();
    }

    @Test
    void shouldPreserveResourceVersion() {
        final EventWatchOptions options = EventWatchOptions.builder()
                .resourceVersion("12345")
                .build();
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getResourceVersion()).isEqualTo("12345");
    }

    @Test
    void shouldCombineLabelAndFieldSelectors() {
        final EventWatchOptions options = EventWatchOptions.builder()
                .labelSelector("app=myapp")
                .eventType(Event.TYPE_WARNING)
                .build();
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getLabelSelector()).isEqualTo("app=myapp");
        assertThat(watchOptions.getFieldSelector()).isEqualTo("type=Warning");
    }

    @Test
    void shouldBuildOptionsWithLabelSelectorOnly() {
        final EventWatchOptions options = EventWatchOptions.builder()
                .labelSelector("component=kubelet,severity=high")
                .build();
        final WatchOptions watchOptions = options.toWatchOptions();

        assertThat(watchOptions.getLabelSelector()).isEqualTo("component=kubelet,severity=high");
        assertThat(watchOptions.getFieldSelector()).isNull();
    }

    @Test
    void shouldThrowExceptionForNullTypeInForType() {
        assertThatThrownBy(() -> EventWatchOptions.forType(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Event type cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullReasonInForReason() {
        assertThatThrownBy(() -> EventWatchOptions.forReason(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Event reason cannot be null");
    }
}
