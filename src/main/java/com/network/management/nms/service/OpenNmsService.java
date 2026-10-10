package com.network.management.nms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class OpenNmsService {

    @Value("${nms.opennms.url}")
    private String baseUrl;

    @Value("${nms.opennms.username}")
    private String username;

    @Value("${nms.opennms.password}")
    private String password;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String getNodes() throws Exception {

        String credentials = username + ":" + password;

        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/rest/nodes"))
                .header("Authorization", "Basic " + encodedCredentials)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "OpenNMS returned HTTP " + response.statusCode()
            );
        }

        return response.body();
    }
}