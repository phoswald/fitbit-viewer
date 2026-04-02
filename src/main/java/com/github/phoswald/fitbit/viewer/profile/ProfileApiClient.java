package com.github.phoswald.fitbit.viewer.profile;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "fitbit-api")
@Path("/1/user/-/profile.json")
interface ProfileApiClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ProfileResponse getProfile(@HeaderParam("Authorization") String authorizationHeader);

    record ProfileResponse(
            UserData user
    ) { }

    record UserData(
            String displayName,
            String fullName,
            Integer age,
            String gender,
            String avatar
    ) { }
}
