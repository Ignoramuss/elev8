package io.elev8.resources.job;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

public final class JobManager extends AbstractResourceManager<Job> {

    public JobManager(final KubernetesClient client) {
        super(client, Job.class, "/apis/batch/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "jobs";
    }
}
