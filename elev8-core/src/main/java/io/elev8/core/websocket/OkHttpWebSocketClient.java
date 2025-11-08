package io.elev8.core.websocket;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp-based implementation of WebSocketClient.
 */
@Slf4j
public final class OkHttpWebSocketClient implements WebSocketClient {
    private final OkHttpClient okHttpClient;
    private WebSocket webSocket;
    private volatile boolean isOpen = false;

    public OkHttpWebSocketClient(final OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @Override
    public void connect(final String url, final Map<String, String> headers, final String[] protocols,
                       final WebSocketListener listener) throws WebSocketException {
        final Request.Builder requestBuilder = new Request.Builder().url(url);

        if (headers != null) {
            headers.forEach(requestBuilder::addHeader);
        }

        if (protocols != null && protocols.length > 0) {
            final String protocolHeader = String.join(", ", protocols);
            requestBuilder.addHeader("Sec-WebSocket-Protocol", protocolHeader);
        }

        final Request request = requestBuilder.build();

        final okhttp3.WebSocketListener okHttpListener = new okhttp3.WebSocketListener() {
            @Override
            public void onOpen(final WebSocket webSocket, final Response response) {
                log.debug("WebSocket connection opened");
                isOpen = true;
                listener.onOpen();
            }

            @Override
            public void onMessage(final WebSocket webSocket, final ByteString bytes) {
                listener.onMessage(bytes.toByteArray());
            }

            @Override
            public void onMessage(final WebSocket webSocket, final String text) {
                listener.onMessage(text);
            }

            @Override
            public void onClosing(final WebSocket webSocket, final int code, final String reason) {
                log.debug("WebSocket connection closing: {} - {}", code, reason);
                isOpen = false;
            }

            @Override
            public void onClosed(final WebSocket webSocket, final int code, final String reason) {
                log.debug("WebSocket connection closed: {} - {}", code, reason);
                isOpen = false;
                listener.onClose(code, reason);
            }

            @Override
            public void onFailure(final WebSocket webSocket, final Throwable t, final Response response) {
                log.error("WebSocket connection failed", t);
                isOpen = false;
                listener.onError(new WebSocketException("WebSocket connection failed: " + t.getMessage(), t));
            }
        };

        try {
            webSocket = okHttpClient.newWebSocket(request, okHttpListener);
        } catch (Exception e) {
            throw new WebSocketException("Failed to connect WebSocket: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendBinary(final byte[] data) throws WebSocketException {
        if (webSocket == null || !isOpen) {
            throw new WebSocketException("WebSocket is not connected");
        }

        final boolean sent = webSocket.send(ByteString.of(data));
        if (!sent) {
            throw new WebSocketException("Failed to send binary data - buffer full");
        }
    }

    @Override
    public void sendText(final String text) throws WebSocketException {
        if (webSocket == null || !isOpen) {
            throw new WebSocketException("WebSocket is not connected");
        }

        final boolean sent = webSocket.send(text);
        if (!sent) {
            throw new WebSocketException("Failed to send text data - buffer full");
        }
    }

    @Override
    public void close(final int code, final String reason) {
        if (webSocket != null) {
            webSocket.close(code, reason);
            isOpen = false;
        }
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Creates an OkHttpWebSocketClient with the specified OkHttpClient.
     *
     * @param okHttpClient the OkHttpClient to use
     * @return a new OkHttpWebSocketClient instance
     */
    public static OkHttpWebSocketClient create(final OkHttpClient okHttpClient) {
        return new OkHttpWebSocketClient(okHttpClient);
    }

    /**
     * Creates an OkHttpWebSocketClient with default configuration.
     *
     * @return a new OkHttpWebSocketClient instance with default configuration
     */
    public static OkHttpWebSocketClient createDefault() {
        final OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
        return new OkHttpWebSocketClient(client);
    }
}
