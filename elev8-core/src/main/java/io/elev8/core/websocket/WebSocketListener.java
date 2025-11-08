package io.elev8.core.websocket;

/**
 * Callback interface for WebSocket connection events.
 * Implementations receive notifications for connection lifecycle and messages.
 */
public interface WebSocketListener {
    /**
     * Called when the WebSocket connection is successfully opened.
     */
    void onOpen();

    /**
     * Called when a binary message is received.
     *
     * @param data the binary message data
     */
    void onMessage(byte[] data);

    /**
     * Called when a text message is received.
     *
     * @param text the text message
     */
    default void onMessage(String text) {
        // Default implementation does nothing
    }

    /**
     * Called when the WebSocket connection is closed.
     *
     * @param code the close code
     * @param reason the close reason
     */
    void onClose(int code, String reason);

    /**
     * Called when an error occurs.
     *
     * @param exception the error that occurred
     */
    void onError(Exception exception);
}
