package io.elev8.resources.pod;

import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientException;
import io.elev8.core.exec.ExecOptions;
import io.elev8.core.exec.ExecWatch;
import io.elev8.core.http.HttpClient;
import io.elev8.core.logs.LogOptions;
import io.elev8.core.logs.LogWatch;
import io.elev8.resources.AbstractResourceManager;
import io.elev8.resources.ResourceException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PodManager extends AbstractResourceManager<Pod> {

    public PodManager(final KubernetesClient client) {
        super(client, Pod.class, "/api/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "pods";
    }

    /**
     * Stream logs from a pod's container.
     * For single-container pods, the container name is optional.
     * For multi-container pods, specify the container name in options.
     *
     * @param namespace the namespace of the pod
     * @param podName the name of the pod
     * @param options log options for configuring the log behavior
     * @param logWatch the callback to handle log lines
     * @throws ResourceException if the log streaming fails
     */
    public void logs(final String namespace, final String podName, final LogOptions options, final LogWatch logWatch)
            throws ResourceException {
        try {
            final String path = buildNamespacePath(namespace) + "/" + podName + "/log";
            log.debug("Streaming logs from pod at path: {}", path);

            final HttpClient.StreamHandler handler = new HttpClient.StreamHandler() {
                @Override
                public void onLine(final String line) {
                    logWatch.onLog(line);
                }

                @Override
                public void onError(final Exception exception) {
                    logWatch.onError(exception);
                }

                @Override
                public void onClose() {
                    logWatch.onClose();
                }
            };

            client.logs(path, options, handler);

        } catch (KubernetesClientException e) {
            throw new ResourceException("Failed to stream pod logs", e);
        }
    }

    /**
     * Stream logs from a specific container in a pod.
     * Convenience method for multi-container pods.
     *
     * @param namespace the namespace of the pod
     * @param podName the name of the pod
     * @param containerName the name of the container
     * @param options log options for configuring the log behavior
     * @param logWatch the callback to handle log lines
     * @throws ResourceException if the log streaming fails
     */
    public void logs(final String namespace, final String podName, final String containerName,
                    final LogOptions options, final LogWatch logWatch) throws ResourceException {
        final LogOptions containerOptions = LogOptions.builder()
                .follow(options != null ? options.getFollow() : false)
                .tailLines(options != null ? options.getTailLines() : null)
                .timestamps(options != null ? options.getTimestamps() : false)
                .sinceSeconds(options != null ? options.getSinceSeconds() : null)
                .sinceTime(options != null ? options.getSinceTime() : null)
                .previous(options != null ? options.getPrevious() : false)
                .limitBytes(options != null ? options.getLimitBytes() : null)
                .container(containerName)
                .build();

        logs(namespace, podName, containerOptions, logWatch);
    }

    /**
     * Execute a command in a pod's container.
     * Note: This is a foundational API implementation. Full WebSocket-based
     * bidirectional streaming will be completed in subsequent iterations.
     *
     * @param namespace the namespace of the pod
     * @param podName the name of the pod
     * @param options exec options for configuring the command execution
     * @param execWatch the callback to handle exec streams
     * @throws ResourceException if the exec operation fails
     */
    public void exec(final String namespace, final String podName, final ExecOptions options, final ExecWatch execWatch)
            throws ResourceException {
        if (options == null) {
            throw new ResourceException("ExecOptions are required for exec operations");
        }

        options.validate();

        log.debug("Exec into pod: {}/{} with command: {}",
                namespace, podName, String.join(" ", options.getCommand()));

        throw new ResourceException(
                "Exec functionality requires WebSocket infrastructure - implementation in progress. " +
                "API structure is defined and ready for WebSocket integration."
        );
    }

    /**
     * Execute a command in a specific container of a pod.
     * Convenience method for multi-container pods.
     *
     * @param namespace the namespace of the pod
     * @param podName the name of the pod
     * @param containerName the name of the container
     * @param command the command to execute
     * @param execWatch the callback to handle exec streams
     * @throws ResourceException if the exec operation fails
     */
    public void exec(final String namespace, final String podName, final String containerName,
                    final String[] command, final ExecWatch execWatch) throws ResourceException {
        final ExecOptions options = ExecOptions.inContainer(command, containerName);
        exec(namespace, podName, options, execWatch);
    }
}
