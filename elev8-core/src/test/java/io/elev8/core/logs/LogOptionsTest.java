package io.elev8.core.logs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogOptionsTest {

    @Test
    void testDefaults() {
        final LogOptions options = LogOptions.defaults();

        assertFalse(options.getFollow());
        assertNull(options.getTailLines());
        assertFalse(options.getTimestamps());
        assertNull(options.getSinceSeconds());
        assertNull(options.getSinceTime());
        assertNull(options.getContainer());
        assertFalse(options.getPrevious());
        assertNull(options.getLimitBytes());
    }

    @Test
    void testBuilder() {
        final LogOptions options = LogOptions.builder()
                .follow(true)
                .tailLines(100)
                .timestamps(true)
                .sinceSeconds(3600)
                .sinceTime("2024-01-01T00:00:00Z")
                .container("my-container")
                .previous(true)
                .limitBytes(1024L)
                .build();

        assertTrue(options.getFollow());
        assertEquals(100, options.getTailLines());
        assertTrue(options.getTimestamps());
        assertEquals(3600, options.getSinceSeconds());
        assertEquals("2024-01-01T00:00:00Z", options.getSinceTime());
        assertEquals("my-container", options.getContainer());
        assertTrue(options.getPrevious());
        assertEquals(1024L, options.getLimitBytes());
    }

    @Test
    void testFollow() {
        final LogOptions options = LogOptions.follow();

        assertTrue(options.getFollow());
        assertNull(options.getTailLines());
        assertFalse(options.getTimestamps());
    }

    @Test
    void testTail() {
        final LogOptions options = LogOptions.tail(50);

        assertEquals(50, options.getTailLines());
        assertFalse(options.getFollow());
    }

    @Test
    void testFollowWithTimestamps() {
        final LogOptions options = LogOptions.followWithTimestamps();

        assertTrue(options.getFollow());
        assertTrue(options.getTimestamps());
        assertNull(options.getTailLines());
    }

    @Test
    void testForContainer() {
        final LogOptions options = LogOptions.forContainer("nginx");

        assertEquals("nginx", options.getContainer());
        assertFalse(options.getFollow());
        assertNull(options.getTailLines());
    }

    @Test
    void testPrevious() {
        final LogOptions options = LogOptions.previous();

        assertTrue(options.getPrevious());
        assertFalse(options.getFollow());
    }

    @Test
    void testBuilderWithNullValues() {
        final LogOptions options = LogOptions.builder()
                .tailLines(null)
                .sinceSeconds(null)
                .sinceTime(null)
                .container(null)
                .limitBytes(null)
                .build();

        assertNull(options.getTailLines());
        assertNull(options.getSinceSeconds());
        assertNull(options.getSinceTime());
        assertNull(options.getContainer());
        assertNull(options.getLimitBytes());
        assertFalse(options.getFollow());
        assertFalse(options.getTimestamps());
        assertFalse(options.getPrevious());
    }

    @Test
    void testCombinedOptions() {
        final LogOptions options = LogOptions.builder()
                .follow(true)
                .tailLines(10)
                .timestamps(true)
                .container("app")
                .build();

        assertTrue(options.getFollow());
        assertEquals(10, options.getTailLines());
        assertTrue(options.getTimestamps());
        assertEquals("app", options.getContainer());
    }
}
