package io.elev8.resources.event;

import io.elev8.core.client.KubernetesClient;
import io.elev8.resources.AbstractResourceManager;

public final class EventManager extends AbstractResourceManager<Event> {

    public EventManager(final KubernetesClient client) {
        super(client, Event.class, "/api/v1");
    }

    @Override
    protected String getResourceTypePlural() {
        return "events";
    }
}
