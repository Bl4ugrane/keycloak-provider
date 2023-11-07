package org.example.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class RestClientImpl implements RestClient {
    private final ObjectMapper mapper;
    private final HttpClient httpClient;

    public RestClientImpl() {
        this.mapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    }

    @Override
    @SneakyThrows
    public void send(String url, RequestDto requestDto) {
        try {
            String requestBody = mapper.writeValueAsString(requestDto);
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            log.info("send request to url -- {}, body -- {}", url, requestDto);
            httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception exception) {
            log.error("couldn't send request to url -- {}", url, exception);
            throw exception;
        }
    }
}
