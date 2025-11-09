package io.elev8.core.portforward;

import io.elev8.core.websocket.WebSocketClient;
import io.elev8.core.websocket.WebSocketException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PortForwardWebSocketAdapterTest {

    private WebSocketClient mockWebSocketClient;
    private TestPortForwardWatch testWatch;
    private PortForwardOptions options;
    private PortForwardWebSocketAdapter adapter;

    @BeforeEach
    void setUp() {
        mockWebSocketClient = mock(WebSocketClient.class);
        testWatch = new TestPortForwardWatch();
        options = PortForwardOptions.of(8080);
        adapter = new PortForwardWebSocketAdapter(testWatch, options);
        adapter.setWebSocketClient(mockWebSocketClient);
    }

    @Test
    void shouldRouteDataToCorrectPort() {
        // Channel 0 = data for first port (8080)
        final byte[] frame = createFrame(0, "Hello".getBytes());

        adapter.onMessage(frame);

        assertEquals(1, testWatch.dataEvents.size());
        assertEquals(8080, testWatch.dataEvents.get(0).port);
        assertArrayEquals("Hello".getBytes(), testWatch.dataEvents.get(0).data);
    }

    @Test
    void shouldRouteErrorToCorrectPort() {
        // Channel 1 = error for first port (8080)
        final byte[] frame = createFrame(1, "Error occurred".getBytes());

        adapter.onMessage(frame);

        assertEquals(1, testWatch.errorEvents.size());
        assertEquals(8080, testWatch.errorEvents.get(0).port);
        assertEquals("Error occurred", testWatch.errorEvents.get(0).error);
    }

    @Test
    void shouldHandleMultiplePortsCorrectly() {
        options = PortForwardOptions.of(8080, 9090, 3000);
        adapter = new PortForwardWebSocketAdapter(testWatch, options);
        adapter.setWebSocketClient(mockWebSocketClient);

        // Channel 0 = data for port 8080
        // Channel 2 = data for port 9090
        // Channel 4 = data for port 3000
        adapter.onMessage(createFrame(0, "Data for 8080".getBytes()));
        adapter.onMessage(createFrame(2, "Data for 9090".getBytes()));
        adapter.onMessage(createFrame(4, "Data for 3000".getBytes()));

        assertEquals(3, testWatch.dataEvents.size());
        assertEquals(8080, testWatch.dataEvents.get(0).port);
        assertEquals(9090, testWatch.dataEvents.get(1).port);
        assertEquals(3000, testWatch.dataEvents.get(2).port);
    }

    @Test
    void shouldHandleErrorsForMultiplePorts() {
        options = PortForwardOptions.of(8080, 9090);
        adapter = new PortForwardWebSocketAdapter(testWatch, options);

        // Channel 1 = error for port 8080
        // Channel 3 = error for port 9090
        adapter.onMessage(createFrame(1, "Error 8080".getBytes()));
        adapter.onMessage(createFrame(3, "Error 9090".getBytes()));

        assertEquals(2, testWatch.errorEvents.size());
        assertEquals(8080, testWatch.errorEvents.get(0).port);
        assertEquals(9090, testWatch.errorEvents.get(1).port);
    }

    @Test
    void shouldCallOnCloseWhenConnectionClosed() {
        adapter.onClose(1000, "Normal closure");

        assertTrue(testWatch.closeCalled);
    }

    @Test
    void shouldCallOnFailureForWebSocketErrors() {
        final Exception testException = new Exception("Test error");

        adapter.onError(testException);

        assertNotNull(testWatch.lastException);
        assertEquals("Test error", testWatch.lastException.getMessage());
    }

    @Test
    void shouldWriteDataToCorrectChannel() throws WebSocketException {
        adapter.writeData(8080, "Hello".getBytes());

        verify(mockWebSocketClient).sendBinary(any(byte[].class));
    }

    @Test
    void shouldThrowExceptionWhenWritingToUnforwardedPort() {
        assertThrows(WebSocketException.class, () ->
                adapter.writeData(9999, "data".getBytes()));
    }

    @Test
    void shouldThrowExceptionWhenWritingWithoutClient() {
        final PortForwardWebSocketAdapter adapterWithoutClient =
                new PortForwardWebSocketAdapter(testWatch, options);

        assertThrows(WebSocketException.class, () ->
                adapterWithoutClient.writeData(8080, "data".getBytes()));
    }

    @Test
    void shouldCloseWebSocketConnection() {
        when(mockWebSocketClient.isOpen()).thenReturn(true);

        adapter.close();

        verify(mockWebSocketClient).close(1000, "Normal closure");
    }

    @Test
    void shouldNotCloseIfWebSocketNotOpen() {
        when(mockWebSocketClient.isOpen()).thenReturn(false);

        adapter.close();

        verify(mockWebSocketClient, never()).close(anyInt(), anyString());
    }

    @Test
    void shouldHandleEmptyMessage() {
        final byte[] emptyFrame = new byte[0];

        adapter.onMessage(emptyFrame);

        // Should not crash, should just log warning
        assertEquals(0, testWatch.dataEvents.size());
        assertEquals(0, testWatch.errorEvents.size());
    }

    @Test
    void shouldHandleNullMessage() {
        adapter.onMessage((byte[]) null);

        // Should not crash, should just log warning
        assertEquals(0, testWatch.dataEvents.size());
        assertEquals(0, testWatch.errorEvents.size());
    }

    @Test
    void shouldLogWarningForTextMessages() {
        adapter.onMessage("Unexpected text");

        // Should not crash, should just log warning
        assertEquals(0, testWatch.dataEvents.size());
    }

    @Test
    void shouldHandleUnknownChannel() {
        // Channel 99 doesn't map to any port
        final byte[] frame = createFrame(99, "data".getBytes());

        adapter.onMessage(frame);

        // Should not crash or call callbacks for unknown channel
        assertEquals(0, testWatch.dataEvents.size());
        assertEquals(0, testWatch.errorEvents.size());
    }

    @Test
    void shouldHandleMultipleDataMessagesForSamePort() {
        adapter.onMessage(createFrame(0, "chunk1".getBytes()));
        adapter.onMessage(createFrame(0, "chunk2".getBytes()));
        adapter.onMessage(createFrame(0, "chunk3".getBytes()));

        assertEquals(3, testWatch.dataEvents.size());
        assertArrayEquals("chunk1".getBytes(), testWatch.dataEvents.get(0).data);
        assertArrayEquals("chunk2".getBytes(), testWatch.dataEvents.get(1).data);
        assertArrayEquals("chunk3".getBytes(), testWatch.dataEvents.get(2).data);
    }

    @Test
    void shouldHandleFrameProcessingErrors() {
        // Even a frame with just channel ID should be processed without error
        // (it will have empty data array)
        final byte[] frameWithOnlyChannel = new byte[]{0}; // Just channel, no data

        adapter.onMessage(frameWithOnlyChannel);

        // Should be processed normally with empty data
        assertEquals(1, testWatch.dataEvents.size());
        assertEquals(0, testWatch.dataEvents.get(0).data.length);
    }

    @Test
    void shouldEncodeChannelCorrectlyWhenWriting() throws WebSocketException {
        final byte[] data = "test data".getBytes();

        adapter.writeData(8080, data);

        verify(mockWebSocketClient).sendBinary(argThat(frame -> {
            // First byte should be channel 0 (data channel for first port)
            if (frame[0] != 0) return false;
            // Rest should be the data
            if (frame.length != data.length + 1) return false;
            for (int i = 0; i < data.length; i++) {
                if (frame[i + 1] != data[i]) return false;
            }
            return true;
        }));
    }

    @Test
    void shouldMapChannelsCorrectlyForMultiplePorts() {
        options = PortForwardOptions.of(8080, 9090, 3000);
        adapter = new PortForwardWebSocketAdapter(testWatch, options);
        adapter.setWebSocketClient(mockWebSocketClient);

        // Test that channels map correctly:
        // 8080: data=0, error=1
        // 9090: data=2, error=3
        // 3000: data=4, error=5
        adapter.onMessage(createFrame(0, "8080-data".getBytes()));
        adapter.onMessage(createFrame(1, "8080-error".getBytes()));
        adapter.onMessage(createFrame(2, "9090-data".getBytes()));
        adapter.onMessage(createFrame(3, "9090-error".getBytes()));
        adapter.onMessage(createFrame(4, "3000-data".getBytes()));
        adapter.onMessage(createFrame(5, "3000-error".getBytes()));

        assertEquals(3, testWatch.dataEvents.size());
        assertEquals(3, testWatch.errorEvents.size());

        assertEquals(8080, testWatch.dataEvents.get(0).port);
        assertEquals(9090, testWatch.dataEvents.get(1).port);
        assertEquals(3000, testWatch.dataEvents.get(2).port);

        assertEquals(8080, testWatch.errorEvents.get(0).port);
        assertEquals(9090, testWatch.errorEvents.get(1).port);
        assertEquals(3000, testWatch.errorEvents.get(2).port);
    }

    private byte[] createFrame(final int channelId, final byte[] data) {
        final byte[] frame = new byte[data.length + 1];
        frame[0] = (byte) channelId;
        System.arraycopy(data, 0, frame, 1, data.length);
        return frame;
    }

    /**
     * Test implementation of PortForwardWatch for testing.
     */
    private static class TestPortForwardWatch implements PortForwardWatch {
        final List<DataEvent> dataEvents = new ArrayList<>();
        final List<ErrorEvent> errorEvents = new ArrayList<>();
        Exception lastException;
        boolean closeCalled = false;

        @Override
        public void onData(final int port, final byte[] data) {
            dataEvents.add(new DataEvent(port, data));
        }

        @Override
        public void onError(final int port, final String error) {
            errorEvents.add(new ErrorEvent(port, error));
        }

        @Override
        public void onClose() {
            closeCalled = true;
        }

        @Override
        public void onFailure(final Exception exception) {
            lastException = exception;
        }

        static class DataEvent {
            final int port;
            final byte[] data;

            DataEvent(final int port, final byte[] data) {
                this.port = port;
                this.data = data;
            }
        }

        static class ErrorEvent {
            final int port;
            final String error;

            ErrorEvent(final int port, final String error) {
                this.port = port;
                this.error = error;
            }
        }
    }
}
