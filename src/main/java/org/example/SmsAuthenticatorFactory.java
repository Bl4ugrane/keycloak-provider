package org.example;

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
        return List.of(new ProviderConfigProperty(Constants.URL, "Адрес сервиса", "", ProviderConfigProperty.STRING_TYPE, "http://localhost:8081"),
                new ProviderConfigProperty(Constants.CODE_LENGTH, "Длина сгенерированного кода", "", ProviderConfigProperty.STRING_TYPE, 6),
                new ProviderConfigProperty(Constants.CODE_TTL, "Длительность действия кода (мс)", "", ProviderConfigProperty.STRING_TYPE, "20000")
        );
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        RestClient restClient = new RestClientImpl();
        SmsSender smsSender = new SmsSenderImpl(restClient);
        return new SmsAuthenticator(smsSender);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

}
