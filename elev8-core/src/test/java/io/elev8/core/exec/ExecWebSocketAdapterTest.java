package io.elev8.core.exec;

import io.elev8.core.websocket.WebSocketClient;
import io.elev8.core.websocket.WebSocketException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExecWebSocketAdapterTest {

    private WebSocketClient mockWebSocketClient;
    private TestExecWatch testExecWatch;
    private ExecOptions options;
    private ExecWebSocketAdapter adapter;

    @BeforeEach
    void setUp() {
        mockWebSocketClient = mock(WebSocketClient.class);
        testExecWatch = new TestExecWatch();
        options = ExecOptions.of(new String[]{"/bin/sh", "-c", "echo test"});
        adapter = new ExecWebSocketAdapter(testExecWatch, options);
        adapter.setWebSocketClient(mockWebSocketClient);
    }

    @Test
    void shouldRouteStdoutMessages() {
        final String stdoutData = "Hello from stdout\n";
        final byte[] frame = ChannelMessage.fromFrame(new byte[]{
                ChannelMessage.STDOUT_CHANNEL,
                's', 't', 'd', 'o', 'u', 't'
        }).toFrame();

        adapter.onMessage(frame);

        assertEquals("stdout", testExecWatch.lastStdout);
    }

    @Test
    void shouldRouteStderrMessages() {
        final byte[] frame = ChannelMessage.fromFrame(new byte[]{
                ChannelMessage.STDERR_CHANNEL,
                's', 't', 'd', 'e', 'r', 'r'
        }).toFrame();

        adapter.onMessage(frame);

        assertEquals("stderr", testExecWatch.lastStderr);
    }

    @Test
    void shouldExtractExitCodeFromErrorChannel() {
        final String statusJson = """
            {
              "status": "Success",
              "details": {
                "causes": [
                  {
                    "reason": "ExitCode",
                    "message": "0"
                  }
                ]
              }
            }
            """;

        final byte[] frame = createErrorChannelFrame(statusJson);

        adapter.onMessage(frame);

        assertEquals(0, testExecWatch.exitCode);
        assertTrue(testExecWatch.closeCalled);
    }

    @Test
    void shouldExtractNonZeroExitCode() {
        final String statusJson = """
            {
              "status": "Failure",
              "details": {
                "causes": [
                  {
                    "reason": "ExitCode",
                    "message": "127"
                  }
                ]
              }
            }
            """;

        final byte[] frame = createErrorChannelFrame(statusJson);

        adapter.onMessage(frame);

        assertEquals(127, testExecWatch.exitCode);
        assertTrue(testExecWatch.closeCalled);
    }

    @Test
    void shouldHandleErrorChannelNonJsonAsPlainError() {
        final String plainError = "Connection failed";
        final byte[] frame = createErrorChannelFrame(plainError);

        adapter.onMessage(frame);

        assertEquals(plainError, testExecWatch.lastError);
        assertFalse(testExecWatch.closeCalled);
    }

    @Test
    void shouldHandleErrorChannelWithoutExitCode() {
        final String statusJson = """
            {
              "status": "Failure",
              "message": "Some error occurred"
            }
            """;

        final byte[] frame = createErrorChannelFrame(statusJson);

        adapter.onMessage(frame);

        assertEquals(statusJson, testExecWatch.lastError);
        assertFalse(testExecWatch.closeCalled);
    }

    @Test
    void shouldDefaultToExitCodeZeroOnNormalClose() {
        adapter.onClose(1000, "Normal closure");

        assertEquals(0, testExecWatch.exitCode);
        assertTrue(testExecWatch.closeCalled);
    }

    @Test
    void shouldDefaultToExitCodeOneOnAbnormalClose() {
        adapter.onClose(1006, "Abnormal closure");

        assertEquals(1, testExecWatch.exitCode);
        assertTrue(testExecWatch.closeCalled);
    }

    @Test
    void shouldNotCallCloseAgainIfExitCodeAlreadyReceived() {
        final String statusJson = """
            {
              "status": "Success",
              "details": {
                "causes": [
                  {
                    "reason": "ExitCode",
                    "message": "42"
                  }
                ]
              }
            }
            """;

        final byte[] frame = createErrorChannelFrame(statusJson);

        adapter.onMessage(frame);
        assertEquals(42, testExecWatch.exitCode);
        assertEquals(1, testExecWatch.closeCallCount);

        adapter.onClose(1000, "Normal closure");
        assertEquals(1, testExecWatch.closeCallCount); // Should not increase
    }

    @Test
    void shouldCallOnFailureForWebSocketErrors() {
        final Exception testException = new Exception("Test error");

        adapter.onError(testException);

        assertNotNull(testExecWatch.lastException);
        assertEquals("Test error", testExecWatch.lastException.getMessage());
    }

    @Test
    void shouldWriteStdinWhenEnabled() throws WebSocketException {
        final ExecOptions optionsWithStdin = ExecOptions.of(new String[]{"cat"})
                .toBuilder()
                .stdin(true)
                .build();
        final ExecWebSocketAdapter adapterWithStdin = new ExecWebSocketAdapter(testExecWatch, optionsWithStdin);
        adapterWithStdin.setWebSocketClient(mockWebSocketClient);

        adapterWithStdin.writeStdin("test input");

        verify(mockWebSocketClient).sendBinary(any(byte[].class));
    }

    @Test
    void shouldThrowExceptionWhenWritingStdinWithoutEnable() {
        final ExecOptions optionsWithoutStdin = ExecOptions.of(new String[]{"cat"});
        final ExecWebSocketAdapter adapterWithoutStdin = new ExecWebSocketAdapter(testExecWatch, optionsWithoutStdin);
        adapterWithoutStdin.setWebSocketClient(mockWebSocketClient);

        assertThrows(WebSocketException.class, () -> adapterWithoutStdin.writeStdin("test input"));
    }

    @Test
    void shouldThrowExceptionWhenWritingStdinWithoutClient() {
        final ExecOptions optionsWithStdin = ExecOptions.of(new String[]{"cat"})
                .toBuilder()
                .stdin(true)
                .build();
        final ExecWebSocketAdapter adapterWithoutClient = new ExecWebSocketAdapter(testExecWatch, optionsWithStdin);

        assertThrows(WebSocketException.class, () -> adapterWithoutClient.writeStdin("test input"));
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
    void shouldHandleMultipleStdoutMessages() {
        final byte[] frame1 = createStdoutFrame("line 1\n");
        final byte[] frame2 = createStdoutFrame("line 2\n");

        adapter.onMessage(frame1);
        adapter.onMessage(frame2);

        assertEquals("line 2\n", testExecWatch.lastStdout);
        assertEquals(2, testExecWatch.stdoutCallCount);
    }

    @Test
    void shouldHandleFrameParsingErrors() {
        final byte[] invalidFrame = new byte[0]; // Empty frame

        adapter.onMessage(invalidFrame);

        assertNotNull(testExecWatch.lastException);
    }

    private byte[] createErrorChannelFrame(final String data) {
        return ChannelMessage.fromFrame(
                prependByte(ChannelMessage.ERROR_CHANNEL, data.getBytes())
        ).toFrame();
    }

    private byte[] createStdoutFrame(final String data) {
        return ChannelMessage.fromFrame(
                prependByte(ChannelMessage.STDOUT_CHANNEL, data.getBytes())
        ).toFrame();
    }

    private byte[] prependByte(final byte prefix, final byte[] data) {
        final byte[] result = new byte[data.length + 1];
        result[0] = prefix;
        System.arraycopy(data, 0, result, 1, data.length);
        return result;
    }

    /**
     * Test implementation of ExecWatch for testing.
     */
    private static class TestExecWatch implements ExecWatch {
        String lastStdout;
        String lastStderr;
        String lastError;
        int exitCode = -1;
        Exception lastException;
        boolean closeCalled = false;
        int closeCallCount = 0;
        int stdoutCallCount = 0;

        @Override
        public void onStdout(final String data) {
            lastStdout = data;
            stdoutCallCount++;
        }

        @Override
        public void onStderr(final String data) {
            lastStderr = data;
        }

        @Override
        public void onError(final String error) {
            lastError = error;
        }

        @Override
        public void onClose(final int exitCode) {
            this.exitCode = exitCode;
            closeCalled = true;
            closeCallCount++;
        }

        @Override
        public void onFailure(final Exception exception) {
            lastException = exception;
        }
    }
}
