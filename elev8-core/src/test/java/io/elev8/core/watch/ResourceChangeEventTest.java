package io.elev8.core.watch;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceChangeEventTest {

    @Nested
    class Builder {

        @Test
        void shouldBuildWithAllFields() {
            final Instant now = Instant.now();
            final String resource = "current";
            final String previous = "previous";

            final ResourceChangeEvent<String> event = ResourceChangeEvent.<String>builder()
                    .type(ResourceChangeType.UPDATED)
                    .resource(resource)
                    .previousResource(previous)
                    .resourceVersion("12345")
                    .timestamp(now)
                    .build();

            assertThat(event.getType()).isEqualTo(ResourceChangeType.UPDATED);
            assertThat(event.getResource()).isEqualTo(resource);
            assertThat(event.getPreviousResource()).isEqualTo(previous);
            assertThat(event.getResourceVersion()).isEqualTo("12345");
            assertThat(event.getTimestamp()).isEqualTo(now);
        }
    }

    @Nested
    class ConvenienceMethods {

        @Test
        void isCreatedShouldReturnTrueForCreatedType() {
            final ResourceChangeEvent<String> event = ResourceChangeEvent.<String>builder()
                    .type(ResourceChangeType.CREATED)
                    .build();

            assertThat(event.isCreated()).isTrue();
            assertThat(event.isUpdated()).isFalse();
            assertThat(event.isDeleted()).isFalse();
            assertThat(event.isSync()).isFalse();
        }

        @Test
        void isUpdatedShouldReturnTrueForUpdatedType() {
            final ResourceChangeEvent<String> event = ResourceChangeEvent.<String>builder()
                    .type(ResourceChangeType.UPDATED)
                    .build();

            assertThat(event.isCreated()).isFalse();
            assertThat(event.isUpdated()).isTrue();
            assertThat(event.isDeleted()).isFalse();
            assertThat(event.isSync()).isFalse();
        }

        @Test
        void isDeletedShouldReturnTrueForDeletedType() {
            final ResourceChangeEvent<String> event = ResourceChangeEvent.<String>builder()
                    .type(ResourceChangeType.DELETED)
                    .build();

            assertThat(event.isCreated()).isFalse();
            assertThat(event.isUpdated()).isFalse();
            assertThat(event.isDeleted()).isTrue();
            assertThat(event.isSync()).isFalse();
        }

        @Test
        void isSyncShouldReturnTrueForSyncType() {
            final ResourceChangeEvent<String> event = ResourceChangeEvent.<String>builder()
                    .type(ResourceChangeType.SYNC)
                    .build();

            assertThat(event.isCreated()).isFalse();
            assertThat(event.isUpdated()).isFalse();
            assertThat(event.isDeleted()).isFalse();
            assertThat(event.isSync()).isTrue();
        }
    }

    @Nested
    class FromWatchEvent {

        @Test
        void shouldConvertAddedEvent() {
            final WatchEvent<String> watchEvent = WatchEvent.of(WatchEventType.ADDED, "new-resource");

            final ResourceChangeEvent<String> event = ResourceChangeEvent.from(watchEvent, null);

            assertThat(event.getType()).isEqualTo(ResourceChangeType.CREATED);
            assertThat(event.getResource()).isEqualTo("new-resource");
            assertThat(event.getPreviousResource()).isNull();
            assertThat(event.getTimestamp()).isNotNull();
        }

        @Test
        void shouldConvertModifiedEvent() {
            final WatchEvent<String> watchEvent = WatchEvent.of(WatchEventType.MODIFIED, "updated-resource");

            final ResourceChangeEvent<String> event = ResourceChangeEvent.from(watchEvent, "old-resource");

            assertThat(event.getType()).isEqualTo(ResourceChangeType.UPDATED);
            assertThat(event.getResource()).isEqualTo("updated-resource");
            assertThat(event.getPreviousResource()).isEqualTo("old-resource");
        }

        @Test
        void shouldConvertDeletedEvent() {
            final WatchEvent<String> watchEvent = WatchEvent.of(WatchEventType.DELETED, "deleted-resource");

            final ResourceChangeEvent<String> event = ResourceChangeEvent.from(watchEvent, "previous-state");

            assertThat(event.getType()).isEqualTo(ResourceChangeType.DELETED);
            assertThat(event.getResource()).isNull();
            assertThat(event.getPreviousResource()).isEqualTo("previous-state");
        }

        @Test
        void shouldConvertDeletedEventWithoutPreviousState() {
            final WatchEvent<String> watchEvent = WatchEvent.of(WatchEventType.DELETED, "deleted-resource");

            final ResourceChangeEvent<String> event = ResourceChangeEvent.from(watchEvent, null);

            assertThat(event.getType()).isEqualTo(ResourceChangeType.DELETED);
            assertThat(event.getResource()).isNull();
            assertThat(event.getPreviousResource()).isEqualTo("deleted-resource");
        }

        @Test
        void shouldConvertBookmarkEvent() {
            final WatchEvent<String> watchEvent = WatchEvent.of(WatchEventType.BOOKMARK, null);

            final ResourceChangeEvent<String> event = ResourceChangeEvent.from(watchEvent, null);

            assertThat(event.getType()).isEqualTo(ResourceChangeType.SYNC);
            assertThat(event.getResource()).isNull();
            assertThat(event.getPreviousResource()).isNull();
        }

        @Test
        void shouldThrowForErrorEvent() {
            final WatchEvent<String> watchEvent = WatchEvent.of(WatchEventType.ERROR, null);

            assertThatThrownBy(() -> ResourceChangeEvent.from(watchEvent, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ERROR");
        }

        @Test
        void shouldThrowForNullEvent() {
            assertThatThrownBy(() -> ResourceChangeEvent.from(null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }
    }
}
