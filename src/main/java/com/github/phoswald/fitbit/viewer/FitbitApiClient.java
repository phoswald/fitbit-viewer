package com.github.phoswald.fitbit.viewer;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static com.github.phoswald.fitbit.viewer.FitbitApiConstants.*;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class FitbitApiClient { // TODO use managed REST client

    @Inject
    FitbitConfig config;

    @Inject
    ObjectMapper objectMapper;

    private HttpClient httpClient;

    @PostConstruct
    void init() {
        httpClient = HttpClient.newHttpClient();
    }

    public String exchangeCode(String code) {
        String credentials = Base64.getEncoder().encodeToString(
                (config.getClientId() + ":" + config.getClientSecret()).getBytes(UTF_8));
        String body = "grant_type=authorization_code"
                + "&code=" + URLEncoder.encode(code, UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(config.getRedirectUri(), UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());
            if (!json.has("access_token")) {
                throw new RuntimeException("Token exchange failed: " + response.body());
            }
            return json.get("access_token").asText();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Token exchange failed", e);
        }
    }

    public FitbitProfile getUserProfile(String accessToken) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROFILE_URL))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode user = objectMapper.readTree(response.body()).get("user");
            return new FitbitProfile(
                    user.path("displayName").asText(""),
                    user.path("fullName").asText(""),
                    user.path("age").asInt(0),
                    user.path("gender").asText(""),
                    user.path("avatar").asText(""));
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch user profile", e);
        }
    }
}
