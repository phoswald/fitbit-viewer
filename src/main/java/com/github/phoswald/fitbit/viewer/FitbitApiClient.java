package com.github.phoswald.fitbit.viewer;

import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@Dependent
public class FitbitApiClient {

    @Inject
    @RestClient
    private FitbitProfileClient profileClient;

    @Inject
    @RestClient
    private FitbitStepsClient stepsClient;

    public FitbitProfile getUserProfile(String accessToken) {
        var user = profileClient.getProfile(createAuthorizationHeader(accessToken)).user();
        return new FitbitProfile(user.displayName(), user.fullName(), user.age(), user.gender(), user.avatar());
    }

    public List<FitbitStepsEntry> getSteps(String accessToken, String startDate, String endDate) {
        return stepsClient.getSteps(createAuthorizationHeader(accessToken), startDate, endDate)
                .activitiesSteps().stream()
                .map(e -> new FitbitStepsEntry(e.dateTime(), Integer.parseInt(e.value())))
                .toList();
    }

    private static String createAuthorizationHeader(String accessToken) {
        return "Bearer " + accessToken;
    }
}
