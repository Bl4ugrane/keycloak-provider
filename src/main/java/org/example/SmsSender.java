package org.example;

import java.util.Map;

public interface SmsSender {

    String send(Map<String, String> config, String mobileNumber);
}
