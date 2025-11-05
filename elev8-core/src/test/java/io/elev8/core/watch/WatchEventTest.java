package io.elev8.core.watch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WatchEventTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testBuilder() {
        final String testObject = "test-resource";
        final WatchEvent<String> event = WatchEvent.<String>builder()
                .type(WatchEventType.ADDED)
                .object(testObject)
                .build();

        assertEquals(WatchEventType.ADDED, event.getType());
        assertEquals(testObject, event.getObject());
    }

    @Test
    void testOf() {
        final String testObject = "test-resource";
        final WatchEvent<String> event = WatchEvent.of(WatchEventType.MODIFIED, testObject);

        assertEquals(WatchEventType.MODIFIED, event.getType());
        assertEquals(testObject, event.getObject());
    }

    @Test
    void testIsAdded() {
        final WatchEvent<String> event = WatchEvent.of(WatchEventType.ADDED, "test");
        assertTrue(event.isAdded());
        assertFalse(event.isModified());
        assertFalse(event.isDeleted());
        assertFalse(event.isBookmark());
        assertFalse(event.isError());
    }

    @Test
    void testIsModified() {
        final WatchEvent<String> event = WatchEvent.of(WatchEventType.MODIFIED, "test");
        assertTrue(event.isModified());
        assertFalse(event.isAdded());
        assertFalse(event.isDeleted());
        assertFalse(event.isBookmark());
        assertFalse(event.isError());
    }

    @Test
    void testIsDeleted() {
        final WatchEvent<String> event = WatchEvent.of(WatchEventType.DELETED, "test");
        assertTrue(event.isDeleted());
        assertFalse(event.isAdded());
        assertFalse(event.isModified());
        assertFalse(event.isBookmark());
        assertFalse(event.isError());
    }

    @Test
    void testIsBookmark() {
        final WatchEvent<String> event = WatchEvent.of(WatchEventType.BOOKMARK, null);
        assertTrue(event.isBookmark());
        assertFalse(event.isAdded());
        assertFalse(event.isModified());
        assertFalse(event.isDeleted());
        assertFalse(event.isError());
    }

    @Test
    void testIsError() {
        final WatchEvent<String> event = WatchEvent.of(WatchEventType.ERROR, null);
        assertTrue(event.isError());
        assertFalse(event.isAdded());
        assertFalse(event.isModified());
        assertFalse(event.isDeleted());
        assertFalse(event.isBookmark());
    }

    @Test
    void testJsonSerialization() throws Exception {
        final WatchEvent<String> event = WatchEvent.of(WatchEventType.ADDED, "test-object");
        final String json = objectMapper.writeValueAsString(event);

        assertTrue(json.contains("\"type\":\"ADDED\""));
        assertTrue(json.contains("\"object\":\"test-object\""));
    }

    @Test
    void testJsonDeserialization() throws Exception {
        final String json = "{\"type\":\"MODIFIED\",\"object\":\"test-object\"}";
        final WatchEvent<?> event = objectMapper.readValue(json, WatchEvent.class);

        assertEquals(WatchEventType.MODIFIED, event.getType());
        assertEquals("test-object", event.getObject());
    }
}
