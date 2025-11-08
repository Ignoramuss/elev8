package io.elev8.core.exec;

import io.elev8.core.websocket.WebSocketClient;
import io.elev8.core.websocket.WebSocketException;
import io.elev8.core.websocket.WebSocketListener;
import lombok.extern.slf4j.Slf4j;

/**
 * Adapter that bridges WebSocket events to ExecWatch callbacks.
 * Handles channel multiplexing protocol and exit code extraction.
 */
@Slf4j
public class ExecWebSocketAdapter implements WebSocketListener {
    private final ExecWatch execWatch;
    private final ExecOptions options;
    private WebSocketClient webSocketClient;
    private boolean exitCodeReceived = false;

    public ExecWebSocketAdapter(final ExecWatch execWatch, final ExecOptions options) {
        this.execWatch = execWatch;
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
        log.debug("Exec WebSocket connection opened");
    }

    @Override
    public void onMessage(final byte[] data) {
        try {
            final ChannelMessage message = ChannelMessage.fromFrame(data);

            if (message.isStdout()) {
                handleStdout(message);
            } else if (message.isStderr()) {
                handleStderr(message);
            } else if (message.isError()) {
                handleError(message);
            } else if (message.isResize()) {
                log.debug("Received RESIZE channel message (ignored)");
            } else {
                log.warn("Received message on unknown channel: {}", message.getChannel());
            }
        } catch (Exception e) {
            log.error("Error processing WebSocket message", e);
            execWatch.onFailure(new Exception("Failed to process exec message", e));
        }
    }

    @Override
    public void onMessage(final String text) {
        log.warn("Received unexpected text message (exec protocol uses binary): {}", text);
    }

    @Override
    public void onClose(final int code, final String reason) {
        log.debug("Exec WebSocket connection closed: code={}, reason={}", code, reason);

        // If exit code wasn't received via ERROR channel, default to 0 for normal close
        if (!exitCodeReceived) {
            final int exitCode = (code == 1000) ? 0 : 1; // 1000 = normal closure
            execWatch.onClose(exitCode);
        }
    }

    @Override
    public void onError(final Exception exception) {
        log.error("Exec WebSocket error", exception);
        execWatch.onFailure(exception);
    }

    /**
     * Writes data to the STDIN channel.
     * Only works if ExecOptions.stdin was set to true.
     *
     * @param data the data to write to STDIN
     * @throws WebSocketException if the send fails
     */
    public void writeStdin(final String data) throws WebSocketException {
        if (webSocketClient == null) {
            throw new WebSocketException("WebSocket client not initialized");
        }

        if (!Boolean.TRUE.equals(options.getStdin())) {
            throw new WebSocketException("STDIN not enabled (set ExecOptions.stdin to true)");
        }

        final ChannelMessage message = ChannelMessage.stdin(data);
        webSocketClient.sendBinary(message.toFrame());
    }

    /**
     * Closes the WebSocket connection.
     */
    public void close() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.close(1000, "Normal closure");
        }
    }

    private void handleStdout(final ChannelMessage message) {
        // In TTY mode, stderr is merged into stdout
        // In non-TTY mode, only route stdout channel to onStdout
        execWatch.onStdout(message.getDataAsString());
    }

    private void handleStderr(final ChannelMessage message) {
        // STDERR should only be received in non-TTY mode
        if (Boolean.TRUE.equals(options.getTty())) {
            log.warn("Received STDERR in TTY mode (should be merged into STDOUT)");
        }
        execWatch.onStderr(message.getDataAsString());
    }

    private void handleError(final ChannelMessage message) {
        final String errorData = message.getDataAsString();

        try {
            // Try to parse as ExecStatus to extract exit code
            final ExecStatus status = ExecStatus.fromJson(errorData);

            if (status.getDetails() != null && status.getDetails().getCauses() != null) {
                final boolean hasExitCode = status.getDetails().getCauses().stream()
                        .anyMatch(cause -> "ExitCode".equals(cause.getReason()));

                if (hasExitCode) {
                    final int exitCode = status.getExitCode();
                    exitCodeReceived = true;
                    log.debug("Received exit code: {}", exitCode);
                    execWatch.onClose(exitCode);
                    return;
                }
            }

            // If no exit code, treat as regular error message
            execWatch.onError(errorData);
        } catch (Exception e) {
            // If JSON parsing fails, treat as plain error message
            log.debug("ERROR channel data is not JSON Status, treating as plain error: {}", errorData);
            execWatch.onError(errorData);
        }
    }
}
