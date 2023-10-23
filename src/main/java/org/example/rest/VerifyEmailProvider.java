package org.example.rest;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.Constants;
import org.example.actiontoken.CustomActionToken;
import org.example.exception.UserAlreadyExistsException;
import org.keycloak.authentication.actiontoken.verifyemail.VerifyEmailActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.theme.Theme;
import org.keycloak.theme.beans.LinkExpirationFormatterMethod;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class VerifyEmailProvider implements RealmResourceProvider {
    private final KeycloakSession session;

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {
        // not used
    }

    @POST
    @Path("invite-user")
    @SneakyThrows
    public Response inviteUser(@QueryParam("email") String email,
                               @QueryParam("organizationId") String organizationId) {
        try {
            RealmModel realm = session.getContext().getRealm();
            UserModel user = createUser(email, organizationId);
            int validityInSecs = realm.getActionTokenGeneratedByUserLifespan(VerifyEmailActionToken.TOKEN_TYPE);
            int absoluteExpirationInSecs = Time.currentTime() + validityInSecs;
            String generatedLink = generateVerificationLink(user, absoluteExpirationInSecs);

            EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("link", "Перейти по ссылке: " + generatedLink);
            attributes.put("linkExpiration", TimeUnit.SECONDS.toMinutes(validityInSecs));
            Locale locale = session.getContext().resolveLocale(user);
            attributes.put("linkExpirationFormatter", new LinkExpirationFormatterMethod(session.theme().getTheme(Theme.Type.EMAIL).getMessages(locale), locale));

            emailTemplateProvider.setRealm(realm).setUser(user)
                    .send("emailVerificationSubject", "email-verification.ftl", attributes);
        } catch (UserAlreadyExistsException exception) {
            return Response.status(Status.CONFLICT).entity(Map.of("message", exception.getMessage())).build();
        } catch (Exception exception) {
            return Response.serverError().build();
        }
        return Response.ok(Map.of("message", Response.Status.OK.getReasonPhrase())).build();
    }

    public UserModel createUser(String email, String organizationId) throws UserAlreadyExistsException {
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserByUsername(realm, email);
        if (user != null) throw new UserAlreadyExistsException("Данный пользователь уже приглашен");
        user = session.users().addUser(realm, email);
        user.setEnabled(true);
        user.setEmail(email);
        user.setSingleAttribute(Constants.ORGANIZATION_ID, organizationId);
        return user;
    }

    public String generateVerificationLink(UserModel user, int absoluteExpirationInSecs) {
        RealmModel realm = session.getContext().getRealm();
        List<String> actions = new LinkedList<>();
        actions.add(UserModel.RequiredAction.UPDATE_PASSWORD.name());

        if (user == null) {
            throw new WebApplicationException(ErrorResponse.error("User undefined", Status.BAD_REQUEST));
        }

        if (!user.isEnabled()) {
            throw new WebApplicationException(ErrorResponse.error("User is disabled", Status.BAD_REQUEST));
        }

        ClientModel client = realm.getClientByClientId("owner");

        if (client == null) {
            throw new WebApplicationException(ErrorResponse.error("Client doesn't exist", Status.BAD_REQUEST));
        }
        if (!client.isEnabled()) {
            throw new WebApplicationException(ErrorResponse.error("Client is not enabled", Status.BAD_REQUEST));
        }

        try {
            CustomActionToken token = new CustomActionToken(user.getId(), user.getEmail(), absoluteExpirationInSecs, actions, null, "owner");
            UriBuilder builder = LoginActionsService.actionTokenProcessor(session.getContext().getUri());
            builder.queryParam("key", token.serialize(session, realm, session.getContext().getUri()));
            builder.queryParam("client_id", client);
            return builder.build(realm.getName()).toString();
        } catch (Exception exception) {
            throw new WebApplicationException(ErrorResponse.error(exception.getMessage(), Status.BAD_REQUEST));
        }
    }
}
