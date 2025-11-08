package io.elev8.core.exec;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.charset.StandardCharsets;

/**
 * Represents a message in a channel-multiplexed stream.
 * Kubernetes exec protocol uses a single byte prefix to indicate the channel,
 * followed by the actual data.
 *
 * <p>Channel IDs:
 * <ul>
 *   <li>0: STDIN - Input stream to the container</li>
 *   <li>1: STDOUT - Standard output from the container</li>
 *   <li>2: STDERR - Standard error from the container</li>
 *   <li>3: ERROR - Special error channel for protocol errors</li>
 *   <li>4: RESIZE - Terminal resize events (TTY only)</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public class ChannelMessage {
    public static final byte STDIN_CHANNEL = 0;
    public static final byte STDOUT_CHANNEL = 1;
    public static final byte STDERR_CHANNEL = 2;
    public static final byte ERROR_CHANNEL = 3;
    public static final byte RESIZE_CHANNEL = 4;

    private final byte channel;
    private final byte[] data;

    /**
     * Creates a ChannelMessage from raw bytes received from WebSocket.
     * First byte is the channel ID, rest is the data.
     *
     * @param frameData the raw frame data
     * @return a ChannelMessage instance
     * @throws IllegalArgumentException if frameData is empty
     */
    public static ChannelMessage fromFrame(final byte[] frameData) {
        if (frameData == null || frameData.length == 0) {
            throw new IllegalArgumentException("Frame data cannot be empty");
        }

        final byte channel = frameData[0];
        final byte[] data = new byte[frameData.length - 1];
        System.arraycopy(frameData, 1, data, 0, data.length);

        return new ChannelMessage(channel, data);
    }

    /**
     * Converts this message to a frame for sending over WebSocket.
     * Prepends the channel ID byte to the data.
     *
     * @return the framed data
     */
    public byte[] toFrame() {
        final byte[] frame = new byte[data.length + 1];
        frame[0] = channel;
        System.arraycopy(data, 0, frame, 1, data.length);
        return frame;
    }

    /**
     * Gets the data as a UTF-8 string.
     *
     * @return the data decoded as UTF-8 string
     */
    public String getDataAsString() {
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * Checks if this message is from STDIN channel.
     *
     * @return true if this is a STDIN message
     */
    public boolean isStdin() {
        return channel == STDIN_CHANNEL;
    }

    /**
     * Checks if this message is from STDOUT channel.
     *
     * @return true if this is a STDOUT message
     */
    public boolean isStdout() {
        return channel == STDOUT_CHANNEL;
    }

    /**
     * Checks if this message is from STDERR channel.
     *
     * @return true if this is a STDERR message
     */
    public boolean isStderr() {
        return channel == STDERR_CHANNEL;
    }

    /**
     * Checks if this message is from ERROR channel.
     *
     * @return true if this is an ERROR message
     */
    public boolean isError() {
        return channel == ERROR_CHANNEL;
    }

    /**
     * Checks if this message is from RESIZE channel.
     *
     * @return true if this is a RESIZE message
     */
    public boolean isResize() {
        return channel == RESIZE_CHANNEL;
    }

    /**
     * Creates a STDIN channel message.
     *
     * @param data the data to send to STDIN
     * @return a ChannelMessage for STDIN
     */
    public static ChannelMessage stdin(final byte[] data) {
        return new ChannelMessage(STDIN_CHANNEL, data);
    }

    /**
     * Creates a STDIN channel message from a string.
     *
     * @param text the text to send to STDIN
     * @return a ChannelMessage for STDIN
     */
    public static ChannelMessage stdin(final String text) {
        return new ChannelMessage(STDIN_CHANNEL, text.getBytes(StandardCharsets.UTF_8));
    }
}
