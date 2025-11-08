package io.elev8.core.websocket;

import java.util.Map;

/**
 * WebSocket client abstraction for Kubernetes API operations that require
 * bidirectional streaming (exec, port-forward, attach).
 */
public interface WebSocketClient {
    /**
     * Connect to a WebSocket endpoint with specified protocols.
     *
     * @param url the WebSocket URL (wss:// or ws://)
     * @param headers HTTP headers for the upgrade request
     * @param protocols WebSocket subprotocols to request (e.g., "v5.channel.k8s.io")
     * @param listener the listener to handle WebSocket events
     * @throws WebSocketException if the connection fails
     */
    void connect(String url, Map<String, String> headers, String[] protocols, WebSocketListener listener)
            throws WebSocketException;

    /**
     * Send binary data over the WebSocket connection.
     *
     * @param data the binary data to send
     * @throws WebSocketException if the send fails
     */
    void sendBinary(byte[] data) throws WebSocketException;

    /**
     * Send text data over the WebSocket connection.
     *
     * @param text the text data to send
     * @throws WebSocketException if the send fails
     */
    void sendText(String text) throws WebSocketException;

    /**
     * Close the WebSocket connection.
     *
     * @param code the close code
     * @param reason the close reason
     */
    void close(int code, String reason);

    /**
     * Check if the WebSocket connection is open.
     *
     * @return true if the connection is open
     */
    boolean isOpen();
}
