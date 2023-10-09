package org.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.SecretGenerator;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SmsSenderImpl implements SmsSender {

    private final RestClient restClient;

    @Override
    public String send(Map<String, String> config, String mobileNumber) {
        String url = config.get(Constants.URL);
        int length = Integer.parseInt(config.get(Constants.CODE_LENGTH));
        String code = SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
        log.warn("***** SIMULATION MODE ***** Would send SMS to {} with code: {}", mobileNumber, code);
        restClient.send(url, new RequestDto(mobileNumber, code));
        return code;
    }
}
