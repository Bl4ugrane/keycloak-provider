package org.example.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.Constants;
import org.keycloak.common.util.SecretGenerator;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SmsSenderImpl implements SmsSender {

    @Override
    public String send(int length, String mobileNumber) {
        String code = SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
        log.warn("***** SIMULATION MODE ***** Would send SMS to {} with code: {}", mobileNumber, code);
        return code;
    }
}
