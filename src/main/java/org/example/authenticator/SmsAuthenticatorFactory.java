package org.example.authenticator;

import org.example.Constants;
import org.example.rest.RestClient;
import org.example.rest.RestClientImpl;
import org.example.rest.SmsSender;
import org.example.rest.SmsSenderImpl;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class SmsAuthenticatorFactory implements AuthenticatorFactory {
    public static final String PROVIDER_ID = "sms-authenticator";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "SMS Authentication";
    }

    @Override
    public String getHelpText() {
        return "Validates an OTP sent via SMS to the users mobile phone";
    }

    @Override
    public String getReferenceCategory() {
        return "otp";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of(
                new ProviderConfigProperty(Constants.BASE_URL_SERVICE_1, "Адрес сервиса 1", "", ProviderConfigProperty.STRING_TYPE, "http://localhost:8081"),
                new ProviderConfigProperty(Constants.BASE_URL_SERVICE_2, "Адрес сервиса 2", "", ProviderConfigProperty.STRING_TYPE, "http://localhost:8081"),
                new ProviderConfigProperty(Constants.CODE_LENGTH, "Длина сгенерированного кода", "", ProviderConfigProperty.STRING_TYPE, 6),
                new ProviderConfigProperty(Constants.CODE_TTL, "Длительность действия кода (мс)", "", ProviderConfigProperty.STRING_TYPE, "20000")
        );
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        SmsSender smsSender = new SmsSenderImpl();
        RestClient restClient = new RestClientImpl();
        return new SmsAuthenticator(smsSender, restClient);
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
}
