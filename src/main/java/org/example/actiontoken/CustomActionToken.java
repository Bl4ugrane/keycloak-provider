package org.example.actiontoken;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.keycloak.authentication.actiontoken.DefaultActionToken;

import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomActionToken extends DefaultActionToken {
    public static final String TOKEN_TYPE = "custom-execute-actions";
    private static final String JSON_FIELD_REQUIRED_ACTIONS = "rqac";
    private static final String JSON_FIELD_REDIRECT_URI = "reduri";


    public CustomActionToken(String userId, int absoluteExpirationInSecs, List<String> requiredActions, String redirectUri, String clientId) {
        super(userId, TOKEN_TYPE, absoluteExpirationInSecs, null);
        this.setRequiredActions(requiredActions == null ? new LinkedList<>() : new LinkedList<>(requiredActions));
        this.setRedirectUri(redirectUri);
        this.issuedFor = clientId;
    }

    public CustomActionToken(String userId, String email, int absoluteExpirationInSecs, List<String> requiredActions, String redirectUri, String clientId) {
        this(userId, absoluteExpirationInSecs, requiredActions, redirectUri, clientId);
        this.setEmail(email);
    }

    @JsonProperty("rqac")
    public List<String> getRequiredActions() {
        return (List) this.getOtherClaims().get(JSON_FIELD_REQUIRED_ACTIONS);
    }

    @JsonProperty("rqac")
    public final void setRequiredActions(List<String> requiredActions) {
        if (requiredActions == null) {
            this.getOtherClaims().remove(JSON_FIELD_REQUIRED_ACTIONS);
        } else {
            this.setOtherClaims(JSON_FIELD_REQUIRED_ACTIONS, requiredActions);
        }
    }

    @JsonProperty("reduri")
    public String getRedirectUri() {
        return (String) this.getOtherClaims().get(JSON_FIELD_REDIRECT_URI);
    }

    @JsonProperty("reduri")
    public final void setRedirectUri(String redirectUri) {
        if (redirectUri == null) {
            this.getOtherClaims().remove(JSON_FIELD_REDIRECT_URI);
        } else {
            this.setOtherClaims(JSON_FIELD_REDIRECT_URI, redirectUri);
        }
    }
}
