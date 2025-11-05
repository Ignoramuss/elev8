package io.elev8.core.logs;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration options for pod log streaming operations.
 * These options control how logs are retrieved and streamed from pod containers.
 */
@Getter
@Builder
public class LogOptions {
    /**
     * Follow the log stream (tail -f behavior).
     * If true, logs will be streamed continuously as they are written.
     */
    @Builder.Default
    private final Boolean follow = false;

    /**
     * Number of lines to show from the end of the logs.
     * If specified, only the last N lines will be returned.
     */
    private final Integer tailLines;

    /**
     * Include timestamps in the log output.
     * If true, each log line will be prefixed with an RFC3339 timestamp.
     */
    @Builder.Default
    private final Boolean timestamps = false;

    /**
     * Return logs newer than a relative duration in seconds.
     * For example, 3600 for logs from the last hour.
     */
    private final Integer sinceSeconds;

    /**
     * Return logs after a specific time (RFC3339 timestamp).
     * Example: "2024-01-01T00:00:00Z"
     */
    private final String sinceTime;

    /**
     * The container name for which to stream logs.
     * Required for multi-container pods, optional for single-container pods.
     */
    private final String container;

    /**
     * Return logs from the previous terminated container.
     * Useful for debugging crashed containers.
     */
    @Builder.Default
    private final Boolean previous = false;

    /**
     * Limit the number of bytes returned.
     * If specified, only the last N bytes will be returned.
     */
    private final Long limitBytes;

    /**
     * Creates a default LogOptions instance with standard settings.
     *
     * @return a new LogOptions with default values
     */
    public static LogOptions defaults() {
        return LogOptions.builder().build();
    }

    /**
     * Creates a LogOptions instance for following (streaming) logs.
     *
     * @return a new LogOptions configured for log streaming
     */
    public static LogOptions follow() {
        return LogOptions.builder()
                .follow(true)
                .build();
    }

    /**
     * Creates a LogOptions instance to tail the last N lines.
     *
     * @param lines the number of lines to tail
     * @return a new LogOptions configured to tail N lines
     */
    public static LogOptions tail(final int lines) {
        return LogOptions.builder()
                .tailLines(lines)
                .build();
    }

    /**
     * Creates a LogOptions instance to follow logs with timestamps.
     *
     * @return a new LogOptions configured for streaming with timestamps
     */
    public static LogOptions followWithTimestamps() {
        return LogOptions.builder()
                .follow(true)
                .timestamps(true)
                .build();
    }

    /**
     * Creates a LogOptions instance for logs from a specific container.
     *
     * @param containerName the container name
     * @return a new LogOptions configured for the specified container
     */
    public static LogOptions forContainer(final String containerName) {
        return LogOptions.builder()
                .container(containerName)
                .build();
    }

    /**
     * Creates a LogOptions instance for previous container logs.
     *
     * @return a new LogOptions configured for previous container logs
     */
    public static LogOptions previous() {
        return LogOptions.builder()
                .previous(true)
                .build();
    }
}
