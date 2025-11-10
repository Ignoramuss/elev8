package io.elev8.core.portforward;

import io.elev8.core.websocket.WebSocketClient;
import io.elev8.core.websocket.WebSocketException;
import io.elev8.core.websocket.WebSocketListener;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * Adapter that bridges WebSocket events to PortForwardWatch callbacks.
 * Handles channel multiplexing protocol for port forwarding.
 *
 * <p>Kubernetes port-forward protocol uses paired channels:
 * <ul>
 *   <li>Channel 0: Data for port 1, Channel 1: Error for port 1</li>
 *   <li>Channel 2: Data for port 2, Channel 3: Error for port 2</li>
 *   <li>Pattern: Even channels = data, Odd channels = errors</li>
 * </ul>
 */
@Slf4j
public class PortForwardWebSocketAdapter implements WebSocketListener {
    private final PortForwardWatch portForwardWatch;
    private final PortForwardOptions options;
    private WebSocketClient webSocketClient;

    public PortForwardWebSocketAdapter(final PortForwardWatch portForwardWatch,
                                       final PortForwardOptions options) {
        this.portForwardWatch = portForwardWatch;
        this.options = options;
    }

    /**
     * Sets the WebSocket client for sending data.
     * Called by KubernetesClient after connection is established.
     *
     * @param webSocketClient the WebSocket client
     */
    public void setWebSocketClient(final WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void onOpen() {
        log.debug("Port forward WebSocket connection opened for ports: {}",
                java.util.Arrays.toString(options.getPorts()));
    }

    @Override
    public void onMessage(final byte[] data) {
        if (data == null || data.length == 0) {
            log.warn("Received empty WebSocket message");
            return;
        }

        try {
            // First byte is the channel ID
            final byte channelId = data[0];
            final byte[] payload = new byte[data.length - 1];
            System.arraycopy(data, 1, payload, 0, payload.length);

            // Even channels = data, odd channels = errors
            if (channelId % 2 == 0) {
                handleDataChannel(channelId, payload);
            } else {
                handleErrorChannel(channelId, payload);
            }
        } catch (Exception e) {
            log.error("Error processing WebSocket message", e);
            portForwardWatch.onFailure(new Exception("Failed to process port forward message", e));
        }
    }

    @Override
    public void onMessage(final String text) {
        log.warn("Received unexpected text message (port-forward uses binary): {}", text);
    }

    @Override
    public void onClose(final int code, final String reason) {
        log.debug("Port forward WebSocket connection closed: code={}, reason={}", code, reason);
        portForwardWatch.onClose();
    }

    @Override
    public void onError(final Exception exception) {
        log.error("Port forward WebSocket error", exception);
        portForwardWatch.onFailure(exception);
    }

    /**
     * Writes binary data to a forwarded port.
     * The data will be sent to the corresponding port in the pod.
     *
     * @param port the port number
     * @param data the binary data to send
     * @throws WebSocketException if the send fails
     */
    public void writeData(final int port, final byte[] data) throws WebSocketException {
        if (webSocketClient == null) {
            throw new WebSocketException("WebSocket client not initialized");
        }

        if (!options.hasPort(port)) {
            throw new WebSocketException("Port " + port + " is not being forwarded");
        }

        // Find the channel for this port (data channels are even: 0, 2, 4, ...)
        final int channelId = getDataChannelForPort(port);

        // Frame the message: [channel_id][data...]
        final byte[] frame = new byte[data.length + 1];
        frame[0] = (byte) channelId;
        System.arraycopy(data, 0, frame, 1, data.length);

        webSocketClient.sendBinary(frame);
    }

    /**
     * Closes the WebSocket connection.
     */
    public void close() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.close(1000, "Normal closure");
        }
    }

    private void handleDataChannel(final byte channelId, final byte[] payload) {
        final int port = getPortForChannel(channelId);
        if (port > 0) {
            portForwardWatch.onData(port, payload);
        } else {
            log.warn("Received data for unknown channel: {}", channelId);
        }
    }

    private void handleErrorChannel(final byte channelId, final byte[] payload) {
        final int port = getPortForChannel(channelId);
        if (port > 0) {
            final String error = new String(payload, StandardCharsets.UTF_8);
            portForwardWatch.onError(port, error);
        } else {
            log.warn("Received error for unknown channel: {}", channelId);
        }
    }

    /**
     * Maps a channel ID to the corresponding port number.
     * Channels are allocated in pairs per port (data=even, error=odd).
     *
     * @param channelId the channel ID
     * @return the port number, or -1 if not found
     */
    private int getPortForChannel(final int channelId) {
        final int portIndex = channelId / 2;
        if (portIndex >= 0 && portIndex < options.getPortCount()) {
            return options.getPorts()[portIndex];
        }
        return -1;
    }

    /**
     * Gets the data channel ID for a specific port.
     *
     * @param port the port number
     * @return the channel ID for data (even number)
     */
    private int getDataChannelForPort(final int port) {
        for (int i = 0; i < options.getPorts().length; i++) {
            if (options.getPorts()[i] == port) {
                return i * 2; // Data channels are even: 0, 2, 4, ...
            }
        }
        throw new IllegalArgumentException("Port " + port + " not found in options");
    }
}
