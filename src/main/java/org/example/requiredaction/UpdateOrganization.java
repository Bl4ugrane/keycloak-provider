package org.example.requiredaction;

import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.example.Constants;
import org.example.exception.InvalidInnException;
import org.example.rest.RequestDto;
import org.example.rest.RestClient;
import org.example.rest.RestClientImpl;
import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;

import java.util.Map;
import java.util.UUID;

@Slf4j
public class UpdateOrganization implements RequiredActionProvider, RequiredActionFactory {
    private static final String OWNER = "owner";
    private static final String ADMIN = "admin";
    private static final String UPDATE_ORGANIZATION_FORM = "update-organization.ftl";

    private RestClient restClient;

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        String clientId = context.getAuthenticationSession().getClient().getClientId();
        UserModel user = context.getUser();
        boolean isAdmin = user.getRoleMappingsStream().anyMatch(roleModel -> ADMIN.equalsIgnoreCase(roleModel.getName()));
        if (OWNER.equalsIgnoreCase(clientId) && isAdmin && !user.getAttributes().containsKey(Constants.ORGANIZATION_ID)) {
            context.getUser().addRequiredAction(Constants.UPDATE_ORGANIZATION);
            log.info("User {} is required to update organization", user.getUsername());
        }
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        context.challenge(context.form().createForm(UPDATE_ORGANIZATION_FORM));
    }

    @Override
    public void processAction(RequiredActionContext context) {
        try {
            Map<String, String> config = resolveConfig(context.getSession());
            String url = config.get(Constants.BASE_URL_SERVICE_2);
            MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
            String organizationType = formData.getFirst(Constants.ORGANIZATION_TYPE);
            String inn = formData.getFirst(Constants.INN);
            String mobileNumber = formData.getFirst(Constants.MOBILE_NUMBER);

            if (mobileNumber != null && !mobileNumber.isEmpty() && !mobileNumber.matches(Constants.PHONE_PATTERN)) {
                createErrorPage(context, Constants.INCORRECT_MOBILE_NUMBER);
                return;
            }

            validateInn(organizationType, inn);

            UserModel user = context.getUser();
            String organizationId = UUID.randomUUID().toString();
            user.setSingleAttribute(Constants.ORGANIZATION_ID, organizationId);
            log.info("UpdateOrganizationAuthenticator.processAction: user -- {}, organizationId -- {}, organizationType -- {}, inn -- {}, mobileNumber -- {}",
                    user.getUsername(), organizationId, organizationType, inn, mobileNumber);
            RequestDto request = new RequestDto(organizationId, organizationType, inn, mobileNumber);
            restClient.send(url, request);
            context.success();
        } catch (InvalidInnException exception) {
            createErrorPage(context, exception.getMessage());
        } catch (Exception exception) {
            log.error("auth error:", exception);
            createErrorPage(context, Constants.AUTHORIZE_ERROR);
        }
    }

    @Override
    public void close() {
        // not used
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {
        restClient = new RestClientImpl();
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // not used
    }

    @Override
    public String getDisplayText() {
        return "Update Organization";
    }


    @Override
    public String getId() {
        return Constants.UPDATE_ORGANIZATION;
    }

    private void validateInn(String organizationType, String inn) throws InvalidInnException {
        if (inn == null || inn.isEmpty()) {
            throw new InvalidInnException("Поле 'ИНН' не должно быть пустым");
        }
        if (!inn.matches("[0-9]+")) {
            throw new InvalidInnException("Поле 'ИНН' должно содержать только цифры");
        }
        if ("IP".equalsIgnoreCase(organizationType) && inn.length() != 12) {
            throw new InvalidInnException("Поле 'ИНН' должно содержать 12 цифр");
        }
        if ("OOO".equalsIgnoreCase(organizationType) && inn.length() != 10) {
            throw new InvalidInnException("Поле 'ИНН' должно содержать 10 цифр");
        }
    }

    private void createErrorPage(RequiredActionContext context, String errorMessage) {
        context.challenge(context.form().addError(new FormMessage(Constants.ERROR, errorMessage)).createForm(UPDATE_ORGANIZATION_FORM));
    }

    private static Map<String, String> resolveConfig(KeycloakSession keycloakSession) {
        AuthenticatorConfigModel config = keycloakSession.getContext().getRealm().getAuthenticatorConfigByAlias("sms");
        return config.getConfig();
    }
}
