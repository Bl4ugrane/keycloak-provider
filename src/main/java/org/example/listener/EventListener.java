package org.example.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.Constants;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class EventListener implements EventListenerProvider {
    private final KeycloakSession session;

    @Override
    public void onEvent(Event event) {
        if (Objects.requireNonNull(event.getType()) == EventType.UPDATE_PASSWORD) {
            RealmModel realm = session.getContext().getRealm();
            UserModel user = session.users().getUserById(realm, event.getUserId());
            String organizationId = user.getFirstAttribute(Constants.ORGANIZATION_ID);
            log.info("update password for user: {}, organizationId {}", user.getUsername(), organizationId);
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        // not used
    }

    @Override
    public void close() {
        // not used
    }
}
