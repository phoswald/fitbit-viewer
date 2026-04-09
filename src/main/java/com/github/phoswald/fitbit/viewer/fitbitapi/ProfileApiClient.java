package com.github.phoswald.fitbit.viewer.fitbitapi;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "fitbit-api")
@Path("/1/user/-")
public interface ProfileApiClient {

    @GET
    @Path("/profile.json")
    @Produces(MediaType.APPLICATION_JSON)
    ProfileResponse getProfile(@HeaderParam("Authorization") String authorizationHeader);

    record ProfileResponse(
            UserData user
    ) { }

    record UserData(
            @JsonbProperty("encodedId") String userId,
            String displayName,
            String fullName,
            String avatar,
            String dateOfBirth,
            Integer age,
            String gender,
            String memberSince,
            Integer height,
            Integer weight,
            Double strideLengthWalking,
            Double strideLengthRunning,
            String averageDailySteps
    ) { }
}
