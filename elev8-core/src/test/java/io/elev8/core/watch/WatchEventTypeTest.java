package io.elev8.core.watch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WatchEventTypeTest {

    @Test
    void testGetValue() {
        assertEquals("ADDED", WatchEventType.ADDED.getValue());
        assertEquals("MODIFIED", WatchEventType.MODIFIED.getValue());
        assertEquals("DELETED", WatchEventType.DELETED.getValue());
        assertEquals("BOOKMARK", WatchEventType.BOOKMARK.getValue());
        assertEquals("ERROR", WatchEventType.ERROR.getValue());
    }

    @Test
    void testFromValue() {
        assertEquals(WatchEventType.ADDED, WatchEventType.fromValue("ADDED"));
        assertEquals(WatchEventType.MODIFIED, WatchEventType.fromValue("MODIFIED"));
        assertEquals(WatchEventType.DELETED, WatchEventType.fromValue("DELETED"));
        assertEquals(WatchEventType.BOOKMARK, WatchEventType.fromValue("BOOKMARK"));
        assertEquals(WatchEventType.ERROR, WatchEventType.fromValue("ERROR"));
    }

    @Test
    void testFromValueCaseInsensitive() {
        assertEquals(WatchEventType.ADDED, WatchEventType.fromValue("added"));
        assertEquals(WatchEventType.MODIFIED, WatchEventType.fromValue("modified"));
        assertEquals(WatchEventType.DELETED, WatchEventType.fromValue("deleted"));
    }

    @Test
    void testFromValueInvalidThrowsException() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> WatchEventType.fromValue("INVALID")
        );
        assertTrue(exception.getMessage().contains("Unknown watch event type"));
    }
}
