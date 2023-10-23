package org.example.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.Constants;
import org.example.rest.SmsSender;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;

@Slf4j
@RequiredArgsConstructor
public class SmsAuthenticator implements Authenticator {
    private static final String LOGIN_FORM = "login.ftl";
    private static final String VALIDATE_CODE_FORM = "validate-code.ftl";
    private static final String INVALID_CODE = "Введенный код неверен";
    private static final String CODE_IS_EXPIRED = "Действие кода закончилось";
    private static final String MOBILE_NUMBER_IS_EMPTY = "Номер телефона должен быть заполнен";
    private final SmsSender smsSender;

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        context.challenge(context.form().createForm(LOGIN_FORM));
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        try {
            MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
            String mobileNumber = formData.getFirst(Constants.MOBILE_NUMBER);
            String enteredCode = formData.getFirst(Constants.CODE);
            log.info("SmsAuthenticator.action: mobileNumber -- {}, enteredCode -- {}", mobileNumber, enteredCode);

            if (enteredCode != null) {

                String generatedCode = context.getAuthenticationSession().getAuthNote(Constants.CODE);

                if (validateCode(enteredCode, generatedCode)) {
                    long ttl = Long.parseLong(context.getAuthenticationSession().getAuthNote(Constants.CODE_TTL));
                    long startTime = Long.parseLong(context.getAuthenticationSession().getAuthNote(Constants.START_TIME));
                    long duration = ttl - (System.currentTimeMillis() - startTime);

                    if (duration < 0) {
                        createErrorPage(context, CODE_IS_EXPIRED, LOGIN_FORM);
                        return;
                    }

                    getOrCreateUser(context);
                    return;
                } else {
                    createErrorPage(context, INVALID_CODE, VALIDATE_CODE_FORM);
                }
                return;
            }

            if (mobileNumber == null || mobileNumber.isEmpty()) {
                createErrorPage(context, MOBILE_NUMBER_IS_EMPTY, LOGIN_FORM);
            } else {
                if (!mobileNumber.matches(Constants.PHONE_PATTERN)) {
                    createErrorPage(context, Constants.INCORRECT_MOBILE_NUMBER, LOGIN_FORM);
                    return;
                }
                sendData(context, mobileNumber);
            }
        } catch (Exception exception) {
            log.error("auth error:", exception);
            createErrorPage(context, Constants.AUTHORIZE_ERROR, LOGIN_FORM);
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // not used
    }

    @Override
    public void close() {
        // not used
    }

    private void sendData(AuthenticationFlowContext context, String mobileNumber) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        String url = config.getConfig().get(Constants.URL);
        String ttl = config.getConfig().get(Constants.CODE_TTL);
        int length = Integer.parseInt(config.getConfig().get(Constants.CODE_LENGTH));

        String code = smsSender.send(length, mobileNumber);

        context.getAuthenticationSession().setAuthNote(Constants.URL, url);
        context.getAuthenticationSession().setAuthNote(Constants.CODE, code);
        context.getAuthenticationSession().setAuthNote(Constants.CODE_TTL, ttl);
        context.getAuthenticationSession().setAuthNote(Constants.START_TIME, String.valueOf(System.currentTimeMillis()));
        context.getAuthenticationSession().setAuthNote(Constants.MOBILE_NUMBER, mobileNumber);
        context.challenge(context.form().createForm(VALIDATE_CODE_FORM));
    }

    private boolean validateCode(String enteredCode, String generatedCode) {
        return enteredCode.equals(generatedCode);
    }

    private void getOrCreateUser(AuthenticationFlowContext context) {
        KeycloakSession session = context.getSession();
        String mobileNumber = context.getAuthenticationSession().getAuthNote(Constants.MOBILE_NUMBER);
        UserModel user = session.users().getUserByUsername(context.getRealm(), mobileNumber);
        if (user == null) {
            user = session.users().addUser(context.getRealm(), mobileNumber);
            log.info("user not found: {}, mobileNumber {}", user.getUsername(), mobileNumber);
            user.setEnabled(true);
        }
        context.setUser(user);
        context.getEvent().detail(Details.USERNAME, user.getUsername());
        context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, user.getUsername());
        context.success();
    }

    private void createErrorPage(AuthenticationFlowContext context, String errorMessage, String page) {
        context.challenge(context.form().addError(new FormMessage(Constants.ERROR, errorMessage)).createForm(page));
    }

}
