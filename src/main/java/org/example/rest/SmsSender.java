package org.example.rest;

import java.util.Map;

public interface SmsSender {

    String send(int length, String mobileNumber);
}
