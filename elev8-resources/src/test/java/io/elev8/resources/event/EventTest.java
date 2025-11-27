package io.elev8.resources.event;

import io.elev8.resources.ObjectReference;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventTest {

    @Test
    void shouldBuildEventWithRequiredFields() {
        final Event event = Event.builder()
                .name("test-event")
                .namespace("default")
                .build();

        assertThat(event.getApiVersion()).isEqualTo("v1");
        assertThat(event.getKind()).isEqualTo("Event");
        assertThat(event.getName()).isEqualTo("test-event");
        assertThat(event.getNamespace()).isEqualTo("default");
    }

    @Test
    void shouldBuildEventWithRegardingObject() {
        final ObjectReference regarding = ObjectReference.builder()
                .kind("Pod")
                .namespace("default")
                .name("my-pod")
                .apiVersion("v1")
                .build();

        final Event event = Event.builder()
                .name("pod-started")
                .namespace("default")
                .regarding(regarding)
                .build();

        assertThat(event.getRegarding()).isNotNull();
        assertThat(event.getRegarding().getKind()).isEqualTo("Pod");
        assertThat(event.getRegarding().getName()).isEqualTo("my-pod");
    }

    @Test
    void shouldBuildEventWithRelatedObject() {
        final ObjectReference regarding = ObjectReference.builder()
                .kind("Pod")
                .namespace("default")
                .name("my-pod")
                .build();

        final ObjectReference related = ObjectReference.builder()
                .kind("ReplicaSet")
                .namespace("default")
                .name("my-replicaset")
                .build();

        final Event event = Event.builder()
                .name("pod-event")
                .namespace("default")
                .regarding(regarding)
                .related(related)
                .build();

        assertThat(event.getRegarding().getKind()).isEqualTo("Pod");
        assertThat(event.getRelated()).isNotNull();
        assertThat(event.getRelated().getKind()).isEqualTo("ReplicaSet");
    }

    @Test
    void shouldBuildEventWithEventSeries() {
        final EventSeries series = EventSeries.builder()
                .count(5)
                .lastObservedTime(Instant.now())
                .build();

        final Event event = Event.builder()
                .name("repeated-event")
                .namespace("default")
                .series(series)
                .build();

        assertThat(event.getSeries()).isNotNull();
        assertThat(event.getSeries().getCount()).isEqualTo(5);
    }

    @Test
    void shouldBuildEventWithAllFields() {
        final Instant eventTime = Instant.parse("2025-01-15T10:30:00Z");
        final ObjectReference regarding = ObjectReference.builder()
                .kind("Deployment")
                .namespace("production")
                .name("app-deployment")
                .uid("abc-123")
                .apiVersion("apps/v1")
                .build();

        final EventSeries series = EventSeries.builder()
                .count(3)
                .lastObservedTime(eventTime)
                .build();

        final Event event = Event.builder()
                .name("deployment-scaled")
                .namespace("production")
                .action("ScalingReplicaSet")
                .eventTime(eventTime)
                .note("Scaled up replica set app-deployment-abc to 5")
                .reason("ScalingReplicaSet")
                .regarding(regarding)
                .reportingController("deployment-controller")
                .reportingInstance("deployment-controller-abc123")
                .series(series)
                .type(Event.TYPE_NORMAL)
                .label("app", "myapp")
                .build();

        assertThat(event.getName()).isEqualTo("deployment-scaled");
        assertThat(event.getNamespace()).isEqualTo("production");
        assertThat(event.getAction()).isEqualTo("ScalingReplicaSet");
        assertThat(event.getEventTime()).isEqualTo(eventTime);
        assertThat(event.getNote()).contains("Scaled up");
        assertThat(event.getReason()).isEqualTo("ScalingReplicaSet");
        assertThat(event.getRegarding().getKind()).isEqualTo("Deployment");
        assertThat(event.getReportingController()).isEqualTo("deployment-controller");
        assertThat(event.getReportingInstance()).isEqualTo("deployment-controller-abc123");
        assertThat(event.getSeries()).isNotNull();
        assertThat(event.getType()).isEqualTo(Event.TYPE_NORMAL);
        assertThat(event.getMetadata().getLabels()).containsEntry("app", "myapp");
    }

    @Test
    void shouldSupportEventTypeConstants() {
        assertThat(Event.TYPE_NORMAL).isEqualTo("Normal");
        assertThat(Event.TYPE_WARNING).isEqualTo("Warning");
    }

    @Test
    void shouldBuildNormalTypeEvent() {
        final Event event = Event.builder()
                .name("normal-event")
                .namespace("default")
                .type(Event.TYPE_NORMAL)
                .reason("Created")
                .note("Pod has been created successfully")
                .build();

        assertThat(event.getType()).isEqualTo("Normal");
        assertThat(event.getReason()).isEqualTo("Created");
    }

    @Test
    void shouldBuildWarningTypeEvent() {
        final Event event = Event.builder()
                .name("warning-event")
                .namespace("default")
                .type(Event.TYPE_WARNING)
                .reason("FailedMount")
                .note("Unable to mount volume")
                .build();

        assertThat(event.getType()).isEqualTo("Warning");
        assertThat(event.getReason()).isEqualTo("FailedMount");
    }

    @Test
    void shouldBuildEventWithLabels() {
        final Event event = Event.builder()
                .name("labeled-event")
                .namespace("default")
                .label("component", "kubelet")
                .label("severity", "high")
                .build();

        assertThat(event.getMetadata().getLabels()).containsEntry("component", "kubelet");
        assertThat(event.getMetadata().getLabels()).containsEntry("severity", "high");
    }

    @Test
    void shouldSerializeToJson() {
        final ObjectReference regarding = ObjectReference.builder()
                .kind("Pod")
                .namespace("default")
                .name("test-pod")
                .build();

        final Event event = Event.builder()
                .name("test-event")
                .namespace("default")
                .action("Started")
                .reason("Started")
                .type(Event.TYPE_NORMAL)
                .regarding(regarding)
                .build();

        final String json = event.toJson();

        assertThat(json).contains("\"apiVersion\":\"v1\"");
        assertThat(json).contains("\"kind\":\"Event\"");
        assertThat(json).contains("\"name\":\"test-event\"");
        assertThat(json).contains("\"namespace\":\"default\"");
        assertThat(json).contains("\"action\":\"Started\"");
        assertThat(json).contains("\"reason\":\"Started\"");
        assertThat(json).contains("\"type\":\"Normal\"");
        assertThat(json).contains("\"regarding\"");
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() {
        final Event event = Event.builder()
                .name("minimal-event")
                .namespace("default")
                .build();

        final String json = event.toJson();

        assertThat(json).doesNotContain("\"action\"");
        assertThat(json).doesNotContain("\"eventTime\"");
        assertThat(json).doesNotContain("\"note\"");
        assertThat(json).doesNotContain("\"reason\"");
        assertThat(json).doesNotContain("\"regarding\"");
        assertThat(json).doesNotContain("\"related\"");
        assertThat(json).doesNotContain("\"series\"");
        assertThat(json).doesNotContain("\"type\"");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> Event.builder()
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Event name is required");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> Event.builder()
                .name("")
                .namespace("default")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Event name is required");
    }

    @Test
    void shouldAllowEventWithoutNamespace() {
        final Event event = Event.builder()
                .name("cluster-event")
                .build();

        assertThat(event.getName()).isEqualTo("cluster-event");
        assertThat(event.getNamespace()).isNull();
    }

    @Test
    void shouldBuildEventWithReportingComponents() {
        final Event event = Event.builder()
                .name("controller-event")
                .namespace("kube-system")
                .reportingController("kube-controller-manager")
                .reportingInstance("kube-controller-manager-master-1")
                .build();

        assertThat(event.getReportingController()).isEqualTo("kube-controller-manager");
        assertThat(event.getReportingInstance()).isEqualTo("kube-controller-manager-master-1");
    }

    @Test
    void shouldBuildEventWithEventTimeAndNote() {
        final Instant eventTime = Instant.parse("2025-01-15T14:45:30Z");
        final Event event = Event.builder()
                .name("timed-event")
                .namespace("default")
                .eventTime(eventTime)
                .note("This is a human-readable description of the event")
                .build();

        assertThat(event.getEventTime()).isEqualTo(eventTime);
        assertThat(event.getNote()).isEqualTo("This is a human-readable description of the event");
    }
}
