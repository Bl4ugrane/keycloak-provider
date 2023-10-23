package org.example.listener;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class EventListenerFactory implements EventListenerProviderFactory {
    private static final String ID = "custom-listener";

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new EventListener(session);
    }

    @Override
    public void init(Config.Scope config) {
        // not used
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // not used
    }

    @Override
    public void close() {
        // not used
    }

    @Override
    public String getId() {
        return ID;
    }
}
