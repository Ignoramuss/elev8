package io.elev8.resources.verticalpodautoscaler;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for VerticalPodAutoscaler resources.
 * Provides CRUD operations for Kubernetes VerticalPodAutoscalers in the autoscaling.k8s.io/v1 API group.
 *
 * VerticalPodAutoscaler automatically adjusts the CPU and memory requests (and optionally limits)
 * for containers in pods based on observed usage patterns. This helps optimize resource allocation
 * and prevents over-provisioning or under-provisioning of resources.
 *
 * NOTE: VPA is a Custom Resource Definition (CRD) and must be installed in the cluster before use.
 * See: https://github.com/kubernetes/autoscaler/tree/master/vertical-pod-autoscaler#installation
 */
public final class VerticalPodAutoscalerManager extends AbstractResourceManager<VerticalPodAutoscaler> {

    public VerticalPodAutoscalerManager(final KubernetesClient client) {
        super(client, VerticalPodAutoscaler.class, "/apis/autoscaling.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "verticalpodautoscalers";
    }
}
