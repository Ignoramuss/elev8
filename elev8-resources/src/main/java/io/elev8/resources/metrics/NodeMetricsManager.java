package io.elev8.resources.metrics;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractReadOnlyClusterResourceManager;

/**
 * Resource manager for Kubernetes NodeMetrics resources from the Metrics API.
 *
 * <p>NodeMetrics provides read-only access to current resource usage metrics for nodes.
 * This manager supports list and get operations only; create, update, delete,
 * patch, and apply operations are not supported.</p>
 *
 * <p>API Path: /apis/metrics.k8s.io/v1beta1/nodes</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * NodeMetricsManager manager = new NodeMetricsManager(client);
 *
 * // List all node metrics in the cluster
 * List<NodeMetrics> metrics = manager.list();
 *
 * // Get metrics for a specific node
 * NodeMetrics nodeMetrics = manager.get("ip-192-168-1-100.ec2.internal");
 * }</pre>
 */
public final class NodeMetricsManager extends AbstractReadOnlyClusterResourceManager<NodeMetrics> {

    private static final String API_PATH = "/apis/metrics.k8s.io/v1beta1";

    public NodeMetricsManager(final KubernetesClient client) {
        super(client, NodeMetrics.class, API_PATH);
    }

    @Override
    protected String getResourceTypePlural() {
        return "nodes";
    }
}
