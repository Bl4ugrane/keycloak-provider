package org.example.actiontoken;

import jakarta.ws.rs.core.Response;
import org.keycloak.TokenVerifier.Predicate;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.authentication.actiontoken.AbstractActionTokenHandler;
import org.keycloak.authentication.actiontoken.ActionTokenContext;
import org.keycloak.authentication.actiontoken.TokenUtils;
import org.keycloak.authentication.requiredactions.util.RequiredActionsValidator;
import org.keycloak.events.Errors;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.Objects;

import static org.keycloak.models.utils.DefaultRequiredActions.getDefaultRequiredActionCaseInsensitively;

public class CustomActionTokenHandler extends AbstractActionTokenHandler<CustomActionToken> {

    public CustomActionTokenHandler() {
        super(CustomActionToken.TOKEN_TYPE, CustomActionToken.class, Messages.INVALID_CODE, EventType.EXECUTE_ACTIONS, Errors.NOT_ALLOWED);
    }

    @Override
    public Predicate<? super CustomActionToken>[] getVerifiers(ActionTokenContext<CustomActionToken> tokenContext) {
        return TokenUtils.predicates(
                TokenUtils.checkThat(
                        t -> t.getRedirectUri() == null
                                || RedirectUtils.verifyRedirectUri(tokenContext.getSession(), t.getRedirectUri(),
                                tokenContext.getAuthenticationSession().getClient()) != null,
                        Errors.INVALID_REDIRECT_URI,
                        Messages.INVALID_REDIRECT_URI
                ),
                verifyEmail(tokenContext),
                verifyRequiredActions(tokenContext)
        );
    }

    @Override
    public Response handleToken(CustomActionToken token, ActionTokenContext<CustomActionToken> tokenContext) {
        AuthenticationSessionModel authSession = tokenContext.getAuthenticationSession();
        String redirectUri = RedirectUtils.verifyRedirectUri(tokenContext.getSession(), token.getRedirectUri(), authSession.getClient());

        if (redirectUri != null) {
            authSession.setAuthNote(AuthenticationManager.SET_REDIRECT_URI_AFTER_REQUIRED_ACTIONS, "true");

            authSession.setRedirectUri(redirectUri);
            authSession.setClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM, redirectUri);
        }

        token.getRequiredActions().forEach(authSession::addRequiredAction);

        UserModel user = tokenContext.getAuthenticationSession().getAuthenticatedUser();
        user.setEmailVerified(true);

        String nextAction = AuthenticationManager.nextRequiredAction(tokenContext.getSession(), authSession, tokenContext.getRequest(), tokenContext.getEvent());
        return AuthenticationManager.redirectToRequiredActions(tokenContext.getSession(), tokenContext.getRealm(), authSession, tokenContext.getUriInfo(), nextAction);
    }

    @Override
    public boolean canUseTokenRepeatedly(CustomActionToken token, ActionTokenContext<CustomActionToken> tokenContext) {
        RealmModel realm = tokenContext.getRealm();
        KeycloakSessionFactory sessionFactory = tokenContext.getSession().getKeycloakSessionFactory();
        return token.getRequiredActions().stream()
                .map(realm::getRequiredActionProviderByAlias)
                .filter(Objects::nonNull)
                .filter(RequiredActionProviderModel::isEnabled)
                .map(RequiredActionProviderModel::getProviderId)
                .map(providerId -> (RequiredActionFactory) sessionFactory.getProviderFactory(RequiredActionProvider.class, getDefaultRequiredActionCaseInsensitively(providerId)))
                .filter(Objects::nonNull)
                .noneMatch(RequiredActionFactory::isOneTimeAction);
    }

    protected Predicate<CustomActionToken> verifyRequiredActions(ActionTokenContext<CustomActionToken> tokenContext) {
        return TokenUtils.checkThat(t -> RequiredActionsValidator.validRequiredActions(tokenContext.getSession(), t.getRequiredActions()),
                Errors.RESOLVE_REQUIRED_ACTIONS, Messages.INVALID_TOKEN_REQUIRED_ACTIONS);
    }
}
