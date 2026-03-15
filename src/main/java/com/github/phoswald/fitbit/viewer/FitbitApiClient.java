package com.github.phoswald.fitbit.viewer;

import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class FitbitApiClient {

    @Inject
    private FitbitConfig config;

    @Inject
    @RestClient
    private FitbitTokenClient tokenClient;

    @Inject
    @RestClient
    private FitbitProfileClient profileClient;

    public String exchangeCode(String code) {
        String credentials = Base64.getEncoder().encodeToString(
                (config.getClientId() + ":" + config.getClientSecret()).getBytes(UTF_8));
        FitbitTokenClient.Response response = tokenClient.post(
                "Basic " + credentials, "authorization_code", code, config.getRedirectUri());
        return response.accessToken();
    }

    public FitbitProfile getUserProfile(String accessToken) {
        FitbitProfileClient.Response.UserData user = profileClient.get("Bearer " + accessToken).user();
        return new FitbitProfile(user.displayName(), user.fullName(), user.age(), user.gender(), user.avatarUrl());
    }
}
