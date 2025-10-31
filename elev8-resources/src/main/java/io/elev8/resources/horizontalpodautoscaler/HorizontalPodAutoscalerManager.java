package io.elev8.resources.horizontalpodautoscaler;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for HorizontalPodAutoscaler resources.
 * Provides CRUD operations for Kubernetes HorizontalPodAutoscalers in the autoscaling/v2 API group.
 *
 * HorizontalPodAutoscaler automatically scales the number of pods in a deployment, replica set,
 * stateful set, or replication controller based on observed CPU utilization, memory, custom metrics,
 * or external metrics.
 */
public final class HorizontalPodAutoscalerManager extends AbstractResourceManager<HorizontalPodAutoscaler> {

    public HorizontalPodAutoscalerManager(final KubernetesClient client) {
        super(client, HorizontalPodAutoscaler.class, "/apis/autoscaling/v2");
    }

    @Override
    protected String getResourceTypePlural() {
        return "horizontalpodautoscalers";
    }
}
