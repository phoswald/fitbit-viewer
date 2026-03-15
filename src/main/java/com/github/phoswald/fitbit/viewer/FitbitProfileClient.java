package com.github.phoswald.fitbit.viewer;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "fitbit-api")
@Path("/1/user/-/profile.json")
public interface FitbitProfileClient {

    record Response(UserData user) {
        record UserData(
                String displayName,
                String fullName,
                int age,
                String gender,
                @JsonProperty("avatar") String avatarUrl) {}
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response get(@HeaderParam("Authorization") String authorization);
}
