package org.example.rest;

public interface SmsSender {

    String send(int length, String mobileNumber);
}
