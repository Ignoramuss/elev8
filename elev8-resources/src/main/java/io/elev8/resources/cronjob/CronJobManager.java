package io.elev8.resources.cronjob;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

public final class CronJobManager extends AbstractResourceManager<CronJob> {

    public CronJobManager(final KubernetesClient client) {
        super(client, CronJob.class, "/apis/batch/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "cronjobs";
    }
}
