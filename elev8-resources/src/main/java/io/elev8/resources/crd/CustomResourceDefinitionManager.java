package io.elev8.resources.crd;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractClusterResourceManager;

/**
 * Manager for CustomResourceDefinition resources.
 * CustomResourceDefinitions allow users to create new custom resources in the cluster.
 */
public final class CustomResourceDefinitionManager
        extends AbstractClusterResourceManager<CustomResourceDefinition> {

    public CustomResourceDefinitionManager(final KubernetesClient client) {
        super(client, CustomResourceDefinition.class, "/apis/apiextensions.k8s.io/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "customresourcedefinitions";
    }
}
