package io.elev8.resources.metrics;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractReadOnlyResourceManager;

/**
 * Resource manager for Kubernetes PodMetrics resources from the Metrics API.
 *
 * <p>PodMetrics provides read-only access to current resource usage metrics for pods.
 * This manager supports list and get operations only; create, update, delete,
 * patch, and apply operations are not supported.</p>
 *
 * <p>API Path: /apis/metrics.k8s.io/v1beta1/namespaces/{namespace}/pods</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * PodMetricsManager manager = new PodMetricsManager(client);
 *
 * // List all pod metrics in a namespace
 * List<PodMetrics> metrics = manager.list("default");
 *
 * // Get metrics for a specific pod
 * PodMetrics podMetrics = manager.get("default", "nginx-deployment-abc123");
 * }</pre>
 */
public final class PodMetricsManager extends AbstractReadOnlyResourceManager<PodMetrics> {

    private static final String API_PATH = "/apis/metrics.k8s.io/v1beta1";

    public PodMetricsManager(final KubernetesClient client) {
        super(client, PodMetrics.class, API_PATH);
    }

    @Override
    protected String getResourceTypePlural() {
        return "pods";
    }
}
