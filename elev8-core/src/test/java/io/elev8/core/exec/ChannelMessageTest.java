package io.elev8.core.exec;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ChannelMessageTest {

    @Test
    void testFromFrame() {
        final byte[] frameData = new byte[]{1, 'H', 'e', 'l', 'l', 'o'};
        final ChannelMessage message = ChannelMessage.fromFrame(frameData);

        assertEquals(1, message.getChannel());
        assertEquals("Hello", message.getDataAsString());
    }

    @Test
    void testFromFrameWithEmptyDataThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> ChannelMessage.fromFrame(new byte[]{}));
        assertThrows(IllegalArgumentException.class, () -> ChannelMessage.fromFrame(null));
    }

    @Test
    void testToFrame() {
        final byte[] data = "Test".getBytes(StandardCharsets.UTF_8);
        final ChannelMessage message = new ChannelMessage((byte) 2, data);
        final byte[] frame = message.toFrame();

        assertEquals(5, frame.length);
        assertEquals(2, frame[0]);
        assertEquals('T', frame[1]);
        assertEquals('e', frame[2]);
        assertEquals('s', frame[3]);
        assertEquals('t', frame[4]);
    }

    @Test
    void testIsStdout() {
        final ChannelMessage message = new ChannelMessage(ChannelMessage.STDOUT_CHANNEL, new byte[]{});
        assertTrue(message.isStdout());
        assertFalse(message.isStdin());
        assertFalse(message.isStderr());
        assertFalse(message.isError());
    }

    @Test
    void testIsStderr() {
        final ChannelMessage message = new ChannelMessage(ChannelMessage.STDERR_CHANNEL, new byte[]{});
        assertTrue(message.isStderr());
        assertFalse(message.isStdout());
        assertFalse(message.isStdin());
        assertFalse(message.isError());
    }

    @Test
    void testIsError() {
        final ChannelMessage message = new ChannelMessage(ChannelMessage.ERROR_CHANNEL, new byte[]{});
        assertTrue(message.isError());
        assertFalse(message.isStdout());
        assertFalse(message.isStderr());
        assertFalse(message.isStdin());
    }

    @Test
    void testStdinFactory() {
        final ChannelMessage message = ChannelMessage.stdin("input");
        assertEquals(ChannelMessage.STDIN_CHANNEL, message.getChannel());
        assertEquals("input", message.getDataAsString());
    }

    @Test
    void testStdinFactoryWithBytes() {
        final byte[] data = "data".getBytes(StandardCharsets.UTF_8);
        final ChannelMessage message = ChannelMessage.stdin(data);
        assertEquals(ChannelMessage.STDIN_CHANNEL, message.getChannel());
        assertArrayEquals(data, message.getData());
    }

    @Test
    void testGetDataAsString() {
        final String text = "Hello World";
        final byte[] data = text.getBytes(StandardCharsets.UTF_8);
        final ChannelMessage message = new ChannelMessage((byte) 1, data);
        assertEquals(text, message.getDataAsString());
    }

    @Test
    void testChannelConstants() {
        assertEquals(0, ChannelMessage.STDIN_CHANNEL);
        assertEquals(1, ChannelMessage.STDOUT_CHANNEL);
        assertEquals(2, ChannelMessage.STDERR_CHANNEL);
        assertEquals(3, ChannelMessage.ERROR_CHANNEL);
        assertEquals(4, ChannelMessage.RESIZE_CHANNEL);
    }
}
