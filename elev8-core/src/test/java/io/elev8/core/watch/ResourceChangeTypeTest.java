package io.elev8.core.watch;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceChangeTypeTest {

    @Test
    void shouldHaveAllExpectedValues() {
        assertThat(ResourceChangeType.values())
                .containsExactly(
                        ResourceChangeType.CREATED,
                        ResourceChangeType.UPDATED,
                        ResourceChangeType.DELETED,
                        ResourceChangeType.SYNC
                );
    }

    @Test
    void shouldMapAddedToCreated() {
        assertThat(ResourceChangeType.fromWatchEventType(WatchEventType.ADDED))
                .isEqualTo(ResourceChangeType.CREATED);
    }

    @Test
    void shouldMapModifiedToUpdated() {
        assertThat(ResourceChangeType.fromWatchEventType(WatchEventType.MODIFIED))
                .isEqualTo(ResourceChangeType.UPDATED);
    }

    @Test
    void shouldMapDeletedToDeleted() {
        assertThat(ResourceChangeType.fromWatchEventType(WatchEventType.DELETED))
                .isEqualTo(ResourceChangeType.DELETED);
    }

    @Test
    void shouldMapBookmarkToSync() {
        assertThat(ResourceChangeType.fromWatchEventType(WatchEventType.BOOKMARK))
                .isEqualTo(ResourceChangeType.SYNC);
    }

    @Test
    void shouldThrowForErrorEventType() {
        assertThatThrownBy(() -> ResourceChangeType.fromWatchEventType(WatchEventType.ERROR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ERROR");
    }

    @Test
    void shouldThrowForNullEventType() {
        assertThatThrownBy(() -> ResourceChangeType.fromWatchEventType(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }
}
