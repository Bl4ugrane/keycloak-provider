package org.example.rest;

import org.example.rest.VerifyEmailProvider;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class VerifyEmailProviderFactory implements RealmResourceProviderFactory {
    public static final String ID = "api";

    public RealmResourceProvider create(KeycloakSession session) {
        return new VerifyEmailProvider(session);
    }

    public void init(Scope config) {
        // not used
    }

    public void postInit(KeycloakSessionFactory factory) {
        // not used
    }

    public void close() {
        // not used
    }

    public String getId() {
        return ID;
    }

}
