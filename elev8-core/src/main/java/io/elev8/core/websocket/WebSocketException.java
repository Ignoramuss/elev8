package io.elev8.core.websocket;

/**
 * Exception thrown when WebSocket operations fail.
 */
public class WebSocketException extends Exception {
    public WebSocketException(final String message) {
        super(message);
    }

    public WebSocketException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
