package org.example;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
    /**
     * User required actions variables
     */
    public String VERIFY_EMAIL = "Custom Verify Email";
    public String VERIFY_EMAIL_KEY = "VERIFY_EMAIL_KEY";
    public String UPDATE_ORGANIZATION = "Update Organization";

    /**
     * User attributes
     */
    public String INN = "inn";
    public String MOBILE_NUMBER = "mobile_number";
    public String ORGANIZATION_ID = "organizationId";
    public String ORGANIZATION_TYPE = "organization_type";

    /**
     * OTP variables
     */
    public String CODE = "code";
    public String CODE_TTL = "ttl";
    public String CODE_LENGTH = "length";
    public String START_TIME = "start_time";

    /**
     * Other variables
     */
    public String URL = "url";
    public String ERROR = "error";

    /**
     * Patterns
     */
    public String PHONE_PATTERN = "(^8|7|\\+7)((\\d{10})|(\\s\\(\\d{3}\\)\\s\\d{3}\\s\\d{2}\\s\\d{2}))";

    /**
     * Errors
     */
    public String INCORRECT_MOBILE_NUMBER = "Номер телефона не соответствует формату";
    public String AUTHORIZE_ERROR = "Возникла ошибка при авторизации пользователя";
}
