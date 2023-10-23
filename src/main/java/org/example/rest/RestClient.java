package org.example.rest;

public interface RestClient {

    void send(String url, RequestDto requestDto);
}
