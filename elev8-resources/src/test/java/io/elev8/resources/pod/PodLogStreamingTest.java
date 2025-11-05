package io.elev8.resources.pod;

import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientConfig;
import io.elev8.core.logs.LogOptions;
import io.elev8.core.logs.LogWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PodLogStreamingTest {

    private PodManager podManager;
    private KubernetesClient mockClient;

    @BeforeEach
    void setUp() {
        mockClient = mock(KubernetesClient.class);
        final KubernetesClientConfig mockConfig = mock(KubernetesClientConfig.class);
        when(mockClient.getConfig()).thenReturn(mockConfig);
        podManager = new PodManager(mockClient);
    }

    @Test
    void testLogsWithDefaultOptions() {
        final LogOptions options = LogOptions.defaults();

        assertFalse(options.getFollow());
        assertNull(options.getTailLines());
    }

    @Test
    void testLogsWithFollowOption() {
        final LogOptions options = LogOptions.follow();

        assertTrue(options.getFollow());
        assertFalse(options.getTimestamps());
        assertNull(options.getTailLines());
    }

    @Test
    void testLogsWithTailLines() {
        final LogOptions options = LogOptions.tail(100);

        assertEquals(100, options.getTailLines());
        assertFalse(options.getFollow());
    }

    @Test
    void testLogsWithTimestamps() {
        final LogOptions options = LogOptions.followWithTimestamps();

        assertTrue(options.getFollow());
        assertTrue(options.getTimestamps());
    }

    @Test
    void testLogsWithContainer() {
        final LogOptions options = LogOptions.forContainer("nginx");

        assertEquals("nginx", options.getContainer());
    }

    @Test
    void testLogsWithPrevious() {
        final LogOptions options = LogOptions.previous();

        assertTrue(options.getPrevious());
    }

    @Test
    void testLogsCallbackInterface() {
        final List<String> logLines = new ArrayList<>();
        final List<Exception> errors = new ArrayList<>();
        final boolean[] closed = {false};

        final LogWatch logWatch = new LogWatch() {
            @Override
            public void onLog(final String line) {
                logLines.add(line);
            }

            @Override
            public void onError(final Exception exception) {
                errors.add(exception);
            }

            @Override
            public void onClose() {
                closed[0] = true;
            }
        };

        logWatch.onLog("Test log line");
        logWatch.onLog("Another log line");
        logWatch.onError(new RuntimeException("Test error"));
        logWatch.onClose();

        assertEquals(2, logLines.size());
        assertEquals("Test log line", logLines.get(0));
        assertEquals("Another log line", logLines.get(1));
        assertEquals(1, errors.size());
        assertEquals("Test error", errors.get(0).getMessage());
        assertTrue(closed[0]);
    }

    @Test
    void testLogsWithContainerName() {
        final LogOptions baseOptions = LogOptions.builder()
                .follow(true)
                .tailLines(50)
                .build();

        final LogWatch logWatch = line -> {};

        assertDoesNotThrow(() ->
                podManager.logs("default", "my-pod", "nginx", baseOptions, logWatch)
        );
    }
}
