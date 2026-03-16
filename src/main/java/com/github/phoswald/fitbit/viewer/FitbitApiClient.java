package com.github.phoswald.fitbit.viewer;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@Dependent
public class FitbitApiClient {

    @Inject
    @RestClient
    private FitbitProfileClient profileClient;

    public FitbitProfile getUserProfile(String accessToken) {
        var user = profileClient.getProfile(createAuthorizationHeader(accessToken)).user();
        return new FitbitProfile(user.displayName(), user.fullName(), user.age(), user.gender(), user.avatar());
    }

    private static String createAuthorizationHeader(String accessToken) {
        return "Bearer " + accessToken;
    }
}
