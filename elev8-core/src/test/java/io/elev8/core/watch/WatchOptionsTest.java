package io.elev8.core.watch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WatchOptionsTest {

    @Test
    void testDefaults() {
        final WatchOptions options = WatchOptions.defaults();

        assertNull(options.getResourceVersion());
        assertEquals(300, options.getTimeoutSeconds());
        assertTrue(options.getAllowWatchBookmarks());
        assertNull(options.getLabelSelector());
        assertNull(options.getFieldSelector());
    }

    @Test
    void testBuilder() {
        final WatchOptions options = WatchOptions.builder()
                .resourceVersion("12345")
                .timeoutSeconds(600)
                .allowWatchBookmarks(false)
                .labelSelector("app=myapp")
                .fieldSelector("metadata.name=my-pod")
                .build();

        assertEquals("12345", options.getResourceVersion());
        assertEquals(600, options.getTimeoutSeconds());
        assertFalse(options.getAllowWatchBookmarks());
        assertEquals("app=myapp", options.getLabelSelector());
        assertEquals("metadata.name=my-pod", options.getFieldSelector());
    }

    @Test
    void testFrom() {
        final WatchOptions options = WatchOptions.from("67890");

        assertEquals("67890", options.getResourceVersion());
        assertEquals(300, options.getTimeoutSeconds());
        assertTrue(options.getAllowWatchBookmarks());
    }

    @Test
    void testWithLabelSelector() {
        final WatchOptions options = WatchOptions.withLabelSelector("env=prod");

        assertEquals("env=prod", options.getLabelSelector());
        assertNull(options.getResourceVersion());
        assertEquals(300, options.getTimeoutSeconds());
    }

    @Test
    void testWithFieldSelector() {
        final WatchOptions options = WatchOptions.withFieldSelector("status.phase=Running");

        assertEquals("status.phase=Running", options.getFieldSelector());
        assertNull(options.getResourceVersion());
        assertEquals(300, options.getTimeoutSeconds());
    }

    @Test
    void testBuilderWithNullValues() {
        final WatchOptions options = WatchOptions.builder()
                .resourceVersion(null)
                .labelSelector(null)
                .fieldSelector(null)
                .build();

        assertNull(options.getResourceVersion());
        assertNull(options.getLabelSelector());
        assertNull(options.getFieldSelector());
        assertEquals(300, options.getTimeoutSeconds());
        assertTrue(options.getAllowWatchBookmarks());
    }
}
