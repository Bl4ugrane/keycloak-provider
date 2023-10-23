package org.example.actiontoken;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.keycloak.authentication.actiontoken.DefaultActionToken;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomVerifyEmailActionToken extends DefaultActionToken {

    public static final String TOKEN_TYPE = "custom-verify-email";

    private static final String JSON_FIELD_ORIGINAL_AUTHENTICATION_SESSION_ID = "oasid";

    @JsonProperty(value = JSON_FIELD_ORIGINAL_AUTHENTICATION_SESSION_ID)
    private String compoundOriginalAuthenticationSessionId;

    public CustomVerifyEmailActionToken(String userId, int absoluteExpirationInSecs, String compoundAuthenticationSessionId, String email, String clientId) {
        super(userId, TOKEN_TYPE, absoluteExpirationInSecs, null, compoundAuthenticationSessionId);
        setEmail(email);
        this.issuedFor = clientId;
    }
}
