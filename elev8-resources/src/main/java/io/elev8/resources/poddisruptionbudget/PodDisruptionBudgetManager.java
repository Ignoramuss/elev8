package io.elev8.resources.poddisruptionbudget;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

/**
 * Manager for PodDisruptionBudget resources.
 * Provides CRUD operations for Kubernetes PodDisruptionBudgets in the policy/v1 API group.
 *
 * PodDisruptionBudget defines the maximum disruption allowed for a collection of pods,
 * enabling controlled voluntary evictions during cluster operations like node drains or upgrades.
 */
public final class PodDisruptionBudgetManager extends AbstractResourceManager<PodDisruptionBudget> {

    public PodDisruptionBudgetManager(final KubernetesClient client) {
        super(client, PodDisruptionBudget.class, "/apis/policy/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "poddisruptionbudgets";
    }
}
